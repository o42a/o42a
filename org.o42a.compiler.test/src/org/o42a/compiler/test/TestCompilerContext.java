/*
    Compiler Tests
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.compiler.test;

import org.o42a.core.source.*;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.util.io.EmptySource;
import org.o42a.util.io.Source;
import org.o42a.util.log.Logger;


class TestCompilerContext extends CompilerContext {

	private final EmptySource source;
	private final CompilerTestCase test;

	TestCompilerContext(CompilerTestCase test, Logger logger) {
		super(
				CompilerTestCase.COMPILER,
				CompilerTestCase.INTRINSICS,
				logger);
		this.test = test;
		this.source = new EmptySource("empty");
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
	public void include(DeclarativeBlock block, SectionTag tag) {
	}

	@Override
	public String toString() {
		if (this.test == null) {
			return super.toString();
		}
		return this.test.getClass().getSimpleName();
	}

}
