package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import main.action.Backup;

public class StartBackup {

	private static String syncSource = "";
	private static String syncDest = "";
	private static BackupOptions backupMode;
	private static List<BackupOptions> backupActions = new ArrayList<BackupOptions>();

	public static void main(String[] args) throws IOException {

		if (args.length == 3) {

			// assign arguments
			getOptions(args[0]);
			syncSource = args[1];
			syncDest = args[2];

		}
		if (!backupActions.isEmpty()) {
			if (backupMode.equals(BackupOptions.SUBFOLDERS_MODE)) {
				File root = new File(syncSource);
				File[] fileList = root.listFiles();
				for (File file : fileList) {
					if (file.isDirectory()) {
						Backup backup = new Backup(syncSource + File.separator + file.getName(),
								syncDest + File.separator + file.getName());
						backup.synchronizeFolders(backupActions);
					}
				}
			} else if (backupMode.equals(BackupOptions.ROOT_FOLDER_MODE)) {
				Backup backup = new Backup(syncSource, syncDest);
				backup.synchronizeFolders(backupActions);
			} else {

				printHelp();
			}
		}

	}

	private static void getOptions(String firstArg) {

		if (firstArg.startsWith("-s")) {
			backupMode = BackupOptions.SUBFOLDERS_MODE;
		} else if (firstArg.startsWith("-r")) {
			backupMode = BackupOptions.ROOT_FOLDER_MODE;
		} else {
			printHelp();
			return;
		}

		if (firstArg.contains("c")) {
			backupActions.add(BackupOptions.COPY);
		}

		if (firstArg.contains("u")) {
			backupActions.add(BackupOptions.UPDATE);
		}

		if (firstArg.contains("a")) {
			backupActions.add(BackupOptions.ARCHIVE);
		}

		if (firstArg.contains("d")) {
			backupActions.add(BackupOptions.DELETE);
		}

	}

	private static void printHelp() {

		System.out.println("Help for backup script, this script needs 3 arguments:");
		System.out.println("");
		System.out.println("ex: bash backup.sh -s path1 path2");
		System.out.println(
				"this command backups each subfolder of path1 into path2, a backup result will be stored for each subfolder");
		System.out.println("");
		System.out.println("ex: bash backup.sh -r path1 path2");
		System.out.println("this command backups path1 into path2, a unique backup result will be stored into path2");

	}

}
