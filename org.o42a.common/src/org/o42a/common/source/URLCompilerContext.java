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

import static org.o42a.core.source.SectionTag.IMPLICIT_SECTION_TAG;

import java.net.MalformedURLException;
import java.net.URL;

import org.o42a.core.source.CompilerContext;
import org.o42a.util.io.URLSource;
import org.o42a.util.log.Logger;


@Deprecated
public class URLCompilerContext extends TreeCompilerContext<URLSource> {

	public URLCompilerContext(
			CompilerContext parentContext,
			String name,
			URL base,
			String path,
			Logger logger) {
		super(
				parentContext,
				new SingleURLSource(new URLSource(name, dir(base), path)),
				logger);
	}

	private URLCompilerContext(URLCompilerContext parent, String path) {
		super(
				parent,
				new SingleURLSource(
						nestedSource(parent, path)),
				IMPLICIT_SECTION_TAG,
				parent.getLogger());
	}

	private static URLSource nestedSource(
			URLCompilerContext parent,
			String path) {

		final URL url;

		try {
			url = new URL(dir(parent.getSource().getURL()), path);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}

		return new URLSource(parent.getSource().getBase(), url);
	}

	@Override
	@Deprecated
	public CompilerContext contextFor(String path) throws Exception {
		return new URLCompilerContext(this, path);
	}

	private static URL dir(URL url) {

		final String path = url.toExternalForm();
		final int slashIdx = path.lastIndexOf('/');

		if (slashIdx < 0) {
			return url;
		}
		if (slashIdx + 1 == path.length()) {
			return url;
		}

		try {
			return new URL(path.substring(0, slashIdx + 1));
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
