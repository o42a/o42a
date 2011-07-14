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

import java.net.MalformedURLException;
import java.net.URL;

import org.o42a.core.source.CompilerContext;
import org.o42a.util.io.URLSource;
import org.o42a.util.log.Logger;


public class URLContext extends CompilerContext {

	private final URLSource source;

	public URLContext(
			CompilerContext parentContext,
			String name,
			URL base,
			String path,
			Logger logger)
	throws MalformedURLException {
		super(parentContext, logger);
		this.source = new URLSource(name, dir(base), path);
	}

	public URLContext(
			URLContext parent,
			String path)
	throws MalformedURLException {
		super(parent, null);
		this.source = new URLSource(parent.getSource(), path);
	}

	@Override
	public CompilerContext contextFor(String path) throws Exception {
		return new URLContext(this, path);
	}

	@Override
	public URLSource getSource() {
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

}
