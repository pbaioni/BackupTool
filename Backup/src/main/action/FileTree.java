package main.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileTree {

	private String absolutePath;

	private List<String> fileNames = new ArrayList<String>();

	private List<FileTree> subfolders = new ArrayList<FileTree>();

	public FileTree() {

	}

	public FileTree(String absolutePath) throws IOException {

		this.absolutePath = absolutePath;

		List<File> fileTree = Files.walk(Paths.get(absolutePath)).map(p -> new File(p.toString()))
				.collect(Collectors.toList());

		for (File file : fileTree) {
			if (file.isDirectory()) {
				if (!file.getAbsolutePath().equals(absolutePath)) {
					subfolders.add(new FileTree(file.getAbsolutePath()));
				}
			} else {
				if (!file.isHidden()) {
					fileNames.add(file.getName());
				}
			}
		}
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public List<String> getFileNames() {
		return fileNames;
	}

	public void setFileNames(List<String> fileNames) {
		this.fileNames = fileNames;
	}

	public List<FileTree> getSubfolders() {
		return subfolders;
	}

	public void setSubfolders(List<FileTree> subfolders) {
		this.subfolders = subfolders;
	}



	@Override
	public String toString() {
		return "FileTree [absolutePath=" + absolutePath + ", fileNames=" + fileNames + ", subfolders=" + subfolders
				+ "]";
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
		if (subfolders == null) {
			if (other.subfolders != null)
				return false;
		}
		return true && equalSubfolders(other);
	}

	private boolean equalSubfolders(FileTree other) {

		int matches = 0;

		for (FileTree tree : subfolders) {
			for (FileTree otherTree : other.getSubfolders()) {
				if (tree.equals(otherTree)) {
					matches++;
				}
			}
		}
		return matches == other.getSubfolders().size();
	}

}
