/*
    Utilities
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;


public class FileSource extends Source {

	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private final File root;
	private final File file;
	private final String name;
	private Charset charset;

	public FileSource(File root, String path) {
		this((String) null, root, path);
	}

	public FileSource(String name, File root, String path) {
		assert root.isDirectory() :
			"Root is not a directory: " + root;
		this.root = root;
		this.file = new File(root, path);
		this.name = name != null ? name : path;
	}

	public FileSource(FileSource parent, String path) {
		this(parent.getRoot(), parent.getFile(), path);
		setCharset(parent.getCharset());
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

	public final Charset getCharset() {
		return this.charset;
	}

	public final void setCharset(Charset charset) {
		this.charset = charset != null ? charset : DEFAULT_CHARSET;
	}

	public final String getEncoding() {
		return this.charset.name();
	}

	public final void setEncoding(String encoding) {
		if (encoding != null) {
			this.charset = Charset.forName(encoding);
		} else {
			this.charset = DEFAULT_CHARSET;
		}
	}

	@Override
	public SourceReader open() throws IOException {
		return new FileSourceReader(this);
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
