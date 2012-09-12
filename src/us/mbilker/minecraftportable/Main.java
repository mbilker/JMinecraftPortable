package us.mbilker.minecraftportable;

public class Main {
	
	private static boolean debug = true;
	
	public static void debugPrint(String line) {
		if (debug == true) {
			System.out.println(line);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		debugPrint("foobar");
		
		new MinecraftPortable();
	}

}
