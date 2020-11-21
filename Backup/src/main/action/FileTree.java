package main.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileTree {

	private String absolutePath;

	private List<String> fileNames = new ArrayList<String>();

	private List<String> subfolderNames = new ArrayList<String>();

	private List<FileTree> subtrees = new ArrayList<FileTree>();

	private long weight = 0;

	public FileTree() {

	}

	public FileTree(String absolutePath) throws IOException {

		this.absolutePath = absolutePath;

		File[] fileList = new File(absolutePath).listFiles();

		for (File file : fileList) {
			if (file.isDirectory()) {
				if (!file.getAbsolutePath().equals(absolutePath)) {
					FileTree subfolder = new FileTree(file.getAbsolutePath());
					subtrees.add(subfolder);
					subfolderNames.add(subfolder.getAbsolutePath().replace(absolutePath, ""));
					weight += subfolder.getWeight();
				}
			} else {
				if (!file.isHidden()) {
					fileNames.add(file.getName());
					weight += file.length();
				}
			}
		}
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public List<String> getFileNames() {
		return fileNames;
	}

	public List<String> getSubfolderNames() {
		return subfolderNames;
	}

	public List<FileTree> getSubtrees() {
		return subtrees;
	}

	public long getWeight() {
		return weight;
	}

	@Override
	public String toString() {
		return "FileTree [absolutePath=" + absolutePath + ", weight=" + weight + ", fileNames=" + fileNames
				+ ", subfolders=" + subfolderNames + "]";
	}

	public boolean hasSameStructure(FileTree otherTree) {
		if (this == otherTree)
			return true;
		if (otherTree == null)
			return false;
		if (fileNames == null) {
			if (otherTree.fileNames != null)
				return false;
		} else if (!fileNames.equals(otherTree.fileNames))
			return false;
		if (subtrees == null) {
			if (otherTree.subtrees != null)
				return false;
		} else if (!subtrees.equals(otherTree.subtrees))
			return false;
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileTree other = (FileTree) obj;
		if (fileNames == null) {
			if (other.fileNames != null)
				return false;
		} else if (!fileNames.equals(other.fileNames))
			return false;
		if (subtrees == null) {
			if (other.subtrees != null)
				return false;
		} else if (!subtrees.equals(other.subtrees))
			return false;
		return true;
	}

}
