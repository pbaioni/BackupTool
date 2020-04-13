package action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Backup {

	private String syncSource;

	private String syncDest;

	public Backup() {

	}

	public Backup(String syncSource, String syncDest) {
		this.syncSource = syncSource;
		this.syncDest = syncDest;
	}

	public void synchronizeFolders() {

		List<String> sourceFiles = new ArrayList<String>();
		List<String> destFiles = new ArrayList<String>();
		File destFolder = new File(syncDest);
		destFolder.mkdirs();

		try (Stream<Path> sourcePaths = Files.walk(Paths.get(syncSource));
				Stream<Path> destPaths = Files.walk(Paths.get(syncDest))) {

			sourceFiles = sourcePaths.map(p -> new File(p.toString())).filter(f -> !f.isHidden())
					.map(f -> f.getAbsolutePath().replace(syncSource, "")).collect(Collectors.toList());

			destFiles = destPaths.map(p -> new File(p.toString())).filter(f -> !f.isHidden())
					.map(f -> f.getAbsolutePath().replace(syncDest, "")).collect(Collectors.toList());

			List<String> filesToBackup = new ArrayList<String>(sourceFiles);
			filesToBackup.removeAll(destFiles);

			List<String> filesToUpdate = new ArrayList<String>(sourceFiles);
			filesToUpdate.retainAll(destFiles);

			List<String> filesToRemove = new ArrayList<String>(destFiles);
			filesToRemove.removeAll(sourceFiles);

			//sourceFiles.forEach(System.out::println);
			//System.out.println();
			//destFiles.forEach(System.out::println);

			backupFiles(filesToBackup);

			updateModifiedFiles(filesToUpdate);

			removeOldFiles(filesToRemove);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void backupFiles(List<String> filesToBackup) throws IOException {

		System.out.println("Evaluating backup for new files...");

		int count = 0;
		for (String fileToBackup : filesToBackup) {
			System.out.println("Saving " + fileToBackup);
			Files.copy(Paths.get(syncSource + fileToBackup), Paths.get(syncDest + fileToBackup),
					StandardCopyOption.REPLACE_EXISTING);
			count++;
		}

		if (count == 0) {
			System.out.println("No file to backup\n");
		} else {
			System.out.println(count + " files saved\n");
		}

	}

	private void updateModifiedFiles(List<String> filesToUpdate) throws IOException {

		System.out.println("Searching files to update...");

		int count = 0;

		for (String fileToUpdate : filesToUpdate) {

			File source = new File(syncSource + fileToUpdate);
			File dest = new File(syncDest + fileToUpdate);
			boolean lastModificationChanged = source.lastModified() > dest.lastModified();

			if (lastModificationChanged) {
				if (!dest.isDirectory()) {
					System.out.println("Updating " + syncDest + fileToUpdate);
					Files.copy(Paths.get(syncSource + fileToUpdate), Paths.get(syncDest + fileToUpdate),
							StandardCopyOption.REPLACE_EXISTING);
					count++;
				} else {
					System.out.println("Updating modification date for folder " + syncDest + fileToUpdate);
					dest.setLastModified(source.lastModified());
				}
			}

		}

		if (count == 0) {
			System.out.println("No file to update\n");
		} else {
			System.out.println(count + " files updated\n");
		}

	}

	private void removeOldFiles(List<String> filesToRemove) throws IOException {

		System.out.println("Searching old backup files to remove...");

		List<String> foldersToRemove = new ArrayList<String>();

		int count = 0;

		for (String fileToRemove : filesToRemove) {

			File dest = new File(syncDest + fileToRemove);

			if (!dest.isDirectory()) {
				System.out.println("Removing " + syncDest + fileToRemove);
				dest.delete();
				count++;
			} else {
				foldersToRemove.add(fileToRemove);
			}

		}

		for (String folderToRemove : foldersToRemove) {

			File dest = new File(syncDest + folderToRemove);
			System.out.println("Removing folder " + syncDest + folderToRemove);
			dest.delete();

		}

		if (count == 0) {
			System.out.println("No file to remove\n");
		} else {
			System.out.println(count + " files removed\n");
		}

	}

}
