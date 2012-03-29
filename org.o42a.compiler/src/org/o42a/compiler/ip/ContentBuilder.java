/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.compiler.ip;

import org.o42a.ast.expression.BlockNode;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.BlockBuilder;


final class ContentBuilder extends BlockBuilder {

	private final StatementVisitor statementVisitor;
	private final BlockNode<?> block;

	ContentBuilder(StatementVisitor statementVisitor, BlockNode<?> block) {
		super(statementVisitor.getContext(), block);
		this.statementVisitor = statementVisitor;
		this.block = block;
	}

	@Override
	public void buildBlock(Block<?> block) {
		Interpreter.addContent(this.statementVisitor, block, this.block);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		this.block.printContent(out);

		return out.toString();
	}

}
