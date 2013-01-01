/*
    Abstract Syntax Tree
    Copyright (C) 2010-2013 Ruslan Lopatin

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
import org.o42a.ast.NodeVisitor;
import org.o42a.ast.atom.CommaSign;
import org.o42a.ast.atom.SignNode;
import org.o42a.util.io.SourcePosition;


public class ArgumentNode extends AbstractNode {

	private final SignNode<CommaSign> separator;
	private final ExpressionNode value;

	public ArgumentNode(SourcePosition start) {
		super(start, start);
		this.separator = null;
		this.value = null;
	}

	public ArgumentNode(SignNode<CommaSign> separator, ExpressionNode value) {
		super(separator, value);
		this.separator = separator;
		this.value = value;
	}

	public SignNode<CommaSign> getSeparator() {
		return this.separator;
	}

	public ExpressionNode getValue() {
		return this.value;
	}

	@Override
	public <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
		return visitor.visitArgument(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.separator != null) {
			this.separator.printContent(out);
		}
		if (this.value != null) {
			this.value.printContent(out);
		}
	}

}
