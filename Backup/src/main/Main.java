package main;

import java.io.IOException;

import action.Copy;
import test.Test;

public class Main {

	//Copy properties
	private static String rootFolderPath = "/Users/paolobaioni/Documents/Music";
	
	private static String[] extensionsToCopy = { };

	//Backup properties
	private static String syncSource = "/Users/paolobaioni/TestSource";
	//private static String syncSource = "/Users/paolobaioni/Documents/Pictures";

	private static String syncDest = "/Users/paolobaioni/TestDest";
	//private static String syncDest = "/Volumes/Paolo_backup/Documents/Pictures";
	

	public static void main(String[] args) throws IOException {
		
		Copy copy = new Copy(rootFolderPath);
		for(String extension : extensionsToCopy) {
			copy.copyFilesByExtension(extension);
		}
		
		Test test = new Test(syncSource, syncDest);
		test.TestBackup();
		

	}
	




}
