package us.mbilker.minecraftportable;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

public class Util
{
  private static File workDir = null;

  public static OS getPlatform()
  {
    String str = System.getProperty("os.name").toLowerCase();
    if (str.contains("win")) return OS.WINDOWS;
    if (str.contains("mac")) return OS.MACOS;
    if (str.contains("solaris")) return OS.SOLARIS;
    if (str.contains("sunos")) return OS.SOLARIS;
    if (str.contains("linux")) return OS.LINUX;
    if (str.contains("unix")) return OS.LINUX;
    return OS.UNKNOWN;
  }

  public static File getWorkingDirectory()
  {
    if (workDir == null) workDir = getWorkingDirectory("minecraft");
    return workDir;
  }

  public static File getWorkingDirectory(String paramString) {
    File localFile = MinecraftPortable.clientDir;
    if ((!localFile.exists()) && (!localFile.mkdirs()))
      throw new RuntimeException("The working directory could not be created: " + localFile);
    return localFile;
  }

  public static String buildQuery(Map<String, Object> paramMap) {
    StringBuilder localStringBuilder = new StringBuilder();

    for (Map.Entry<String, Object> localEntry : paramMap.entrySet()) {
      if (localStringBuilder.length() > 0) {
        localStringBuilder.append('&');
      }
      try
      {
        localStringBuilder.append(URLEncoder.encode((String)localEntry.getKey(), "UTF-8"));
      } catch (UnsupportedEncodingException localUnsupportedEncodingException1) {
        localUnsupportedEncodingException1.printStackTrace();
      }

      if (localEntry.getValue() != null) {
        localStringBuilder.append('=');
        try {
          localStringBuilder.append(URLEncoder.encode(localEntry.getValue().toString(), "UTF-8"));
        } catch (UnsupportedEncodingException localUnsupportedEncodingException2) {
          localUnsupportedEncodingException2.printStackTrace();
        }
      }
    }

    return localStringBuilder.toString();
  }

  public static String executePost(String paramString, Map<String, Object> paramMap) {
    return executePost(paramString, buildQuery(paramMap));
  }

  public static String executePost(String paramString1, String paramString2)
  {
    HttpsURLConnection localHttpsURLConnection = null;
    try
    {
      URL localURL = new URL(paramString1);
      localHttpsURLConnection = (HttpsURLConnection)localURL.openConnection();
      localHttpsURLConnection.setRequestMethod("POST");
      localHttpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

      localHttpsURLConnection.setRequestProperty("Content-Length", "" + Integer.toString(paramString2.getBytes().length));
      localHttpsURLConnection.setRequestProperty("Content-Language", "en-US");

      localHttpsURLConnection.setUseCaches(false);
      localHttpsURLConnection.setDoInput(true);
      localHttpsURLConnection.setDoOutput(true);

      localHttpsURLConnection.connect();
      Certificate[] arrayOfCertificate = localHttpsURLConnection.getServerCertificates();

      byte[] arrayOfByte1 = new byte[294];
      DataInputStream localDataInputStream = new DataInputStream(Util.class.getResourceAsStream("minecraft.key"));
      localDataInputStream.readFully(arrayOfByte1);
      localDataInputStream.close();

      Certificate localCertificate = arrayOfCertificate[0];
      PublicKey localPublicKey = localCertificate.getPublicKey();
      byte[] arrayOfByte2 = localPublicKey.getEncoded();

      for (int i = 0; i < arrayOfByte2.length; i++) {
        if (arrayOfByte2[i] != arrayOfByte1[i]) throw new RuntimeException("Public key mismatch");

      }

      DataOutputStream localDataOutputStream = new DataOutputStream(localHttpsURLConnection.getOutputStream());
      localDataOutputStream.writeBytes(paramString2);
      localDataOutputStream.flush();
      localDataOutputStream.close();

      InputStream localInputStream = localHttpsURLConnection.getInputStream();
      BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localInputStream));

      StringBuffer localStringBuffer = new StringBuffer();
      String str1;
      while ((str1 = localBufferedReader.readLine()) != null) {
        localStringBuffer.append(str1);
        localStringBuffer.append('\r');
      }
      localBufferedReader.close();

      return localStringBuffer.toString();
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
      return null;
    }
    finally
    {
      if (localHttpsURLConnection != null)
        localHttpsURLConnection.disconnect();
    }
  }

  public static boolean isEmpty(String paramString)
  {
    return (paramString == null) || (paramString.length() == 0);
  }

  public static void openLink(URI paramURI) {
    try {
      Object localObject = Class.forName("java.awt.Desktop").getMethod("getDesktop", new Class[0]).invoke(null, new Object[0]);
      localObject.getClass().getMethod("browse", new Class[] { URI.class }).invoke(localObject, new Object[] { paramURI });
    } catch (Throwable localThrowable) {
      System.out.println("Failed to open link " + paramURI.toString());
    }
  }

  public static enum OS
  {
    LINUX, 
    SOLARIS, 
    WINDOWS, 
    MACOS, 
    UNKNOWN;
  }
}