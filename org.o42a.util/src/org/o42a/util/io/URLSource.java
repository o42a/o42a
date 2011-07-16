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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;


public class URLSource extends Source {

	public static boolean urlIsDirectory(URL url) {

		final String path = url.getPath();

		return path.isEmpty() || path.endsWith("/");
	}

	private final URL base;
	private final URL url;
	private final String name;

	public URLSource(String name, URL base, String path) {
		this.base = base;
		try {
			this.url = new URL(base, path);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		this.name = name != null ? name : path;
	}

	public URLSource(URLSource parent, String path) {
		this(parent.getBase(), parent.getURL(), path);
	}

	@Deprecated
	public URLSource(URL base, URL url) {
		this.base = base;
		this.url = url;

		final String name = name(null);

		this.name = name != null ? name : url.toString();
	}

	public URLSource(URL base, URL relativeTo, String path) {
		this.base = base;
		try {
			this.url = new URL(relativeTo, path);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		this.name = name(path);
	}

	public final URL getBase() {
		return this.base;
	}

	public final URL getURL() {
		return this.url;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public final boolean isDirectory() {
		return urlIsDirectory(getURL());
	}

	@Override
	public boolean isEmpty() {
		return isDirectory();
	}

	@Override
	public Reader open() throws IOException {
		return new InputStreamReader(this.url.openStream(), "UTF-8");
	}

	private String name(String path) {

		final String baseString = this.base.toString();
		final String urlString = this.url.toString();

		if (urlString.startsWith(baseString)) {
			return urlString.substring(baseString.length());
		}

		final String rootString;

		try {
			rootString = new URL(this.base, "/").toString();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException();
		}

		if (!urlString.startsWith(rootString)) {
			return path;
		}

		return urlString.substring(rootString.length());
	}

}
