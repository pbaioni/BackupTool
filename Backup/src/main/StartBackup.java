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

		} else {
			printHelp();
		}

		if (!backupActions.isEmpty()) {

			// backup root folder in any case
			Backup backup = new Backup(syncSource, syncDest);
			backup.synchronizeFolders(backupActions);

			// backup subfolders if subfolders_mode is set
			if (backupMode.equals(BackupOptions.SUBFOLDERS_MODE)) {
				File root = new File(syncSource);
				File[] fileList = root.listFiles();
				for (File file : fileList) {
					if (file.isDirectory()) {
						Backup folderBackup = new Backup(syncSource + File.separator + file.getName(),
								syncDest + File.separator + file.getName());
						folderBackup.synchronizeFolders(backupActions);
					}
				}
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
		System.out.println("");
		System.out.println("Help for backup script, this script needs 3 arguments:");
		System.out.println("ex: bash backup.sh [mode+options] [source] [dest]");

		System.out.println("");
		System.out.println("Modes: [mandatory -s || -r]");
		System.out.println(
				"-s: performs a backup for each subfolder of source into dest. Each folder in path2 will have a specific backup result");
		System.out
				.println("-r: performs a backup of path1 into path2. A unique backup result will be stored into path2");
		System.out.println("Options: [optional]");
		System.out.println("c: copy new files of the source into destination");
		System.out.println(
				"u: update in dest all the files existing in both source and dest if source has a more recent version");
		System.out.println("a: archive (zip format) dest files no more existing in source");
		System.out.println("d: delete dest files no more existing in source");

		System.out.println("");
		System.out.println("ex: bash backup.sh -scuad path1 path2");
		System.out.println("ex: bash backup.sh -scu path1 path2");
		System.out.println("ex: bash backup.sh -rcd path1 path2");

	}

}
