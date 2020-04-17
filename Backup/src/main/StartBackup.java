package main;

import java.io.IOException;

import main.action.Backup;
import main.action.Copy;
import main.action.FileTree;

public class StartBackup {

	// Copy properties
	private static String rootFolderPath = "/Users/paolobaioni/Documents/Music";

	private static String[] extensionsToCopy = {};

	// Backup properties
	private static String testSyncSource = "/Users/paolobaioni/TestSource";
	private static String syncSource = "/Users/paolobaioni/Documents/";

	private static String testSyncDest = "/Users/paolobaioni/TestDest";
	private static String syncDest = "/Volumes/Paolo_backup/Documents/";

	public static void main(String[] args) throws IOException {

		boolean isTest = true;

		for (String arg : args) {
			isTest = false;
			if (!arg.equals("")) {
				Backup backup = new Backup(syncSource + arg, syncDest + arg);
				backup.synchronizeFolders();
			}
		}

		if (isTest) {
			Copy copy = new Copy(rootFolderPath);
			for (String extension : extensionsToCopy) {
				copy.copyFilesByExtension(extension);
			}

			Backup backup = new Backup(testSyncSource, testSyncDest);
			backup.synchronizeFolders();
		}

	}

}
