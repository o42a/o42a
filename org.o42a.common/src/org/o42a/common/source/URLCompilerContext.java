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

import java.net.URL;

import org.o42a.core.source.CompilerContext;
import org.o42a.util.io.URLSource;
import org.o42a.util.log.Logger;


public class URLCompilerContext extends TreeCompilerContext<URLSource> {

	public URLCompilerContext(
			CompilerContext parentContext,
			String name,
			URL base,
			String path,
			Logger logger) {
		super(
				parentContext,
				new SingleURLSource(name, base, path),
				logger);
	}

	private URLCompilerContext(
			URLCompilerContext parent,
			String path) {
		super(
				parent,
				new SingleURLSource(parent.getSource(), path),
				IMPLICIT_SECTION_TAG);
	}

	@Override
	@Deprecated
	public CompilerContext contextFor(String path) throws Exception {
		return new URLCompilerContext(this, path);
	}

}
