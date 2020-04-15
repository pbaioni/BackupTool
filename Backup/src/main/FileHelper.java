package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

public class FileHelper {
	
	private String source;
	
	private String dest;
	
	private final String FILE_SEPARATOR = "/";
	
	public FileHelper(String source, String dest) {
		this.source = source;
		this.dest = dest;
		File sourceFolder = new File(source);
		sourceFolder.mkdirs();
		File destFolder = new File(dest);
		destFolder.mkdirs();
	}
	
	public void createStandardSourceTree() throws IOException {
		
		clearSource();
		//creating root level files
		createFile("D1.dat");
		createFile("D2.dat");
		
		//creating 3 levels folders and files
		createFolder("F1");
		createFile("F1/T1.txt");
		createFolder("F1/F2");
		createFile("F1/F2/P1.pib");
		createFile("F1/F2/P2.pib");
		createFolder("F1/F2/F3");
		createFile("F1/F2/F3/H1.html");
		
		//creating 2 levels folders and files
		createFolder("G1");
		createFile("G1/T1.txt");
		createFolder("G1/G2");
		createFile("G1/G2/P1.pib");
		createFile("G1/G2/P2.pib");
		
	}
	
	public void createFile(String name) throws IOException {
		Files.createFile(Paths.get(absolute(name)));
	}
	
	public void createFolder(String name) throws IOException {
		Files.createDirectory(Paths.get(source + FILE_SEPARATOR + name));
	}
	
	public void renameFileOrFolder(String oldName, String newName) throws IOException {

		Files.move(Paths.get(absolute(oldName)), Paths.get(absolute(newName)), StandardCopyOption.REPLACE_EXISTING);
	}
	
	public void updateFileOrFolder(String name) throws IOException {

		File update = new File(absolute(name));
		update.setLastModified(update.lastModified()+100);
		
	}
	
	public void removeFileOrFolder(String name) throws IOException {
		Files.delete(Paths.get(absolute(name)));
	}
	
	public void clearSource() throws IOException {
	    Files.walk(Paths.get(source))
	      .sorted(Comparator.reverseOrder())
	      .map(Path::toFile)
	      .forEach(File::delete);
		File sourceFolder = new File(source);
		sourceFolder.mkdirs();
	}
	
	public void clearDest() throws IOException {
	    Files.walk(Paths.get(dest))
	      .sorted(Comparator.reverseOrder())
	      .map(Path::toFile)
	      .forEach(File::delete);
	}
	
	private String absolute(String name) {
		return source + FILE_SEPARATOR + name;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public String getFILE_SEPARATOR() {
		return FILE_SEPARATOR;
	}

}
