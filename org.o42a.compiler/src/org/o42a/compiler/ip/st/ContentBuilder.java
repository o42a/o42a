/*
    Compiler
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.st;

import static org.o42a.compiler.ip.st.StInterpreter.addContent;

import org.o42a.ast.expression.BlockNode;
import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.BlockBuilder;


final class ContentBuilder extends BlockBuilder {

	private final AccessRules accessRules;
	private final StatementVisitor statementVisitor;
	private final BlockNode<?> block;

	ContentBuilder(
			AccessRules accessRules,
			StatementVisitor statementVisitor,
			BlockNode<?> block) {
		super(statementVisitor.getContext(), block);
		this.accessRules = accessRules;
		this.statementVisitor = statementVisitor;
		this.block = block;
	}

	@Override
	public void buildBlock(Block<?> block) {
		addContent(this.accessRules, this.statementVisitor, block, this.block);
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		this.block.printContent(out);

		return out.toString();
	}

}
