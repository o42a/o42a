/*
    Intrinsics
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.intrinsic.impl;

import static org.o42a.util.log.Logger.DECLARATION_LOGGER;

import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.FieldCompiler;
import org.o42a.core.source.ModuleCompiler;
import org.o42a.core.st.sentence.Block;
import org.o42a.intrinsic.CompilerIntrinsics;
import org.o42a.util.io.EmptySource;
import org.o42a.util.io.Source;


public final class TopContext extends CompilerContext {

	private final EmptySource source = new EmptySource("<TOP>");

	public TopContext(CompilerIntrinsics intrinsics) {
		super(intrinsics.getCompiler(), intrinsics, DECLARATION_LOGGER);
	}

	@Override
	public Source getSource() {
		return this.source;
	}

	@Override
	public ModuleCompiler compileModule() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FieldCompiler compileField() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void include(Block block) {
	}

	@Override
	public String toString() {
		return "<TOP>";
	}

}
