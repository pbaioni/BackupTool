package main;

public class BackupResult {

	private int renamedFolders;

	private int copiedFiles;

	private int copiedFolders;

	private int updatedFiles;

	private int updatedFolders;

	private int removedFiles;

	private int removedFolders;

	public BackupResult() {

	}

	public int getRenamedFolders() {
		return renamedFolders;
	}

	public void setRenamedFolders(int renamedFolders) {
		this.renamedFolders = renamedFolders;
	}

	public int getCopiedFiles() {
		return copiedFiles;
	}

	public void setCopiedFiles(int copiedFiles) {
		this.copiedFiles = copiedFiles;
	}

	public int getCopiedFolders() {
		return copiedFolders;
	}

	public void setCopiedFolders(int copiedFolders) {
		this.copiedFolders = copiedFolders;
	}

	public int getUpdatedFiles() {
		return updatedFiles;
	}

	public void setUpdatedFiles(int updatedFiles) {
		this.updatedFiles = updatedFiles;
	}

	public int getUpdatedFolders() {
		return updatedFolders;
	}

	public void setUpdatedFolders(int updatedFolders) {
		this.updatedFolders = updatedFolders;
	}

	public int getRemovedFiles() {
		return removedFiles;
	}

	public void setRemovedFiles(int removedFiles) {
		this.removedFiles = removedFiles;
	}

	public int getRemovedFolders() {
		return removedFolders;
	}

	public void setRemovedFolders(int removedFolders) {
		this.removedFolders = removedFolders;
	}

	public String check(int[] expectedResults) {
		
		if (expectedResults[0] != renamedFolders) {
			return "Wrong number of renamed folders. Expected " + expectedResults[0] + " but was " + renamedFolders;
		}
		if (expectedResults[1] != copiedFiles) {
			return "Wrong number of copied files. Expected " + expectedResults[1] + " but was " + copiedFiles;
		}
		if (expectedResults[2] != copiedFolders) {
			return "Wrong number of copied folders. Expected " + expectedResults[2] + " but was " + copiedFolders;
		}
		if (expectedResults[3] != updatedFiles) {
			return "Wrong number of updated files. Expected " + expectedResults[3] + " but was " + updatedFiles;
		}
		if (expectedResults[4] != updatedFolders) {
			return "Wrong number of updated folders. Expected " + expectedResults[4] + " but was " + updatedFolders;
		}
		if (expectedResults[5] != removedFiles) {
			return "Wrong number of removed files. Expected " + expectedResults[5] + " but was " + removedFiles;
		}
		if (expectedResults[6] != removedFolders) {
			return "Wrong number of removed folders. Expected " + expectedResults[6] + " but was " + removedFolders;
		}
		
		return "OK";
	}

	public void clear() {
		renamedFolders = 0;

		copiedFiles = 0;

		copiedFolders = 0;

		updatedFiles = 0;

		updatedFolders = 0;

		removedFiles = 0;

		removedFolders = 0;
	}

}
