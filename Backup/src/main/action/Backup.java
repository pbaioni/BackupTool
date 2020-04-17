package main.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import main.BackupResult;
import main.helper.FileHelper;

public class Backup {

	private String syncSource;

	private String syncDest;

	private static final String ARCHIVE = "/BackupArchive";
	
	private static final String LOG_FOLDER = "/Results";
	
	private String backupTime;

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
		calculateBackupAction();
	}

	private void calculateBackupAction() {

		List<String> sourceFiles = new ArrayList<String>();
		List<String> destFiles = new ArrayList<String>();

		try (Stream<Path> sourcePaths = Files.walk(Paths.get(syncSource));
				Stream<Path> destPaths = Files.walk(Paths.get(syncDest))) {

			sourceFiles = sourcePaths.map(p -> new File(p.toString())).filter(f -> !f.isHidden())
					.map(f -> f.getAbsolutePath().replace(syncSource, "")).collect(Collectors.toList());

			destFiles = destPaths.map(p -> new File(p.toString())).filter(f -> !f.isHidden())
					.map(f -> f.getAbsolutePath().replace(syncDest, "")).collect(Collectors.toList());

			filesToBackup = new ArrayList<String>(sourceFiles);
			filesToBackup.removeAll(destFiles);

			commonFiles = new ArrayList<String>(sourceFiles);
			commonFiles.retainAll(destFiles);

			filesToRemove = new ArrayList<String>(destFiles);
			filesToRemove.removeAll(sourceFiles);
			filesToRemove.removeIf(s -> s.contains(ARCHIVE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public BackupResult synchronizeFolders() throws IOException {

		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		backupTime = sdf.format(now.getTime());
		
		calculateBackupAction();

		printToLogAndConsole("********* " + backupTime + " *********");
		printToLogAndConsole("Synchronising folders " + syncSource + " and " + syncDest + "");
		printToLogAndConsole("");

		// search for folders simply renamed (no file added or deleted) and rename them
		renameFolders();

		// copy files existing in source folder but missing in destination folder
		backupNewFiles();

		// update files existing in destination folder but recently modified in source
		// folder
		updateModifiedFiles();

		// archive files existing in destination folder but removed in source folder
		removeAndArchiveObsoleteFiles();

		printToLogAndConsole("Done");
		
		writeLog();

		return result;

	}

	private void renameFolders() throws IOException {

		printToLogAndConsole("Searching for folders to rename...");

		int renamed = 0;
		while (renameOneFolder()) {
			renamed++;
		}
		if (renamed == 0) {
			printToLogAndConsole("No folder to rename");
		} else {
			printToLogAndConsole(renamed + " folders renamed");
		}
		
		printToLogAndConsole("");

		result.setRenamedFolders(renamed);

	}

	/**
	 * Searches a match for renamed but corresponding folders between source and
	 * dest, renames the destination folder and recalculates the file trees
	 **/
	private boolean renameOneFolder() throws IOException {

		boolean rval = false;

		List<FileTree> newTrees = createFileTrees(syncSource, filesToBackup);

		List<FileTree> oldTrees = createFileTrees(syncDest, filesToRemove);

		for (FileTree tree : newTrees) {
			if (!rval) {
				for (FileTree oldTree : oldTrees) {
					if (tree.equals(oldTree)) {
						String oldFolderName = oldTree.getAbsolutePath();
						String newFolderName = syncDest + tree.getAbsolutePath().replace(syncSource, "");
						File newFolderFile = new File(newFolderName);
						newFolderFile.mkdirs();
						Files.move(Paths.get(oldFolderName), Paths.get(newFolderName),
								StandardCopyOption.REPLACE_EXISTING);
						printToLogAndConsole("Folder " + oldFolderName + " renamed to " + newFolderName);
						calculateBackupAction();
						rval = true;
						break;
					}
				}
			}
		}

		return rval;

	}

	private List<FileTree> createFileTrees(String rootFolder, List<String> relativePaths) throws IOException {

		List<FileTree> trees = new ArrayList<FileTree>();
		relativePaths.sort(Comparator.comparingInt(String::length));

		List<String> folders = relativePaths.stream().map(s -> new File(rootFolder + s)).filter(f -> f.isDirectory())
				.map(f -> f.getAbsolutePath()).collect(Collectors.toList());

		for (String folder : folders) {

			FileTree tree = new FileTree(folder);

			trees.add(tree);

		}

		return trees;
	}

	private void backupNewFiles() throws IOException {

		printToLogAndConsole("Searching for new files to backup...");

		int countFiles = 0;
		int countFolders = 0;
		for (String fileToBackup : filesToBackup) {
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
		}

		if (countFiles == 0 && countFolders == 0) {
			printToLogAndConsole("Nothing to backup");
		} else {
			printToLogAndConsole(countFiles + " files and " + countFolders + " folders saved");
		}
		
		printToLogAndConsole("");

		result.setCopiedFiles(countFiles);
		result.setCopiedFolders(countFolders);

	}

	private void updateModifiedFiles() throws IOException {

		printToLogAndConsole("Searching for updated files...");

		int countFiles = 0;
		int countFolders = 0;

		for (String fileToUpdate : commonFiles) {

			File source = new File(syncSource + fileToUpdate);
			File dest = new File(syncDest + fileToUpdate);
			boolean updated = source.lastModified() > dest.lastModified();

			if (updated) {
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
			}

		}

		if (countFiles == 0 && countFolders == 0) {
			printToLogAndConsole("No update found");
		} else {
			printToLogAndConsole(countFiles + " files and " + countFolders + " folders updated");
		}
		
		printToLogAndConsole("");

		result.setUpdatedFiles(countFiles);
		result.setUpdatedFolders(countFolders);

	}

	private void removeAndArchiveObsoleteFiles() throws IOException {

		printToLogAndConsole("Searching for obsolete backup files to archive and remove...");

		List<String> foldersToRemove = new ArrayList<String>();

		int countFiles = 0;
		int countFolders = 0;

		// zipping and deleting obsolete files
		if (!filesToRemove.isEmpty()) {

			// creating archive folder
			File archive = new File(syncDest + ARCHIVE);
			archive.mkdirs();

			// zipping file for archive
			FileOutputStream fos = new FileOutputStream(syncDest + ARCHIVE + "/ObsoleteFiles_" + backupTime + ".zip");
			ZipOutputStream zipOut = new ZipOutputStream(fos);

			for (String fileToRemoveName : filesToRemove) {

				File fileToRemove = new File(syncDest + fileToRemoveName);

				if (!fileToRemove.isDirectory()) {
					printToLogAndConsole("Removing and archiving " + syncDest + fileToRemoveName);

					ZipEntry zipEntry = new ZipEntry(backupTime + "/" + fileToRemove.getName());
					zipOut.putNextEntry(zipEntry);
					zipOut.write(FileHelper.getFileContentAsBytes(fileToRemove.getAbsolutePath()));
					zipOut.closeEntry();

					// deleting file
					fileToRemove.delete();

					countFiles++;
				} else {
					foldersToRemove.add(fileToRemoveName);
					countFolders++;
				}

			}

			zipOut.close();
			fos.close();

			// deleting obsolete folders
			for (String folderToRemove : foldersToRemove) {

				File dest = new File(syncDest + folderToRemove);
				printToLogAndConsole("Removing folder " + syncDest + folderToRemove);
				dest.delete();

			}

		}

		if (countFiles == 0 && countFolders == 0) {
			printToLogAndConsole("Nothing to remove");
		} else {
			printToLogAndConsole(countFiles + " files and " + countFolders + " folders removed");
		}
		
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
		String logPath = syncDest + ARCHIVE + LOG_FOLDER + "/backupResult_"+ backupTime + ".txt";
		FileHelper.writeLinesInFile(logPath, log);
		
	}

}
