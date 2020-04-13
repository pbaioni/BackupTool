package action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Backup {

	private String syncSource;

	private String syncDest;

	private List<String> filesToBackup;

	private List<String> commonFiles;

	private List<String> filesToRemove;

	public Backup() {

	}

	public Backup(String syncSource, String syncDest) {
		this.syncSource = syncSource;
		this.syncDest = syncDest;
		evaluateBackupAction();
	}

	private void evaluateBackupAction() {

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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void synchronizeFolders() throws IOException {

		File destFolder = new File(syncDest);
		destFolder.mkdirs();

		System.out.println("Searching for renamed folders...");
		
		while (renameFolders(filesToBackup, filesToRemove) > 0) {

		}
		
		System.out.println();
		
		backupNewFiles(filesToBackup);

		updateModifiedFiles(commonFiles);

		removeOldFiles(filesToRemove);

	}

	private int renameFolders(List<String> filesToBackup, List<String> filesToRemove) throws IOException {
		
		int renamed = 0;

		List<FileTree> newTrees = createFileTrees(syncSource, filesToBackup);

		List<FileTree> oldTrees = createFileTrees(syncDest, filesToRemove);

		for (FileTree tree : newTrees) {
			if (renamed == 0) {
				for (FileTree oldTree : oldTrees) {
					if (tree.equals(oldTree)) {
						String oldFolderName = oldTree.getAbsolutePath();
						String newFolderName = syncDest + tree.getAbsolutePath().replace(syncSource, "");
						Files.move(Paths.get(oldFolderName), Paths.get(newFolderName), StandardCopyOption.REPLACE_EXISTING);
						System.out.println("Folder " + oldFolderName + " renamed to " + newFolderName);
						evaluateBackupAction();
						renamed++;
						break;
					}
				}
			}
		}

		return renamed;

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

	private void backupNewFiles(List<String> filesToBackup) throws IOException {

		System.out.println("Evaluating backup for new files...");

		int countFiles = 0;
		int countFolders = 0;
		for (String fileToBackup : filesToBackup) {
			File source = new File(syncSource + fileToBackup);
			if (source.isDirectory()) {
				System.out.println("Creating folder " + fileToBackup);
				Files.copy(Paths.get(syncSource + fileToBackup), Paths.get(syncDest + fileToBackup),
						StandardCopyOption.REPLACE_EXISTING);
				countFolders++;
			} else {
				System.out.println("Saving " + fileToBackup);
				Files.copy(Paths.get(syncSource + fileToBackup), Paths.get(syncDest + fileToBackup),
						StandardCopyOption.REPLACE_EXISTING);
				countFiles++;
			}
		}

		if (countFiles == 0 && countFolders == 0) {
			System.out.println("No file to backup\n");
		} else {
			System.out.println(countFiles + " files and " + countFolders + " folders saved\n");
		}

	}

	private void updateModifiedFiles(List<String> commonFiles) throws IOException {

		System.out.println("Searching files to update...");

		int countFiles = 0;
		int countFolders = 0;

		for (String fileToUpdate : commonFiles) {

			File source = new File(syncSource + fileToUpdate);
			File dest = new File(syncDest + fileToUpdate);
			boolean updated = source.lastModified() > dest.lastModified();

			if (updated) {
				if (!dest.isDirectory()) {
					System.out.println("Updating " + syncDest + fileToUpdate);
					Files.copy(Paths.get(syncSource + fileToUpdate), Paths.get(syncDest + fileToUpdate),
							StandardCopyOption.REPLACE_EXISTING);
					countFiles++;
				} else {
					System.out.println("Updating modification date for folder " + syncDest + fileToUpdate);
					dest.setLastModified(source.lastModified());
					countFolders++;
				}
			}

		}

		if (countFiles == 0 && countFolders == 0) {
			System.out.println("No file to update\n");
		} else {
			System.out.println(countFiles + " files and " + countFolders + " folders updated\n");
		}

	}

	private void removeOldFiles(List<String> filesToRemove) throws IOException {

		System.out.println("Searching old backup files to remove...");

		List<String> foldersToRemove = new ArrayList<String>();

		int countFiles = 0;
		int countFolders = 0;

		for (String fileToRemove : filesToRemove) {

			File dest = new File(syncDest + fileToRemove);

			if (!dest.isDirectory()) {
				System.out.println("Removing " + syncDest + fileToRemove);
				dest.delete();
				countFiles++;
			} else {
				foldersToRemove.add(fileToRemove);
				countFolders++;
			}

		}

		for (String folderToRemove : foldersToRemove) {

			File dest = new File(syncDest + folderToRemove);
			System.out.println("Removing folder " + syncDest + folderToRemove);
			dest.delete();

		}

		if (countFiles == 0 && countFolders == 0) {
			System.out.println("No file to remove\n");
		} else {
			System.out.println(countFiles + " files and " + countFolders + " folders removed\n");
		}

	}

}
