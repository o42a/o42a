/*
    Abstract Syntax Tree
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.TypeArgumentNode;


public class GroupNode extends AbstractExpressionNode {

	private final ExpressionNode expression;
	private final SignNode<Separator> separator;

	public GroupNode(
			ExpressionNode expression,
			SignNode<Separator> separator) {
		super(expression.getStart(), separator.getEnd());
		this.expression = expression;
		this.separator = separator;
	}

	public final ExpressionNode getExpression() {
		return this.expression;
	}

	public final SignNode<Separator> getSeparator() {
		return this.separator;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitGroup(this, p);
	}

	@Override
	public final DeclarableNode toDeclarable() {
		return null;
	}

	@Override
	public final ClauseIdNode toClauseId() {
		return null;
	}

	@Override
	public final TypeArgumentNode toTypeArgument() {
		return null;
	}

	@Override
	public final RefNode toRef() {
		return null;
	}

	@Override
	public final BinaryNode toBinary() {
		return null;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.expression.printContent(out);
		this.separator.printContent(out);
	}

	public enum Separator implements SignType {

		BACKSLASH() {

			@Override
			public String getSign() {
				return "\\";
			}

		};

	}

}
