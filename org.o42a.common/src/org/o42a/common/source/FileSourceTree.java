/*
    Compiler Commons
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
package org.o42a.common.source;

import static org.o42a.util.io.SourceFileName.FILE_SUFFIX;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.o42a.util.io.FileSource;
import org.o42a.util.io.SourceFileName;


public class FileSourceTree extends SourceTree<FileSource> {

	public static File childDir(File parent) {
		if (parent.isDirectory()) {
			return parent;
		}

		final File parentDir = parent.getParentFile();
		final String name = parent.getName();

		if (!name.endsWith(FILE_SUFFIX)) {
			return new File(parentDir, name + FILE_SUFFIX);
		}

		return new File(parentDir, removeSuffix(name));
	}

	public FileSourceTree(String name, File root, String path) {
		this(new FileSource(name, root, path));
	}

	public FileSourceTree(FileSource parent, String path) {
		this(new FileSource(
				parent.getRoot(),
				childDir(parent.getFile()),
				path));
		getSource().setCharset(parent.getCharset());
	}

	public FileSourceTree(FileSourceTree parent, String path) {
		this(parent.getSource(), path);
	}

	public FileSourceTree(FileSource source) {
		super(source, new SourceFileName(source.getFile().getName()));
	}

	@Override
	public Iterator<? extends FileSourceTree> subTrees() {

		final File dir = childDir(getSource().getFile());
		final File[] list = dir.listFiles();

		if (list == null) {
			return Collections.<FileSourceTree>emptyList().iterator();
		}

		final HashMap<String, FileSourceTree> sources =
				new HashMap<>(list.length);

		for (File file : list) {
			if (!containsSource(file)) {
				continue;
			}

			final String name = file.getName();

			if (file.isFile()) {
				sources.put(removeSuffix(name), new FileSourceTree(this, name));
				continue;
			}

			if (sources.containsKey(name)) {
				// Use directory only if corresponding file didn't used already.
				continue;
			}
			sources.put(name, new FileSourceTree(this, name));
		}

		return sources.values().iterator();
	}

	private static String removeSuffix(String name) {
		return name.substring(0, name.length() - FILE_SUFFIX.length());
	}

	private static boolean containsSource(File file) {

		final String name = file.getName();

		if (name.startsWith(".") || name.startsWith("_")) {
			// Hidden file or directory.
			return false;
		}
		if (file.isFile()) {
			// File should have '.o42a' extension.
			return name.endsWith(FILE_SUFFIX);
		}
		if (!file.isDirectory()) {
			// Neither file, not directory.
			return false;
		}

		// Only consider directories containing sources.
		for (File f : file.listFiles()) {
			if (containsSource(f)) {
				return true;
			}
		}

		return false;
	}

}
