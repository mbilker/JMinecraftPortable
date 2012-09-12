package us.mbilker.minecraftportable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Launcher {
	
	public Launcher(File outputFile, String url) {
		if (!outputFile.exists()) {
			try {
				URLConnection connection = new URL(url).openConnection();
				InputStream jarStream = connection.getInputStream();
				OutputStream jarFileOutput = new FileOutputStream(outputFile);
				
				byte buf[] = new byte[1024];
				int len;
				while((len=jarStream.read(buf))>0)
					jarFileOutput.write(buf,0,len);
				
				jarFileOutput.close();
				jarStream.close();
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
