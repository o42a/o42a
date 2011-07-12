/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import org.o42a.core.source.CompilerContext;
import org.o42a.util.io.Source;
import org.o42a.util.log.Logger;


public class URLContext extends CompilerContext {

	private final BaseSource source;

	public URLContext(
			CompilerContext parentContext,
			String name,
			URL base,
			String path,
			Logger logger) {
		super(parentContext, logger);
		this.source =
				new BaseSource(dir(base), path, name != null ? name : path);
	}

	@Override
	public CompilerContext contextFor(String path) throws Exception {
		return new ChildContext(this, path);
	}

	@Override
	public Source getSource() {
		return this.source;
	}

	private static URL dir(URL url) {

		final String path = url.getPath();

		if (path.endsWith("/")) {
			return url;
		}

		final int slashIdx = path.lastIndexOf('/');

		if (slashIdx < 0) {
			return url;
		}
		if (slashIdx + 1 == path.length()) {
			return url;
		}

		try {
			return new URL(url, path.substring(0, slashIdx + 1));
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static final class ChildContext extends CompilerContext {

		private final URLSource source;

		ChildContext(
				URLContext parent,
				String path)
		throws IOException {
			super(parent, null);
			this.source = new URLSource(parent.source.base, path);
		}

		private ChildContext(
				ChildContext parent,
				String path)
		throws IOException {
			super(parent, null);
			this.source = new URLSource(parent.source, path);
		}

		@Override
		public CompilerContext contextFor(String path) throws Exception {
			return new ChildContext(this, path);
		}

		@Override
		public Source getSource() {
			return this.source;
		}

	}

	private static final class BaseSource extends Source {

		private static final long serialVersionUID = -5259605489095552120L;

		private final URL base;
		private final String source;
		private final String name;

		BaseSource(URL base, String path, String name) {
			this.base = base;
			this.source = path;
			this.name = name;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getFileName() {
			return this.base.getFile();
		}

		@Override
		public Reader open() throws IOException {

			final URL url = new URL(this.base, this.source);

			return new InputStreamReader(url.openStream(), "UTF-8");
		}

	}

	private static final class URLSource extends Source {

		private static final long serialVersionUID = -2519270339565039100L;

		private final URL base;
		private final URL url;
		private final String name;

		URLSource(URL base, String path) throws IOException {
			this(base, base, path);
		}

		URLSource(URLSource parent, String path) throws IOException {
			this(parent.base, parent.url, path);
		}

		private URLSource(
				URL base,
				URL relativeTo,
				String path)
		throws IOException {
			this.base = base;
			this.url = new URL(relativeTo, path);

			final String baseString = base.toString();
			final String urlString = this.url.toString();

			if (urlString.startsWith(baseString)) {
				this.name = urlString.substring(baseString.length());
			} else {

				final String rootString = new URL(base, "/").toString();

				if (urlString.startsWith(rootString)) {
					this.name = urlString.substring(rootString.length());
				} else {
					this.name = path;
				}
			}
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getFileName() {
			return this.url.getFile();
		}

		@Override
		public Reader open() throws IOException {
			return new InputStreamReader(this.url.openStream(), "UTF-8");
		}

	}

}
