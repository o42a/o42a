/*
    Intrinsics
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
package org.o42a.intrinsic.root;

import static org.o42a.util.log.Logger.DECLARATION_LOGGER;

import org.o42a.core.source.CompilerContext;
import org.o42a.intrinsic.CompilerIntrinsics;
import org.o42a.util.io.Source;


public final class TopContext extends CompilerContext {

	public TopContext(CompilerIntrinsics intrinsics) {
		super(intrinsics.getCompiler(), intrinsics, DECLARATION_LOGGER);
	}

	@Override
	public Source getSource() {
		return null;
	}

	@Override
	public CompilerContext contextFor(String path) throws Exception {
		throw new UnsupportedOperationException(this + " has no child contexts");
	}

	@Override
	public String toString() {
		return "<TOP>";
	}

}
