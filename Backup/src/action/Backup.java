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

import main.BackupResult;

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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BackupResult synchronizeFolders() throws IOException {
		
		BackupResult result = new BackupResult();

		calculateBackupAction();
		
		System.out.println("************************");
		renameFolders(result);
		
		backupNewFiles(result);

		updateModifiedFiles(result);

		removeObsoleteFiles(result);
		
		return result;

	}

	private void renameFolders(BackupResult result) throws IOException {
		
		System.out.println("Searching for folders to rename...");
		
		int renamed = 0;
		while (renameOneFolder()) {
			renamed++;
		}
		if (renamed == 0) {
			System.out.println("No folder to rename\n");
		} else {
			System.out.println(renamed + " folders renamed\n");
		}
		
		result.setRenamedFolders(renamed);

	}
	
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
						Files.move(Paths.get(oldFolderName), Paths.get(newFolderName), StandardCopyOption.REPLACE_EXISTING);
						System.out.println("Folder " + oldFolderName + " renamed to " + newFolderName);
						calculateBackupAction();
						rval=true;
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

	private void backupNewFiles(BackupResult result) throws IOException {

		System.out.println("Searching for new files to backup...");

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
			System.out.println("Nothing to backup\n");
		} else {
			System.out.println(countFiles + " files and " + countFolders + " folders saved\n");
		}
		
		result.setCopiedFiles(countFiles);
		result.setCopiedFolders(countFolders);

	}

	private void updateModifiedFiles(BackupResult result) throws IOException {

		System.out.println("Searching for updated files...");

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
			System.out.println("No update found\n");
		} else {
			System.out.println(countFiles + " files and " + countFolders + " folders updated\n");
		}
		
		result.setUpdatedFiles(countFiles);
		result.setUpdatedFolders(countFolders);

	}

	private void removeObsoleteFiles(BackupResult result) throws IOException {

		System.out.println("Searching for obsolete backup files to remove...");

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
			System.out.println("Nothing to remove\n");
		} else {
			System.out.println(countFiles + " files and " + countFolders + " folders removed\n");
		}
		
		result.setRemovedFiles(countFiles);
		result.setRemovedFolders(countFolders);

	}

}
