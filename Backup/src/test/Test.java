package test;

import java.io.IOException;

import action.Backup;
import main.BackupResult;
import main.FileHelper;

public class Test {

	private String source;
	
	private String dest;
	
	public Test(String source, String dest) {
		this.source = source;
		this.dest = dest;
	}
	

	public void TestBackup() throws IOException {

		String overallResult = "";
		//creating backup objects
		FileHelper fh = new FileHelper(source, dest);

		
		//starting test: creating source tree, deleting dest
		fh.createStandardSourceTree();
		fh.clearDest();

		//launching first backup
		Backup backup = new Backup(source, dest);
		int[] firstBackupResults = {0, 9, 5, 0, 0, 0, 0};
		overallResult += "First backup: " + backup.synchronizeFolders().check(firstBackupResults) + "\n";
		
		//renaming one file and launching backup
		fh.renameFileOrFolder("D1.dat", "R1.dat");
		int[] renameFoldersResults = {0, 1, 0, 0, 0, 1, 0};
		overallResult += "Rename one file: " + backup.synchronizeFolders().check(renameFoldersResults) + "\n";
		
		System.out.println(overallResult);

	}

}
