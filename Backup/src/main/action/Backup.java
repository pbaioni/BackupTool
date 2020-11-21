package main.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import main.BackupOptions;
import main.BackupResult;
import main.helper.FileHelper;
import main.helper.TimeHelper;

public class Backup {

	private static final Logger LOGGER = Logger.getLogger(Backup.class.getName());

	private String syncSource;

	private String syncDest;

	private static final String ARCHIVE_FOLDER = "/Backup/Archive";

	private static final String LOG_FOLDER = "/Backup/Log";

	private Calendar backupStartTime;

	private TreeSet<String> filesToCopy;

	private TreeSet<String> commonFiles;

	private TreeSet<String> filesToRemove;

	private List<String> log;

	BackupResult result;

	private BufferedReader answerReader;

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

	public BackupResult synchronizeFolders(List<BackupOptions> options) throws IOException {

		backupStartTime = TimeHelper.getNow();
		printToLogAndConsole("********* Start: " + TimeHelper.format(backupStartTime) + " *********");
		printToLogAndConsole("Synchronising folders " + syncSource + " and " + syncDest + "");
		printToLogAndConsole("");

		execute(options);

		Calendar end = Calendar.getInstance();
		printToLogAndConsole("********* End: " + TimeHelper.format(end) + " ( "
				+ TimeHelper.getElapsedTime(backupStartTime, end) + " )  *********");
		printToLogAndConsole("");

		writeLog();

		return result;

	}

	private void execute(List<BackupOptions> backupActions) throws IOException {

		boolean doCopy = backupActions.contains(BackupOptions.COPY);

		boolean doUpdate = backupActions.contains(BackupOptions.UPDATE);

		boolean doArchive = backupActions.contains(BackupOptions.ARCHIVE);

		boolean doDelete = backupActions.contains(BackupOptions.DELETE);

		Set<String> sourceFiles = new HashSet<String>();
		Set<String> destFiles = new HashSet<String>();

		try (Stream<Path> sourcePaths = Files.walk(Paths.get(syncSource));
				Stream<Path> destPaths = Files.walk(Paths.get(syncDest))) {

			answerReader = new BufferedReader(new InputStreamReader(System.in));

			LOGGER.info("Evaluating source file tree...");

			sourceFiles = sourcePaths.map(p -> new File(p.toString()))
					.filter(f -> !f.getAbsolutePath().equals(syncSource))
					.map(f -> f.getAbsolutePath().replace(syncSource, "")).collect(Collectors.toSet());

			LOGGER.info("Source file tree: " + sourceFiles);

			LOGGER.info("Evaluating destination file tree...");

			destFiles = destPaths.map(p -> new File(p.toString())).filter(f -> !f.getAbsolutePath().equals(syncDest))
					.map(f -> f.getAbsolutePath().replace(syncDest, "")).collect(Collectors.toSet());

			LOGGER.info("Dest file tree: " + destFiles);

			if (doCopy) {
				printToLogAndConsole("Calculating files to copy...");
				filesToCopy = new TreeSet<String>(sourceFiles);
				filesToCopy.removeAll(destFiles);
				filesToCopy.remove("");
				LOGGER.info("Files to copy: " + filesToCopy);
				if (getConfirmation("copy", filesToCopy)) {
					// copy files existing in source folder but missing in destination folder
					copyNewFiles();
				}
			}

			if (doUpdate) {
				printToLogAndConsole("Calculating common files to update...");
				commonFiles = new TreeSet<String>(sourceFiles);
				commonFiles.retainAll(destFiles);
				LOGGER.info("Common files: " + commonFiles);

				if (getConfirmation("update", commonFiles)) {
					// update files existing in destination folder but recently modified in source
					// folder
					updateModifiedFiles();
				}
			}

			if (doArchive || doDelete) {
				printToLogAndConsole("Calculating obsolete backup files to archive and remove...");
				filesToRemove = new TreeSet<String>(destFiles);
				filesToRemove.removeAll(sourceFiles);
				filesToRemove.removeIf(s -> s.contains(ARCHIVE_FOLDER));
				LOGGER.info("Files to archive and delete: " + filesToRemove);
			}

			// archive files existing in destination folder but removed in source folder
			if (doArchive) {
				if (getConfirmation("archive", filesToRemove)) {
					archiveObsoleteFiles();
				}
			}

			if (doDelete) {
				if (getConfirmation("delete", filesToRemove)) {
					removeObsoleteFiles();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			answerReader.close();
		}
	}

	private void copyNewFiles() throws IOException {

		int countFiles = 0;
		int countFolders = 0;
		if (filesToCopy.isEmpty()) {

			printToLogAndConsole("Nothing to copy");

		} else {

			printToLogAndConsole("Starting to copy...");

			for (String fileToCopy : filesToCopy) {
				try {
					File source = new File(syncSource + fileToCopy);
					if (source.isDirectory()) {
						printToLogAndConsole("Creating folder " + fileToCopy);

						Files.copy(Paths.get(syncSource + fileToCopy), Paths.get(syncDest + fileToCopy),
								StandardCopyOption.REPLACE_EXISTING);
						countFolders++;
					} else {
						printToLogAndConsole("Saving " + fileToCopy);

						Files.copy(Paths.get(syncSource + fileToCopy), Paths.get(syncDest + fileToCopy),
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

				printToLogAndConsole("No file updated");

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

			printToLogAndConsole("Nothing to archive");

		} else {

			// zipping and deleting obsolete files
			printToLogAndConsole("Starting to archive files...");

			// creating archive folder
			File archiveFile = new File(syncDest + ARCHIVE_FOLDER);
			archiveFile.mkdirs();

			// opening streams
			FileOutputStream fos = new FileOutputStream(
					syncDest + ARCHIVE_FOLDER + "/ObsoleteFiles_" + TimeHelper.format(backupStartTime) + ".zip");
			ZipOutputStream zipOut = new ZipOutputStream(fos);

			for (String fileToRemoveName : filesToRemove) {
				try {
					File fileToRemove = new File(syncDest + fileToRemoveName);
					if (!fileToRemove.isDirectory()) {
						printToLogAndConsole("Archiving " + syncDest + fileToRemoveName);

						// adding file to archive zip
						ZipEntry zipEntry = new ZipEntry(
								TimeHelper.format(backupStartTime) + "/" + fileToRemove.getName());
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

	private boolean getConfirmation(String action, TreeSet<String> files) throws IOException {

		System.out.println(filesToCopy.size() + " files to " + action + ", do you want to proceed?");
		System.out.println("y=yes, n=no, s=show files");

		String line = answerReader.readLine();

		switch (line) {
		case "y":
			return true;
		case "s":
			return showFiles(action, files);
		default:
			System.out.println(action + " aborted");
			return false;
		}
	}

	private boolean showFiles(String action, TreeSet<String> files) throws IOException {

		for (String file : files) {
			System.out.println(file);
		}

		System.out.println("Do you want to proceed?");
		System.out.println("y=yes, n=no");

		String line = answerReader.readLine();

		switch (line) {
		case "y":
			return true;
		default:
			System.out.println(action + " aborted");
			return false;
		}

	}

	private void printToLogAndConsole(String line) {

		System.out.println(line);

		log.add(line);
	}

	private void writeLog() {

		File logs = new File(syncDest + LOG_FOLDER);
		logs.mkdirs();
		String logPath = syncDest + LOG_FOLDER + "/backupResult_" + TimeHelper.format(backupStartTime) + ".txt";
		FileHelper.writeLinesInFile(logPath, log);

	}

}
