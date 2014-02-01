/*
    Abstract Syntax Tree
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.ast.statement;

import org.o42a.ast.AbstractNode;
import org.o42a.ast.NodeVisitor;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.expression.ExpressionNode;


public class LocalNode extends AbstractNode implements AssignableNode {

	private final ExpressionNode expression;
	private final SignNode<Separator> separator;
	private final NameNode name;

	public LocalNode(
			ExpressionNode expression,
			SignNode<Separator> separator,
			NameNode name) {
		super(expression.getStart(), end(separator, name));
		this.expression = expression;
		this.separator = separator;
		this.name = name;
	}

	public final ExpressionNode getExpression() {
		return this.expression;
	}

	public final SignNode<Separator> getSeparator() {
		return this.separator;
	}

	public final NameNode getName() {
		return this.name;
	}

	@Override
	public final ExpressionNode toExpression() {
		return null;
	}

	@Override
	public final LocalNode toLocal() {
		return this;
	}

	@Override
	public <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
		return visitor.visitLocal(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.expression.printContent(out);
		out.append(' ');
		this.separator.printContent(out);
		if (this.name != null) {
			out.append(' ');
			this.name.printContent(out);
		}
	}

	public static enum Separator implements SignType {

		DOLLAR_SIGN() {

			@Override
			public String getSign() {
				return "$";
			}

		}

	}

}
