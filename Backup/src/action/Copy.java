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

public class Copy {
	
	private String rootFolderPath;
	
	public Copy() {
		
	}
	
	public Copy(String rootFolderPath) {
		this.rootFolderPath = rootFolderPath;
	}
	
	public void copyFilesByExtension(String extension) throws IOException {

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

}
