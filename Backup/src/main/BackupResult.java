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
			return "Wrong number of renamed folders";
		}
		if (expectedResults[1] != copiedFiles) {
			return "Wrong number of copied Files";
		}
		if (expectedResults[2] != copiedFolders) {
			return "Wrong number of copied folders";
		}
		if (expectedResults[3] != updatedFiles) {
			return "Wrong number of updated files";
		}
		if (expectedResults[4] != updatedFolders) {
			return "Wrong number of updated folders";
		}
		if (expectedResults[5] != removedFiles) {
			return "Wrong number of removed files";
		}
		if (expectedResults[6] != removedFolders) {
			return "Wrong number of removed folders";
		}
		
		return "OK";
	}

}
