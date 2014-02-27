/*
    Abstract Syntax Tree
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
package org.o42a.ast.expression;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.atom.CommaSign;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.statement.AssignmentOperator;
import org.o42a.util.io.SourcePosition;


public class ArgumentNode extends AbstractNode {

	private final SignNode<CommaSign> separator;
	private final SignNode<AssignmentOperator> init;
	private final ExpressionNode value;

	public ArgumentNode(SourcePosition start) {
		super(start, start);
		this.separator = null;
		this.init = null;
		this.value = null;
	}

	public ArgumentNode(
			SignNode<CommaSign> separator,
			SignNode<AssignmentOperator> init,
			ExpressionNode value) {
		super(separator, init, value);
		this.separator = separator;
		this.init = init;
		this.value = value;
	}

	public final SignNode<CommaSign> getSeparator() {
		return this.separator;
	}

	public final SignNode<AssignmentOperator> getInit() {
		return this.init;
	}

	public final boolean isInitializer() {
		return this.init != null;
	}

	public final ExpressionNode getValue() {
		return this.value;
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.separator != null) {
			this.separator.printContent(out);
		}
		if (this.init != null) {
			this.init.printContent(out);
		}
		if (this.value != null) {
			this.value.printContent(out);
		}
	}

}
