/*
    Modules Commons
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
package org.o42a.common.source;

import static org.o42a.core.source.SourceFileName.FILE_SUFFIX;
import static org.o42a.util.io.URLSource.urlIsDirectory;

import java.net.MalformedURLException;
import java.net.URL;

import org.o42a.core.source.SourceFileName;
import org.o42a.util.io.URLSource;


public abstract class URLSourceTree extends SourceTree<URLSource> {

	public URLSourceTree(String name, URL base, String path) {
		this(new URLSource(name, relativeTo(base), path));
	}

	public URLSourceTree(URLSource parent, String path) {
		this(new URLSource(
				parent.getBase(),
				relativeTo(parent.getURL()),
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

	private static URL relativeTo(URL url) {
		if (urlIsDirectory(url)) {
			return url;
		}

		final String path = url.toExternalForm();
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

	private static String fileName(String path) {

		final String filePath = removeTrailingSlashes(path);
		final int slashIdx = filePath.lastIndexOf('/');

		if (slashIdx < 0) {
			return filePath;
		}

		return filePath.substring(slashIdx + 1);
	}

	private static String removeTrailingSlashes(String path) {

		int last = path.length() - 1;

		while (last >= 0) {
			if (path.charAt(last) != '/') {
				break;
			}
			--last;
		}

		return path.substring(0, last + 1);
	}

}
