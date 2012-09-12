package us.mbilker.minecraftportable;

import java.io.File;

public class MinecraftPortable {
	
	public Platform platform;
	
	public Platform getPlatform() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("linux")) return Platform.LINUX;
		if (osName.contains("unix")) return Platform.UNIX;
		if (osName.contains("solaris")) return Platform.SOLARIS;
		if (osName.contains("sunos")) return Platform.SUNOS;
		if (osName.contains("win")) return Platform.WINDOWS;
		if (osName.contains("mac")) return Platform.MAC;
		return Platform.UNIX;
	}
	
	public static File currentDir = new File(System.getProperty("user.dir", "."));
	public static File dataDir = new File(currentDir, "mcp_data");
	public static File launcherDir = new File(currentDir, "launcher");
	public static File serverDir = new File(currentDir, "server");
	
	public static File clientDir = new File(dataDir, "mc");
	
	public static File launcherFile = new File(launcherDir, "minecraft.jar");
	public static String launcherUrl = "https://s3.amazonaws.com/MinecraftDownload/launcher/minecraft.jar";
	
	public static File serverFile = new File(serverDir, "minecraft_server.jar");
	public static String serverUrl = "https://s3.amazonaws.com/MinecraftDownload/launcher/minecraft_server.jar";
	
	private Launcher launcher;
	
	public MinecraftPortable() {
		if (!dataDir.exists()) {
			Main.debugPrint("Data folder does not exist, creating. Typical on first start.");
			dataDir.mkdir();
		}
		
		if (!launcherDir.exists()) {
			Main.debugPrint("Launcher folder does not exist, creating. Typical on first start.");
			launcherDir.mkdir();
		}
		
		if (!serverDir.exists()) {
			Main.debugPrint("Server folder does not exist, creating. Typical on first start.");
			serverDir.mkdir();
		}
		
		if (!clientDir.exists()) {
			Main.debugPrint(".minecraft folder does not exist, creating. Typical on first start.");
			clientDir.mkdir();
		}
		
		launcher = new Launcher(launcherFile, launcherUrl);
	}

}
