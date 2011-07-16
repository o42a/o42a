/*
    Compiler Tests
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
package org.o42a.compiler.test;

import java.util.HashMap;

import org.o42a.core.source.*;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.util.io.Source;
import org.o42a.util.io.StringSource;
import org.o42a.util.log.Logger;


class TestCompilerContext extends CompilerContext {

	private final CompilerTestCase test;
	private Source source;
	private final HashMap<String, TestCompilerContext> subContexts =
			new HashMap<String, TestCompilerContext>();

	TestCompilerContext(CompilerTestCase test, Logger logger) {
		super(
				CompilerTestCase.COMPILER,
				CompilerTestCase.INTRINSICS,
				logger);
		this.test = test;
		this.source = new StringSource(this.test.getModuleName(), "");
	}

	TestCompilerContext(
			CompilerTestCase test,
			CompilerContext parent,
			Source source) {
		super(parent, null);
		this.test = test;
		this.source = source;
	}

	@Override
	public ModuleCompiler compileModule() {
		return getCompiler().compileModule(
				new TestModuleSource(this, getSource()));
	}

	@Override
	public FieldCompiler compileField() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void include(DeclarativeBlock block, SectionTag tag) {
	}

	@Override
	@Deprecated
	public CompilerContext contextFor(String path) throws Exception {

		final int idx = path.indexOf('/');
		final String src;

		if (idx < 0) {
			src = path;
		} else {
			src = path.substring(0, idx);
		}

		final TestCompilerContext context = this.subContexts.get(src);

		if (context == null) {
			throw new IllegalStateException(src + " not found in " + this);
		}
		if (idx < 0) {
			return context;
		}

		return context.contextFor(path.substring(idx + 1));
	}

	@Override
	public Source getSource() {
		return this.source;
	}

	void setSource(Source source) {
		this.source = source;
	}

	void addSource(String path, Source source) {

		final int idx = path.indexOf('/');

		if (idx < 0) {
			this.subContexts.put(path, new TestCompilerContext(this.test, this, source));
			return;
		}

		final String src = path.substring(0, idx);
		final TestCompilerContext context = this.subContexts.get(src);

		if (context == null) {
			throw new IllegalStateException(src + " not found in " + this);
		}

		context.addSource(path.substring(idx + 1), source);
	}

}
