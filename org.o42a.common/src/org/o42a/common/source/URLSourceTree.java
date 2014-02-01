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
import static org.o42a.util.io.URLSource.urlIsDirectory;
import static org.o42a.util.string.StringUtil.removeTrailingChars;

import java.net.MalformedURLException;
import java.net.URL;

import org.o42a.util.io.SourceFileName;
import org.o42a.util.io.URLSource;


public abstract class URLSourceTree extends SourceTree<URLSource> {

	public static URL childDirURL(URL parentURL) {
		if (urlIsDirectory(parentURL)) {
			return parentURL;
		}

		final String path = parentURL.toExternalForm();
		final String dir;

		if (path.endsWith(FILE_SUFFIX)) {
			dir = path.substring(0, path.length() - FILE_SUFFIX.length());
		} else {
			dir = path;
		}

		try {
			return new URL(dir + "/");
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public URLSourceTree(String name, URL base, String path) {
		this(new URLSource(name, childDirURL(base), path));
	}

	public URLSourceTree(URLSource parent, String path) {
		this(new URLSource(
				parent.getBase(),
				childDirURL(parent.getURL()),
				path));
	}

	public URLSourceTree(URLSourceTree parent, String path) {
		this(parent.getSource(), path);
	}

	public URLSourceTree(URLSource source) {
		super(
				source,
				new SourceFileName(fileName(source.getURL().getPath())));
	}

	private static String fileName(String path) {

		final String filePath = removeTrailingChars(path, '/');
		final int slashIdx = filePath.lastIndexOf('/');

		if (slashIdx < 0) {
			return filePath;
		}

		return filePath.substring(slashIdx + 1);
	}

}
