/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test;

import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.FieldCompiler;
import org.o42a.core.source.ModuleCompiler;
import org.o42a.core.st.sentence.Block;
import org.o42a.util.io.EmptySource;
import org.o42a.util.io.Source;


class TestCompilerContext extends CompilerContext {

	private final EmptySource source;
	private final CompilerTestCase test;

	TestCompilerContext(CompilerTestCase test) {
		super(test.getCompiler(), test.getIntrinsics());
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
	public void include(Block block) {
	}

	@Override
	public String toString() {
		if (this.test == null) {
			return super.toString();
		}
		return this.test.getClass().getSimpleName();
	}

}
