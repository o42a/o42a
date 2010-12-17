/*
    Compiler LLVM Back-end Tests
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
package org.o42a.backend.llvm.test;

import static org.o42a.backend.llvm.LLVMGenerator.newGenerator;

import org.o42a.backend.llvm.LLVMGenerator;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.ir.IRGenerator;
import org.o42a.util.Source;


public class GeneratorTestCase extends CompilerTestCase {

	@Override
	protected void compile(Source source) {
		super.compile(source);

		final LLVMGenerator generator = newGenerator("test", "test");

		generator.setDebug(true);
		generator.addArgs(System.getProperty("llvm.args", ""));
		generateCode(new IRGenerator(generator));
		generator.write();
	}

}
