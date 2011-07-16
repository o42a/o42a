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

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.o42a.util.io.URLSource;


public class URLSources extends URLSourceTree {

	private HashMap<String, URLSources> childTrees;

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
	public Iterator<? extends URLSources> childTrees() {
		if (this.childTrees != null) {
			return this.childTrees.values().iterator();
		}
		return Collections.<URLSources>emptyList().iterator();
	}

	public URLSources addDirectory(String name) {
		if (this.childTrees == null) {
			this.childTrees = new HashMap<String, URLSources>();
		}

		final URLSources dir = new URLSources(this, name + '/');
		final String fileName = dir.getFileName().getName();
		final URLSources existing = this.childTrees.put(fileName, dir);

		if (existing == null) {
			return dir;
		}

		this.childTrees.put(fileName, existing);

		return existing;
	}

	public URLSources addFile(String name) {
		if (this.childTrees == null) {
			this.childTrees = new HashMap<String, URLSources>();
		}

		final URLSources dir = new URLSources(this, name);
		final String fileName = dir.getFileName().getName();
		final URLSources existing = this.childTrees.put(fileName, dir);

		if (existing == null || existing.getSource().isDirectory()) {
			return dir;
		}

		this.childTrees.put(fileName, existing);

		throw new IllegalArgumentException(
				"File " + name + " already present in " + this);
	}

}
