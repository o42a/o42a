/*
    Intrinsics
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.intrinsic.root;

import static org.o42a.util.log.Logger.DECLARATION_LOGGER;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

import org.o42a.core.CompilerContext;
import org.o42a.util.Source;


final class RootContext extends CompilerContext {

	private final RootSource source;

	RootContext(CompilerContext topContext) {
		super(topContext, DECLARATION_LOGGER);
		this.source = new RootContext.RootSource();
	}

	@Override
	public CompilerContext contextFor(String path) throws Exception {
		return new ChildContext(this, path);
	}

	@Override
	public RootContext.RootSource getSource() {
		return this.source;
	}

	private static final class ChildContext extends CompilerContext {

		private final URISource source;

		ChildContext(
				RootContext parent,
				String path)
		throws URISyntaxException {
			super(parent, null);
			this.source = new URISource(parent.source.getBase(), path);
		}

		private ChildContext(
				ChildContext parent,
				String path)
		throws URISyntaxException {
			super(parent, null);
			this.source = new URISource(parent.source, path);
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

	private static final class RootSource extends Source {

		private static final long serialVersionUID = -8754601810815914875L;

		private URI base;

		@Override
		public String getName() {
			return "ROOT";
		}

		@Override
		public Reader open() throws IOException {

			final URI uri;

			try {
				uri = getBase().resolve("root.o42a");
			} catch (URISyntaxException e) {
				throw new IOException(e);
			}

			return new InputStreamReader(
					uri.toURL().openStream(),
					"UTF-8");
		}

		private final URI getBase() throws URISyntaxException {
			if (this.base == null) {
				this.base =
					getClass().getResource(".").toURI().resolve(
							new URI(null, null, "../../../..", null));
			}
			return this.base;
		}

	}

	private static final class URISource extends Source {

		private static final long serialVersionUID = 9099583978813394720L;

		private final URI base;
		private final URI uri;
		private final String name;

		URISource(URI base, String path) throws URISyntaxException {
			this(base, base, path);
		}

		URISource(URISource parent, String path) throws URISyntaxException {
			this(parent.base, parent.uri, path);
		}

		private URISource(
				URI base,
				URI relativeTo,
				String path)
		throws URISyntaxException {

			final URI uri = new URI(path);

			if (uri.isAbsolute()) {
				this.base = this.uri = uri.normalize();
				this.name = this.uri.toString();
			} else {
				this.base = base;
				this.uri = relativeTo.resolve(uri).normalize();
				this.name = this.base.relativize(this.uri).toString();
			}
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public Reader open() throws IOException {
			return new InputStreamReader(
					this.uri.toURL().openStream(),
					"UTF-8");
		}

	}

}
