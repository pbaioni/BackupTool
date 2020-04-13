package main;

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

public class Main {

	private static String rootFolderPath = "/Users/paolobaioni/Documents/Music";

	private static String syncSource = "/Users/paolobaioni/TestSyncSource";

	private static String syncDest = "/Users/paolobaioni/TestSyncDest";

	public static void main(String[] args) throws IOException {

		String[] extensions = { "m3u" };

		for (String extension : extensions) {
			// copyFilesByExtension(extension);
		}

		synchronizeFolders();

	}

	private static void copyFilesByExtension(String extension) throws IOException {

		System.out.println("Starting scan for " + extension + " files in all " + rootFolderPath + " subdirectories...");

		String copyFolderPath = rootFolderPath + "/Copy/" + extension;
		List<String> files = new ArrayList<String>();

		try (Stream<Path> walk = Files.walk(Paths.get(rootFolderPath))) {

			files = walk.map(f -> f.toString()).filter(f -> f.endsWith("." + extension) && !f.contains(copyFolderPath))
					.collect(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(files.size() + " files found");

		System.out.println("Copying all files in " + copyFolderPath);

		File copyFolder = new File(copyFolderPath);

		copyFolder.mkdirs();

		for (String fileToCopy : files) {
			File source = new File(fileToCopy);
			File dest = new File(copyFolder.getPath() + "/" + source.getName());
			Files.copy(Paths.get(fileToCopy), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		System.out.println("Done copying " + extension + " files");

	}

	private static void synchronizeFolders() {

		List<String> sourceFiles = new ArrayList<String>();
		List<String> destFiles = new ArrayList<String>();
		File destFolder = new File(syncDest);
		destFolder.mkdirs();

		try (Stream<Path> sourcePaths = Files.walk(Paths.get(syncSource));
				Stream<Path> destPaths = Files.walk(Paths.get(syncDest))) {

			sourceFiles = sourcePaths.map(f -> f.toString().replace(syncSource, ""))
					.filter(f -> !f.equals("") && !f.contains("/.")).collect(Collectors.toList());

			destFiles = destPaths.map(f -> f.toString().replace(syncDest, ""))
					.filter(f -> !f.equals("") && !f.contains("/.")).collect(Collectors.toList());

			copyMissingFiles(sourceFiles, destFiles);

			updateModifiedFiles(sourceFiles, destFiles);


		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void copyMissingFiles(List<String> sourceFiles, List<String> destFiles) throws IOException {

		System.out.println("Searching files to backup...");

		int count = 0;
		for (String sourceFile : sourceFiles) {
			if (!destFiles.contains(sourceFile)) {
				System.out.println("Saving " + sourceFile);
				Files.copy(Paths.get(syncSource + sourceFile), Paths.get(syncDest + sourceFile),
						StandardCopyOption.REPLACE_EXISTING);
				count++;
			}
		}

		System.out.println(count + " files saved");

	}

	private static void updateModifiedFiles(List<String> sourceFiles, List<String> destFiles) throws IOException {

		System.out.println("Searching files to update...");

		int count = 0;
		List<String> commonFiles = new ArrayList<String>(sourceFiles);
		commonFiles.retainAll(destFiles);

		for (String commonFile : commonFiles) {

			File source = new File(syncSource + commonFile);
			File dest = new File(syncDest + commonFile);
			boolean lastModificationChanged = source.lastModified() > dest.lastModified();

			if (lastModificationChanged) {
				if (!dest.isDirectory()) {
					System.out.println("Updating " + commonFile);
					Files.copy(Paths.get(syncSource + commonFile), Paths.get(syncDest + commonFile),
							StandardCopyOption.REPLACE_EXISTING);
					count++;
				} else{
					System.out.println("Updating modification date for folder " + commonFile);
					dest.setLastModified(source.lastModified());
				}
			}

		}

		System.out.println(count + " files updated");

	}

}
