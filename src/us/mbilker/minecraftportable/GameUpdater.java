package us.mbilker.minecraftportable;

import java.applet.Applet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.SocketPermission;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.PermissionCollection;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

public class GameUpdater
  implements Runnable
{
  public static final int STATE_INIT = 1;
  public static final int STATE_DETERMINING_PACKAGES = 2;
  public static final int STATE_CHECKING_CACHE = 3;
  public static final int STATE_DOWNLOADING = 4;
  public static final int STATE_EXTRACTING_PACKAGES = 5;
  public static final int STATE_UPDATING_CLASSPATH = 6;
  public static final int STATE_SWITCHING_APPLET = 7;
  public static final int STATE_INITIALIZE_REAL_APPLET = 8;
  public static final int STATE_START_REAL_APPLET = 9;
  public static final int STATE_DONE = 10;
  public int percentage;
  public int currentSizeDownload;
  public int totalSizeDownload;
  public int currentSizeExtract;
  public int totalSizeExtract;
  protected URL[] urlList;
  private static ClassLoader classLoader;
  protected Thread loaderThread;
  public boolean fatalError;
  public String fatalErrorDescription;
  protected String subtaskMessage = "";
  protected int state = 1;

  protected boolean lzmaSupported = false;
  protected boolean pack200Supported = false;
  protected boolean certificateRefused;
  protected static boolean natives_loaded = false;
  public static boolean forceUpdate = false;
  private String latestVersion;
  private String mainGameUrl;
  public boolean pauseAskUpdate;
  public boolean shouldUpdate;
  public boolean skipUpdate;

  public GameUpdater(String paramString1, String paramString2, boolean paramBoolean)
  {
    latestVersion = paramString1;
    mainGameUrl = paramString2;
    skipUpdate = paramBoolean;
  }

  public void init() {
    state = 1;
    try
    {
      Class.forName("LZMA.LzmaInputStream");
      lzmaSupported = true;
    }
    catch (ClassNotFoundException localClassNotFoundException) {
    }
    try {
      Pack200.class.getSimpleName();
      pack200Supported = true;
    } catch (Throwable localThrowable) {
    }
  }

  private String generateStacktrace(Exception paramException) {
    StringWriter localStringWriter = new StringWriter();
    PrintWriter localPrintWriter = new PrintWriter(localStringWriter);
    paramException.printStackTrace(localPrintWriter);
    return localStringWriter.toString();
  }

  protected String getDescriptionForState()
  {
    switch (state) {
    case 1:
      return "Initializing loader";
    case 2:
      return "Determining packages to load";
    case 3:
      return "Checking cache for existing files";
    case 4:
      return "Downloading packages";
    case 5:
      return "Extracting downloaded packages";
    case 6:
      return "Updating classpath";
    case 7:
      return "Switching applet";
    case 8:
      return "Initializing real applet";
    case 9:
      return "Starting real applet";
    case 10:
      return "Done loading";
    }
    return "unknown state";
  }

  protected String trimExtensionByCapabilities(String paramString)
  {
    if (!pack200Supported) {
      paramString = paramString.replaceAll(".pack", "");
    }

    if (!lzmaSupported) {
      paramString = paramString.replaceAll(".lzma", "");
    }
    return paramString;
  }

  protected void loadJarURLs() throws Exception {
    state = 2;
    String str1 = "lwjgl.jar, jinput.jar, lwjgl_util.jar, " + mainGameUrl;
    str1 = trimExtensionByCapabilities(str1);

    StringTokenizer localStringTokenizer = new StringTokenizer(str1, ", ");
    int i = localStringTokenizer.countTokens() + 1;

    urlList = new URL[i];
    URL localURL = new URL("http://s3.amazonaws.com/MinecraftDownload/");

    for (int j = 0; j < i - 1; j++) {
      urlList[j] = new URL(localURL, localStringTokenizer.nextToken());
    }

    String str2 = System.getProperty("os.name");
    String str3 = null;

    if (str2.startsWith("Win"))
      str3 = "windows_natives.jar.lzma";
    else if (str2.startsWith("Linux"))
      str3 = "linux_natives.jar.lzma";
    else if (str2.startsWith("Mac"))
      str3 = "macosx_natives.jar.lzma";
    else if ((str2.startsWith("Solaris")) || (str2.startsWith("SunOS")))
      str3 = "solaris_natives.jar.lzma";
    else {
      fatalErrorOccured("OS (" + str2 + ") not supported", null);
    }

    if (str3 == null) {
      fatalErrorOccured("no lwjgl natives files found", null);
    } else {
      str3 = trimExtensionByCapabilities(str3);
      urlList[(i - 1)] = new URL(localURL, str3);
    }
  }

  @Override
public void run()
  {
    init();
    state = 3;

    percentage = 5;
    try
    {
      loadJarURLs();

      String str = (String)AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
        @Override
		public Object run() throws Exception {
          return Util.getWorkingDirectory() + File.separator + "bin" + File.separator;
        }
      });
      File localFile1 = new File(str);

      if (!localFile1.exists()) {
        localFile1.mkdirs();
      }

      if (latestVersion != null) {
        File localFile2 = new File(localFile1, "version");

        int i = 0;
        if ((!skipUpdate) && (!forceUpdate) && (localFile2.exists()) && (
          (latestVersion.equals("-1")) || (latestVersion.equals(readVersionFile(localFile2))))) {
          i = 1;
          percentage = 90;
        }

        if ((!skipUpdate) && ((forceUpdate) || (i == 0))) {
          shouldUpdate = true;
          if ((!forceUpdate) && (localFile2.exists()))
          {
            checkShouldUpdate();
          }
          if (shouldUpdate)
          {
            writeVersionFile(localFile2, "");

            downloadJars(str);
            extractJars(str);
            extractNatives(str);

            if (latestVersion != null) {
              percentage = 90;
              writeVersionFile(localFile2, latestVersion);
            }
          } else {
            i = 1;
            percentage = 90;
          }
        }
      }

      updateClassPath(localFile1);
      state = 10;
    } catch (AccessControlException localAccessControlException) {
      fatalErrorOccured(localAccessControlException.getMessage(), localAccessControlException);
      certificateRefused = true;
    } catch (Exception localException) {
      fatalErrorOccured(localException.getMessage(), localException);
    } finally {
      loaderThread = null;
    }
  }

  private void checkShouldUpdate() {
    pauseAskUpdate = true;
    while (pauseAskUpdate)
      try {
        Thread.sleep(1000L);
      } catch (InterruptedException localInterruptedException) {
        localInterruptedException.printStackTrace();
      }
  }

  protected String readVersionFile(File paramFile) throws Exception
  {
    DataInputStream localDataInputStream = new DataInputStream(new FileInputStream(paramFile));
    String str = localDataInputStream.readUTF();
    localDataInputStream.close();
    return str;
  }

  protected void writeVersionFile(File paramFile, String paramString) throws Exception {
    DataOutputStream localDataOutputStream = new DataOutputStream(new FileOutputStream(paramFile));
    localDataOutputStream.writeUTF(paramString);
    localDataOutputStream.close();
  }

  protected void updateClassPath(File paramFile) throws Exception
  {
    state = 6;

    percentage = 95;

    //URL[] arrayOfURL = new URL[urlList.length];
    /* for (int i = 0; i < urlList.length; i++) {
      arrayOfURL[i] = new File(paramFile, getJarName(urlList[i])).toURI().toURL();
    } */
    
    URL[] arrayOfURL = new URL[(MinecraftPortable.ignore.length + 1)];
    
    for (int i = 0; i < MinecraftPortable.ignore.length; i++) {
    	arrayOfURL[i] = new File(paramFile, MinecraftPortable.ignore[i]).toURI().toURL();
    	//Main.log("JAR: %s", new File(paramFile, MinecraftPortable.ignore[i]));
    }
    
    arrayOfURL[arrayOfURL.length-1] = new File(paramFile, ((String)LoginForm.jarBox.getSelectedItem())).toURI().toURL();
    //Main.log("JAR1: %s", arrayOfURL[arrayOfURL.length-1]);

    if (classLoader == null) {
      classLoader = new URLClassLoader(arrayOfURL)
      {
        @Override
		protected PermissionCollection getPermissions(CodeSource paramAnonymousCodeSource) {
          PermissionCollection localPermissionCollection = null;
          try
          {
            Method localMethod = SecureClassLoader.class.getDeclaredMethod("getPermissions", new Class[] { CodeSource.class });
            localMethod.setAccessible(true);
            localPermissionCollection = (PermissionCollection)localMethod.invoke(getClass().getClassLoader(), new Object[] { paramAnonymousCodeSource });

            localPermissionCollection.add(new SocketPermission("www.minecraft.net", "connect,accept"));
            localPermissionCollection.add(new FilePermission("<<ALL FILES>>", "read"));
          }
          catch (Exception localException) {
            localException.printStackTrace();
          }

          return localPermissionCollection;
        }
      };
    }

    String str = paramFile.getAbsolutePath();
    if (!str.endsWith(File.separator)) str = str + File.separator;
    unloadNatives(str);

    System.setProperty("org.lwjgl.librarypath", str + "natives");
    System.setProperty("net.java.games.input.librarypath", str + "natives");

    natives_loaded = true;
  }

  @SuppressWarnings("rawtypes")
  private void unloadNatives(String paramString) {
    if (!natives_loaded) {
      return;
    }
    try
    {
      Field localField = ClassLoader.class.getDeclaredField("loadedLibraryNames");
      localField.setAccessible(true);
      Vector localVector = (Vector)localField.get(getClass().getClassLoader());

      String str1 = new File(paramString).getCanonicalPath();

      for (int i = 0; i < localVector.size(); i++) {
        String str2 = (String)localVector.get(i);

        if (str2.startsWith(str1)) {
          localVector.remove(i);
          i--;
        }
      }
    } catch (Exception localException) {
      localException.printStackTrace();
    }
  }

  @SuppressWarnings("rawtypes")
  public Applet createApplet() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
	Launcher.setMinecraftDirectory(classLoader, MinecraftPortable.clientDir);
	Class localClass = classLoader.loadClass("net.minecraft.client.MinecraftApplet");
    return (Applet)localClass.newInstance();
  }

  protected void downloadJars(String paramString) throws Exception {
    File localFile = new File(paramString, "md5s");
    Properties localProperties = new Properties();
    if (localFile.exists()) {
      try {
        FileInputStream localFileInputStream = new FileInputStream(localFile);
        localProperties.load(localFileInputStream);
        localFileInputStream.close();
      } catch (Exception localException1) {
        localException1.printStackTrace();
      }
    }
    state = 4;
    
    int i;

    int[] arrayOfInt = new int[urlList.length];
    boolean[] arrayOfBoolean = new boolean[urlList.length];
    URLConnection localURLConnection;
    String localObject;
    for (int i1 = 0; i1 < urlList.length; i1++) {
      localURLConnection = urlList[i1].openConnection();
      localURLConnection.setDefaultUseCaches(false);
      arrayOfBoolean[i1] = false;
      if ((localURLConnection instanceof HttpURLConnection)) {
        ((HttpURLConnection)localURLConnection).setRequestMethod("HEAD");

        localObject = "\"" + localProperties.getProperty(getFileName(urlList[i1])) + "\"";

        if ((!forceUpdate) && (localObject != null)) localURLConnection.setRequestProperty("If-None-Match", (String)localObject);

        int j = ((HttpURLConnection)localURLConnection).getResponseCode();
        if (j / 100 == 3) {
          arrayOfBoolean[i1] = true;
        }
      }
      arrayOfInt[i1] = localURLConnection.getContentLength();
      totalSizeDownload += arrayOfInt[i1];
    }

    i = this.percentage = 10;

    Object localObject1 = new byte[65536];
    for (int j = 0; j < urlList.length; j++) {
      if (arrayOfBoolean[j] != false) {
        percentage = (i + arrayOfInt[j] * 45 / totalSizeDownload);
      }
      else
      {
        try
        {
          localProperties.remove(getFileName(urlList[j]));
          localProperties.store(new FileOutputStream(localFile), "md5 hashes for downloaded files");
        } catch (Exception localException2) {
          localException2.printStackTrace();
        }

        int k = 0;
        int m = 3;
        int n = 1;

        while (n != 0) {
          n = 0;

          localURLConnection = urlList[j].openConnection();

          String str1 = "";

          if ((localURLConnection instanceof HttpURLConnection)) {
            localURLConnection.setRequestProperty("Cache-Control", "no-cache");
            localURLConnection.connect();

            str1 = localURLConnection.getHeaderField("ETag");
            str1 = str1.substring(1, str1.length() - 1);
          }

          String str2 = getFileName(urlList[j]);
          InputStream localInputStream = getJarInputStream(str2, localURLConnection);
          FileOutputStream localFileOutputStream = new FileOutputStream(paramString + str2);

          long l1 = System.currentTimeMillis();
          int i2 = 0;
          int i3 = 0;
          String str3 = "";

          MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
          int i1;
          
          while ((i1 = localInputStream.read((byte[])localObject1, 0, ((byte[])localObject1).length)) != -1) {
            localFileOutputStream.write((byte[])localObject1, 0, i1);
            localMessageDigest.update((byte[])localObject1, 0, i1);
            currentSizeDownload += i1;
            i3 += i1;
            percentage = (i + currentSizeDownload * 45 / totalSizeDownload);
            subtaskMessage = ("Retrieving: " + str2 + " " + currentSizeDownload * 100 / totalSizeDownload + "%");

            i2 += i1;
            long l2 = System.currentTimeMillis() - l1;

            if (l2 >= 1000L) {
              float f = i2 / (float)l2;
              f = (int)(f * 100.0F) / 100.0F;
              str3 = " @ " + f + " KB/sec";
              i2 = 0;
              l1 += 1000L;
            }

            subtaskMessage += str3;
          }

          localInputStream.close();
          localFileOutputStream.close();
          String str4 = new BigInteger(1, localMessageDigest.digest()).toString(16);
          while (str4.length() < 32) {
            str4 = "0" + str4;
          }
          boolean bool = true;
          if (str1 != null) {
            bool = str4.equals(str1);
          }

          if ((localURLConnection instanceof HttpURLConnection)) {
            if ((bool) && ((i3 == arrayOfInt[j]) || (arrayOfInt[j] <= 0)))
            {
              try {
                localProperties.setProperty(getFileName(urlList[j]), str1);
                localProperties.store(new FileOutputStream(localFile), "md5 hashes for downloaded files");
              } catch (Exception localException3) {
                localException3.printStackTrace();
              }
            } else {
              k++;
              if (k < m) {
                n = 1;
                currentSizeDownload -= i3;
              } else {
                throw new Exception("failed to download " + str2);
              }
            }
          }
        }
      }
    }
    subtaskMessage = "";
  }

  protected InputStream getJarInputStream(String paramString, final URLConnection paramURLConnection)
    throws Exception
  {
    final InputStream[] arrayOfInputStream = new InputStream[1];

    for (int i = 0; (i < 3) && (arrayOfInputStream[0] == null); i++) {
      Thread local3 = new Thread()
      {
        @Override
		public void run() {
          try {
            arrayOfInputStream[0] = paramURLConnection.getInputStream();
          }
          catch (IOException localIOException)
          {
          }
        }
      };
      local3.setName("JarInputStreamThread");
      local3.start();

      int j = 0;
      while ((arrayOfInputStream[0] == null) && (j++ < 5)) {
        try {
          local3.join(1000L);
        }
        catch (InterruptedException localInterruptedException1)
        {
        }
      }
      if (arrayOfInputStream[0] == null) {
        try {
          local3.interrupt();
          local3.join();
        }
        catch (InterruptedException localInterruptedException2)
        {
        }
      }
    }
    if (arrayOfInputStream[0] == null) {
      throw new Exception("Unable to download " + paramString);
    }

    return arrayOfInputStream[0];
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
protected void extractLZMA(String paramString1, String paramString2)
    throws Exception
  {
    File localFile = new File(paramString1);
    if (!localFile.exists()) return;
    FileInputStream localFileInputStream = new FileInputStream(localFile);

    Class localClass = Class.forName("LZMA.LzmaInputStream");
    Constructor localConstructor = localClass.getDeclaredConstructor(new Class[] { InputStream.class });
    InputStream localInputStream = (InputStream)localConstructor.newInstance(new Object[] { localFileInputStream });

    FileOutputStream localFileOutputStream = new FileOutputStream(paramString2);

    byte[] arrayOfByte = new byte[16384];

    int i = localInputStream.read(arrayOfByte);
    while (i >= 1) {
      localFileOutputStream.write(arrayOfByte, 0, i);
      i = localInputStream.read(arrayOfByte);
    }

    localInputStream.close();
    localFileOutputStream.close();

    localFileOutputStream = null;
    localInputStream = null;

    localFile.delete();
  }

  protected void extractPack(String paramString1, String paramString2)
    throws Exception
  {
    File localFile = new File(paramString1);
    if (!localFile.exists()) return;

    FileOutputStream localFileOutputStream = new FileOutputStream(paramString2);
    JarOutputStream localJarOutputStream = new JarOutputStream(localFileOutputStream);

    Pack200.Unpacker localUnpacker = Pack200.newUnpacker();
    localUnpacker.unpack(localFile, localJarOutputStream);
    localJarOutputStream.close();

    localFile.delete();
  }

  protected void extractJars(String paramString)
    throws Exception
  {
    state = 5;

    float f = 10.0F / urlList.length;

    for (int i = 0; i < urlList.length; i++) {
      percentage = (55 + (int)(f * i + 1));
      String str = getFileName(urlList[i]);

      if (str.endsWith(".pack.lzma")) {
        subtaskMessage = ("Extracting: " + str + " to " + str.replaceAll(".lzma", ""));
        extractLZMA(paramString + str, paramString + str.replaceAll(".lzma", ""));

        subtaskMessage = ("Extracting: " + str.replaceAll(".lzma", "") + " to " + str.replaceAll(".pack.lzma", ""));
        extractPack(paramString + str.replaceAll(".lzma", ""), paramString + str.replaceAll(".pack.lzma", ""));
      } else if (str.endsWith(".pack")) {
        subtaskMessage = ("Extracting: " + str + " to " + str.replace(".pack", ""));
        extractPack(paramString + str, paramString + str.replace(".pack", ""));
      } else if (str.endsWith(".lzma")) {
        subtaskMessage = ("Extracting: " + str + " to " + str.replace(".lzma", ""));
        extractLZMA(paramString + str, paramString + str.replace(".lzma", ""));
      }
    }
  }

  @SuppressWarnings("rawtypes")
protected void extractNatives(String paramString) throws Exception
  {
    state = 5;

    int i = percentage;

    String str = getJarName(urlList[(urlList.length - 1)]);

    Certificate[] arrayOfCertificate = Launcher.class.getProtectionDomain().getCodeSource().getCertificates();
    
    URL localObject1;
    JarURLConnection localObject2;
    
    if (arrayOfCertificate == null) {
      localObject1 = Launcher.class.getProtectionDomain().getCodeSource().getLocation();

      localObject2 = (JarURLConnection)new URL("jar:" + ((URL)localObject1).toString() + "!/net/minecraft/Launcher.class").openConnection();
      ((JarURLConnection)localObject2).setDefaultUseCaches(true);
      try {
        arrayOfCertificate = ((JarURLConnection)localObject2).getCertificates();
      }
      catch (Exception localException)
      {
      }
    }
    Object localObject3 = new File(paramString + "natives");
    if (!((File)localObject3).exists()) {
      ((File)localObject3).mkdir();
    }

    Object localObject4 = new File(paramString + str);
    if (!((File)localObject4).exists()) return;
    JarFile localJarFile = new JarFile((File)localObject4, true);
    Enumeration localEnumeration = localJarFile.entries();

    totalSizeExtract = 0;

    while (localEnumeration.hasMoreElements()) {
      localObject3 = (JarEntry)localEnumeration.nextElement();

      if ((!((JarEntry)localObject3).isDirectory()) && (((JarEntry)localObject3).getName().indexOf('/') == -1))
      {
        totalSizeExtract = (int)(totalSizeExtract + ((JarEntry)localObject3).getSize());
      }
    }
    currentSizeExtract = 0;

    localEnumeration = localJarFile.entries();

    while (localEnumeration.hasMoreElements()) {
      localObject3 = (JarEntry)localEnumeration.nextElement();

      if ((!((JarEntry)localObject3).isDirectory()) && (((JarEntry)localObject3).getName().indexOf('/') == -1))
      {
        File localFile = new File(paramString + "natives" + File.separator + ((JarEntry)localObject3).getName());
        if ((!localFile.exists()) || 
          (localFile.delete()))
        {
          InputStream localInputStream = localJarFile.getInputStream(localJarFile.getEntry(((JarEntry)localObject3).getName()));
          FileOutputStream localFileOutputStream = new FileOutputStream(paramString + "natives" + File.separator + ((JarEntry)localObject3).getName());

          byte[] arrayOfByte = new byte[65536];
          int j;
          while ((j = localInputStream.read(arrayOfByte, 0, arrayOfByte.length)) != -1) {
            localFileOutputStream.write(arrayOfByte, 0, j);
            currentSizeExtract += j;

            percentage = (i + currentSizeExtract * 20 / totalSizeExtract);
            subtaskMessage = ("Extracting: " + ((JarEntry)localObject3).getName() + " " + currentSizeExtract * 100 / totalSizeExtract + "%");
          }

          validateCertificateChain(arrayOfCertificate, ((JarEntry)localObject3).getCertificates());

          localInputStream.close();
          localFileOutputStream.close();
        }
      }
    }
    subtaskMessage = "";

    localJarFile.close();

    Object localObject5 = new File(paramString + str);
    ((File)localObject5).delete();
  }

  protected static void validateCertificateChain(Certificate[] paramArrayOfCertificate1, Certificate[] paramArrayOfCertificate2)
    throws Exception
  {
    if (paramArrayOfCertificate1 == null) return;
    if (paramArrayOfCertificate2 == null) {
      throw new Exception("Unable to validate certificate chain. Native entry did not have a certificate chain at all");
    }
    if (paramArrayOfCertificate1.length != paramArrayOfCertificate2.length) {
      throw new Exception("Unable to validate certificate chain. Chain differs in length [" + paramArrayOfCertificate1.length + " vs " + paramArrayOfCertificate2.length + "]");
    }
    for (int i = 0; i < paramArrayOfCertificate1.length; i++)
      if (!paramArrayOfCertificate1[i].equals(paramArrayOfCertificate2[i]))
        throw new Exception("Certificate mismatch: " + paramArrayOfCertificate1[i] + " != " + paramArrayOfCertificate2[i]);
  }

  protected String getJarName(URL paramURL)
  {
    String str = paramURL.getFile();

    if (str.contains("?")) {
      str = str.substring(0, str.indexOf("?"));
    }
    if (str.endsWith(".pack.lzma"))
      str = str.replaceAll(".pack.lzma", "");
    else if (str.endsWith(".pack"))
      str = str.replaceAll(".pack", "");
    else if (str.endsWith(".lzma")) {
      str = str.replaceAll(".lzma", "");
    }

    return str.substring(str.lastIndexOf('/') + 1);
  }

  protected String getFileName(URL paramURL) {
    String str = paramURL.getFile();
    if (str.contains("?")) {
      str = str.substring(0, str.indexOf("?"));
    }
    return str.substring(str.lastIndexOf('/') + 1);
  }

  protected void fatalErrorOccured(String paramString, Exception paramException) {
    paramException.printStackTrace();
    fatalError = true;
    fatalErrorDescription = ("Fatal error occured (" + state + "): " + paramString);
    System.out.println(fatalErrorDescription);
    System.out.println(generateStacktrace(paramException));
  }

  public boolean canPlayOffline()
  {
    try
    {
      String str1 = (String)AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
        @Override
		public Object run() throws Exception {
          return Util.getWorkingDirectory() + File.separator + "bin" + File.separator;
        }
      });
      File localFile = new File(str1);
      if (!localFile.exists()) return false;

      localFile = new File(localFile, "version");
      if (!localFile.exists()) return false;

      if (localFile.exists()) {
        String str2 = readVersionFile(localFile);
        if ((str2 != null) && (str2.length() > 0))
          return true;
      }
    }
    catch (Exception localException) {
      localException.printStackTrace();
      return false;
    }
    return false;
  }
}