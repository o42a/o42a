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
import static org.o42a.util.string.StringUtil.indexOfDiff;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.o42a.util.io.URLSource;


public class URLSources extends URLSourceTree {

	private HashMap<String, URLSources> subTrees;

	public URLSources(String name, URL base, String path) {
		super(name, base, path);
	}

	public URLSources(URLSource parent, String path) {
		super(parent, path);
	}

	public URLSources(URLSourceTree parent, String path) {
		super(parent, path);
	}

	public URLSources(URLSource source) {
		super(source);
	}

	@Override
	public Iterator<? extends URLSources> subTrees() {
		if (this.subTrees != null) {
			return this.subTrees.values().iterator();
		}
		return Collections.<URLSources>emptyList().iterator();
	}

	public URLSourceTree add(String path) {

		URLSources dir = this;
		int first = 0;

		for (;;) {
			first = indexOfDiff(path, '/', first);
			if (first < 0) {
				return dir;
			}

			final int slashIdx = path.indexOf('/', first);

			if (slashIdx < 0) {

				final String name = first == 0 ? path : path.substring(first);

				if (name.endsWith(FILE_SUFFIX)) {
					return dir.addFile(name);
				}

				return dir.addEmpty(name);
			}

			dir = addDirectory(path.substring(first, slashIdx));
			first = slashIdx + 1;
		}
	}

	public URLSources addDirectory(String name) {
		init();

		final URLSources dir = new URLSources(this, name + '/');
		final String dirName = dir.getFileName().getName();
		final URLSources existing = this.subTrees.put(dirName, dir);

		if (existing == null) {
			return dir;
		}

		assert (existing.getSource().isDirectory()
				|| !existing.getSource().isEmpty()) :
					existing + " is empty";

		this.subTrees.put(dirName, existing);

		return existing;
	}

	public URLSourceTree addFile(String name) {
		init();

		final URLSources file = new URLSources(this, name);
		final String fileName = file.getFileName().getName();
		final URLSources existing = this.subTrees.put(fileName, file);

		if (existing == null) {
			return file;
		}

		assert (existing.getSource().isDirectory()
				|| !existing.getSource().isEmpty()) :
					existing + " is empty";

		this.subTrees.put(fileName, existing);

		return existing;
	}

	public URLSourceTree addEmpty(String name) {
		init();

		final URLSources file = new URLSources(
				new EmptyURLSource.EmptySource(
						getSource().getBase(),
						childDirURL(getSource().getURL()),
						name));
		final String fileName = file.getFileName().getName();
		final URLSources existing = this.subTrees.put(fileName, file);

		if (existing == null) {
			return file;
		}

		assert (!existing.getSource().isDirectory()
				&& existing.getSource().isEmpty()) :
					existing + " is not empty";

		this.subTrees.put(fileName, existing);

		return existing;
	}

	private void init() {
		assert !getSource().isEmpty() || getSource().isDirectory() :
			this + " is empty";
		if (this.subTrees == null) {
			this.subTrees = new HashMap<>();
		}
	}

}
