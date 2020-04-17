package test;

import java.io.IOException;

import main.BackupResult;
import main.action.Backup;
import main.helper.TestFileHelper;

public class Test {

	private String source;
	
	private String dest;
	
	String overallResult = "Results:\n";
	
	public Test(String source, String dest) {
		this.source = source;
		this.dest = dest;
	}
	
	public void executeAllTests() throws IOException {
		SimpleRename();
		simpleBackup();
		simpleUpdate();
		
		System.out.println(overallResult);
	}
	

	public void simpleBackup() throws IOException {
		
		overallResult += "- Simple backup\n";
		//creating backup objects
		TestFileHelper fh = new TestFileHelper(source, dest);

		
		//starting test: creating source tree, deleting dest
		fh.createStandardSourceTree();
		fh.clearDest();

		//launching first backup
		Backup backup = new Backup(source, dest);
		int[] firstBackupResults = {0, 9, 5, 0, 0, 0, 0};
		overallResult += "backup: " + backup.synchronizeFolders().check(firstBackupResults) + "\n";
		
	}
	
	public void simpleUpdate() throws IOException {
		
		overallResult += "- Simple update\n";
		//creating backup objects
		TestFileHelper fh = new TestFileHelper(source, dest);

		
		//starting test: creating source tree, deleting dest
		fh.createStandardSourceTree();
		fh.clearDest();

		//launching first backup
		Backup backup = new Backup(source, dest);
		backup.synchronizeFolders();
		
		fh.updateFileOrFolder("G1/T1.txt");
		int[] fileUpdateResults = {0, 0, 0, 1, 0, 0, 0};
		overallResult += "file update: " + backup.synchronizeFolders().check(fileUpdateResults) + "\n";
		backup.clearResults();
		
		fh.updateFileOrFolder("G1/G2");
		int[] folderUpdateResults = {0, 0, 0, 0, 1, 0, 0};
		overallResult += "folder update: " + backup.synchronizeFolders().check(folderUpdateResults) + "\n";
		
	}
	
	public void simpleRemove() throws IOException {
		
		overallResult += "- Simple update\n";
		//creating backup objects
		TestFileHelper fh = new TestFileHelper(source, dest);

		
		//starting test: creating source tree, deleting dest
		fh.createStandardSourceTree();
		fh.clearDest();

		//launching first backup
		Backup backup = new Backup(source, dest);
		backup.synchronizeFolders();
		
		fh.updateFileOrFolder("G1/T1.txt");
		int[] fileUpdateResults = {0, 0, 0, 1, 0, 0, 0};
		overallResult += "file update: " + backup.synchronizeFolders().check(fileUpdateResults) + "\n";
		
	}

	public void SimpleRename() throws IOException {

		overallResult += "- Simple rename\n";
		//creating backup objects
		TestFileHelper fh = new TestFileHelper(source, dest);

		
		//starting test: creating source tree, deleting dest
		fh.createStandardSourceTree();
		fh.clearDest();

		//launching first backup
		Backup backup = new Backup(source, dest);
		backup.synchronizeFolders();
		
		//renaming one file and launching backup
		fh.renameFileOrFolder("D1.dat", "R1.dat");
		int[] renameFileResults = {0, 1, 0, 0, 0, 1, 0};
		overallResult += "Rename one file: " + backup.synchronizeFolders().check(renameFileResults) + "\n";
		
		//renaming N1 folder
		fh.renameFileOrFolder("F1", "RF1");
		int[] renameN1FolderResults = {1, 0, 0, 0, 0, 0, 0};
		overallResult += "Rename one N1 folder: " + backup.synchronizeFolders().check(renameN1FolderResults) + "\n";
		//renaming N2 folder
		fh.renameFileOrFolder("RF1/F2", "RF1/RF2");
		int[] renameN2FolderResults = {1, 0, 0, 0, 0, 0, 0};
		overallResult += "Rename one N2 folder: " + backup.synchronizeFolders().check(renameN2FolderResults) + "\n";
		//renaming N3 folder
		fh.renameFileOrFolder("RF1/RF2/F3", "RF1/RF2/RF3");
		int[] renameN3FolderResults = {1, 0, 0, 0, 0, 0, 0};
		overallResult += "Rename one N3 folder: " + backup.synchronizeFolders().check(renameN3FolderResults) + "\n";
		
		//renaming 2 N1 folders
		fh.renameFileOrFolder("RF1", "F1");
		fh.renameFileOrFolder("G1", "RG1");
		int[] rename2N1FoldersResults = {2, 0, 0, 0, 0, 0, 0};
		overallResult += "Rename two N1 folders: " + backup.synchronizeFolders().check(rename2N1FoldersResults) + "\n";
		
		//renaming 2 N2 folders
		fh.renameFileOrFolder("F1/RF2", "F1/F2");
		fh.renameFileOrFolder("RG1/G2", "RG1/RG2");
		int[] rename2N2FoldersResults = {2, 0, 0, 0, 0, 0, 0};
		overallResult += "Rename two N2 folders: " + backup.synchronizeFolders().check(rename2N2FoldersResults) + "\n";
		

	}

}
