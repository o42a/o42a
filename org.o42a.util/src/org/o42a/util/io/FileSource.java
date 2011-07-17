/*
    Utilities
    Copyright (C) 2011 Ruslan Lopatin

    This file is part of o42a.

    o42a is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    o42a is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.o42a.util.io;

import static org.o42a.util.string.StringUtil.removeLeadingChars;

import java.io.*;


public class FileSource extends Source {

	private final File root;
	private final File file;
	private final String name;

	public FileSource(String name, File root, String path) {
		assert root.isDirectory() :
			"Root is not a directory: " + root;
		this.root = root;
		this.file = new File(root, path);
		this.name = name != null ? name : path;
	}

	public FileSource(FileSource parent, String path) {
		this(parent.getRoot(), parent.getFile(), path);
	}

	public FileSource(File root, File relativeTo, String path) {
		assert root.isDirectory() :
			"Root is not a directory: " + root;
		this.root = root;
		this.file = new File(relativeTo, path);
		this.name = name(path);
	}

	public final File getRoot() {
		return this.root;
	}

	public final File getFile() {
		return this.file;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public final boolean isDirectory() {
		return this.file.isDirectory();
	}

	@Override
	public boolean isEmpty() {
		return !this.file.isFile();
	}

	@Override
	public Reader open() throws IOException {
		return new InputStreamReader(new FileInputStream(getFile()), "UTF-8");
	}

	private String name(String path) {

		final String rootPath = this.root.getPath();
		final String filePath = this.file.getPath();

		if (!filePath.startsWith(rootPath)) {
			return path;
		}

		return removeLeadingChars(
				filePath.substring(rootPath.length()),
				File.separatorChar);
	}

}
