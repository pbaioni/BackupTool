package main.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import main.BackupOptions;
import main.BackupResult;
import main.helper.FileHelper;

public class Backup {

	private String syncSource;

	private String syncDest;

	private static final String ARCHIVE = "/BackupArchive";

	private static final String LOG_FOLDER = "/Results";

	private String backupStartTime;

	private List<String> filesToBackup;

	private List<String> commonFiles;

	private List<String> filesToRemove;

	private List<String> log;

	BackupResult result;

	public Backup() {

	}

	public Backup(String syncSource, String syncDest) throws IOException {
		this.syncSource = syncSource;
		this.syncDest = syncDest;
		result = new BackupResult();
		log = new ArrayList<String>();
		File destFolder = new File(syncDest);
		destFolder.mkdirs();
	}

	private void calculateBackupAction(List<BackupOptions> backupActions) {

		boolean doCopy = backupActions.contains(BackupOptions.COPY);

		boolean doUpdate = backupActions.contains(BackupOptions.UPDATE);

		boolean doArchive = backupActions.contains(BackupOptions.ARCHIVE);

		boolean doDelete = backupActions.contains(BackupOptions.DELETE);

		List<String> sourceFiles = new ArrayList<String>();
		List<String> destFiles = new ArrayList<String>();

		try (Stream<Path> sourcePaths = Files.walk(Paths.get(syncSource));
				Stream<Path> destPaths = Files.walk(Paths.get(syncDest))) {

			sourceFiles = sourcePaths.map(p -> new File(p.toString()))
					.filter(f -> !f.getAbsolutePath().equals(syncSource))
					.map(f -> f.getAbsolutePath().replace(syncSource, "")).collect(Collectors.toList());

			destFiles = destPaths.map(p -> new File(p.toString())).filter(f -> !f.getAbsolutePath().equals(syncDest))
					.map(f -> f.getAbsolutePath().replace(syncDest, "")).collect(Collectors.toList());

			if (doCopy) {
				printToLogAndConsole("Calculating files to backup...");
				filesToBackup = new ArrayList<String>(sourceFiles);
				filesToBackup.removeAll(destFiles);
				filesToBackup.remove("");
				// copy files existing in source folder but missing in destination folder
				backupNewFiles();
			}

			if (doUpdate) {
				printToLogAndConsole("Calculating common files to update...");
				commonFiles = new ArrayList<String>(sourceFiles);
				commonFiles.retainAll(destFiles);
				// update files existing in destination folder but recently modified in source
				// folder
				updateModifiedFiles();
			}

			if (doArchive || doDelete) {
				printToLogAndConsole("Calculating obsolete backup files to archive and remove...");
				filesToRemove = new ArrayList<String>(destFiles);
				filesToRemove.removeAll(sourceFiles);
				filesToRemove.removeIf(s -> s.contains(ARCHIVE));
			}

			// archive files existing in destination folder but removed in source folder
			if (doArchive) {
				archiveObsoleteFiles();
			}

			if (doDelete) {
				removeObsoleteFiles();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public BackupResult synchronizeFolders(List<BackupOptions> options) throws IOException {

		Calendar start = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		backupStartTime = sdf.format(start.getTime());

		printToLogAndConsole("********* Start: " + backupStartTime + " *********");
		printToLogAndConsole("Synchronising folders " + syncSource + " and " + syncDest + "");
		printToLogAndConsole("");

		calculateBackupAction(options);

		Calendar end = Calendar.getInstance();
		String backupEndTime = sdf.format(end.getTime());
		Duration diff = Duration.between(start.toInstant(), end.toInstant());
		long hours = diff.toHours();
		long minutes = diff.toMinutes() - 60 * hours;
		long seconds = diff.getSeconds() - 60 * minutes - 3600 * hours;

		String duration = hours + " hours, " + minutes + " minutes, " + seconds + " seconds";

		printToLogAndConsole("********* End: " + backupEndTime + " ( " + duration + " )  *********");
		printToLogAndConsole("");

		// writeLog();

		return result;

	}

	private void backupNewFiles() throws IOException {

		int countFiles = 0;
		int countFolders = 0;
		if (filesToBackup.isEmpty()) {

			printToLogAndConsole("Nothing to backup");

		} else {

			printToLogAndConsole("Starting to copy...");

			for (String fileToBackup : filesToBackup) {
				try {
					File source = new File(syncSource + fileToBackup);
					if (source.isDirectory()) {
						printToLogAndConsole("Creating folder " + fileToBackup);

						Files.copy(Paths.get(syncSource + fileToBackup), Paths.get(syncDest + fileToBackup),
								StandardCopyOption.REPLACE_EXISTING);
						countFolders++;
					} else {
						printToLogAndConsole("Saving " + fileToBackup);

						Files.copy(Paths.get(syncSource + fileToBackup), Paths.get(syncDest + fileToBackup),
								StandardCopyOption.REPLACE_EXISTING);
						countFiles++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			printToLogAndConsole(countFiles + " files and " + countFolders + " folders saved");
		}

		printToLogAndConsole("");

		result.setCopiedFiles(countFiles);
		result.setCopiedFolders(countFolders);

	}

	private void updateModifiedFiles() throws IOException {

		int countFiles = 0;
		int countFolders = 0;

		if (commonFiles.isEmpty()) {

			printToLogAndConsole("No file to update");

		} else {

			printToLogAndConsole("Starting to update...");

			for (String fileToUpdate : commonFiles) {

				File source = new File(syncSource + fileToUpdate);
				File dest = new File(syncDest + fileToUpdate);
				boolean updated = source.lastModified() > dest.lastModified();

				if (updated) {
					try {
						if (!dest.isDirectory()) {
							printToLogAndConsole("Updating " + syncDest + fileToUpdate);
							Files.copy(Paths.get(syncSource + fileToUpdate), Paths.get(syncDest + fileToUpdate),
									StandardCopyOption.REPLACE_EXISTING);
							countFiles++;
						} else {
							printToLogAndConsole("Updating modification date for folder " + syncDest + fileToUpdate);
							dest.setLastModified(source.lastModified());
							countFolders++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (countFiles == 0 && countFolders == 0) {

				printToLogAndConsole("No file to update");

			} else {

				printToLogAndConsole(countFiles + " files and " + countFolders + " folders updated");

			}
		}

		printToLogAndConsole("");

		result.setUpdatedFiles(countFiles);
		result.setUpdatedFolders(countFolders);

	}

	private void archiveObsoleteFiles() throws IOException {

		if (filesToRemove.isEmpty()) {

			printToLogAndConsole("Nothing to remove");

		} else {

			// zipping and deleting obsolete files
			printToLogAndConsole("Starting to archive files...");

			// creating archive folder
			File archiveFile = new File(syncDest + ARCHIVE);
			archiveFile.mkdirs();

			// opening streams
			FileOutputStream fos = new FileOutputStream(
					syncDest + ARCHIVE + "/ObsoleteFiles_" + backupStartTime + ".zip");
			ZipOutputStream zipOut = new ZipOutputStream(fos);

			for (String fileToRemoveName : filesToRemove) {
				try {
					File fileToRemove = new File(syncDest + fileToRemoveName);
					if (!fileToRemove.isDirectory()) {
						printToLogAndConsole("Archiving " + syncDest + fileToRemoveName);

						// adding file to archive zip
						ZipEntry zipEntry = new ZipEntry(backupStartTime + "/" + fileToRemove.getName());
						zipOut.putNextEntry(zipEntry);
						zipOut.write(FileHelper.getFileContentAsBytes(fileToRemove.getAbsolutePath()));
						zipOut.closeEntry();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// closing streams
			zipOut.close();
			fos.close();

		}
	}

	private void removeObsoleteFiles() throws IOException {

		List<String> foldersToRemove = new ArrayList<String>();

		int countFiles = 0;
		int countFolders = 0;

		if (filesToRemove.isEmpty()) {

			printToLogAndConsole("Nothing to remove");

		} else {

			// deleting obsolete files
			printToLogAndConsole("Starting to remove files...");

			for (String fileToRemoveName : filesToRemove) {
				try {
					File fileToRemove = new File(syncDest + fileToRemoveName);

					if (!fileToRemove.isDirectory()) {
						printToLogAndConsole("Removing " + syncDest + fileToRemoveName);

						// deleting file
						fileToRemove.delete();

						countFiles++;
					} else {
						foldersToRemove.add(fileToRemoveName);
						countFolders++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

		// ordering list by entry length (longer items first)
		foldersToRemove.sort(Comparator.comparingInt(String::length));
		Collections.reverse(foldersToRemove);

		// deleting obsolete folders
		for (String folderToRemove : foldersToRemove) {
			try {
				File dest = new File(syncDest + folderToRemove);
				printToLogAndConsole("Removing folder " + syncDest + folderToRemove);
				dest.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		printToLogAndConsole(countFiles + " files and " + countFolders + " folders removed");

		printToLogAndConsole("");

		result.setRemovedFiles(countFiles);
		result.setRemovedFolders(countFolders);

	}

	public void clearResults() {
		result.clear();
	}

	private void printToLogAndConsole(String line) {

		System.out.println(line);

		log.add(line);
	}

	private void writeLog() {

		File logs = new File(syncDest + ARCHIVE + LOG_FOLDER);
		logs.mkdirs();
		String logPath = syncDest + ARCHIVE + LOG_FOLDER + "/backupResult_" + backupStartTime + ".txt";
		FileHelper.writeLinesInFile(logPath, log);

	}

}
