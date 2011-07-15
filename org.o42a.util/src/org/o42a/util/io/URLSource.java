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

	private static final long serialVersionUID = -711560294197084017L;

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

	public URLSource(URL base, String path) {
		this.base = base;
		try {
			this.url = new URL(base, path);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		this.name = path;
	}

	public URLSource(URLSource parent, String path) {
		this.base = parent.getBase();
		try {
			this.url = new URL(parent.getURL(), path);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}

		final String baseString = this.base.toString();
		final String urlString = this.url.toString();

		if (urlString.startsWith(baseString)) {
			this.name = urlString.substring(baseString.length());
		} else {

			final String rootString;

			try {
				rootString = new URL(this.base, "/").toString();
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException();
			}

			if (urlString.startsWith(rootString)) {
				this.name = urlString.substring(rootString.length());
			} else {
				this.name = path;
			}
		}
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

	@Override
	public Reader open() throws IOException {
		return new InputStreamReader(this.url.openStream(), "UTF-8");
	}

}
