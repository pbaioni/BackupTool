package main.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

	public static List<String> getFileAsLines(String filePath) {
		List<String> lines = new ArrayList<String>();
		try {
			BufferedReader br = Files.newBufferedReader(Paths.get(filePath));
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return lines;
	}

	public static String getFileAsString(String filePath) {

		List<String> lines = getFileAsLines(filePath);
		String fileContent = "";

		for (String line : lines) {
			fileContent += line;
		}

		return fileContent;
	}
	
	public static byte[] getFileContentAsBytes(String filePath) {

		String stringFileContent = getFileAsString(filePath);;

		return stringFileContent.getBytes();
	}
	
	public static void writeLinesInFile(String filePath, List<String> lines) {
		try {
			BufferedWriter bw = Files.newBufferedWriter(Paths.get(filePath));
			for(String line : lines) {
				bw.write(line+"\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
