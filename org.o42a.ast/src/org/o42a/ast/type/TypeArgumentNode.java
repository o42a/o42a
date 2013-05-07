/*
    Abstract Syntax Tree
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.ast.type;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.expression.AbstractExpressionNode;
import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.ref.RefNode;


public class TypeArgumentNode
		extends AbstractExpressionNode
		implements TypeNode {

	private final TypeNode argument;
	private final SignNode<TypeArgumentSeparator> separator;
	private final TypeNode type;

	public TypeArgumentNode(
			TypeNode argument,
			SignNode<TypeArgumentSeparator> separator,
			TypeNode type) {
		super(argument.getStart(), type.getEnd());
		this.argument = argument;
		this.separator = separator;
		this.type = type;
	}

	public final TypeNode getArgument() {
		return this.argument;
	}

	public final SignNode<TypeArgumentSeparator> getSeparator() {
		return this.separator;
	}

	public final TypeNode getType() {
		return this.type;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitTypeArgument(this, p);
	}

	@Override
	public <R, P> R accept(TypeNodeVisitor<R, P> visitor, P p) {
		return visitor.visitTypeArgument(this, p);
	}

	@Override
	public final DeclarableNode toDeclarable() {
		return null;
	}

	@Override
	public final TypeNode toType() {
		return this;
	}

	@Override
	public final BinaryNode toBinary() {
		return null;
	}

	@Override
	public final ClauseIdNode toClauseId() {
		return null;
	}

	@Override
	public final RefNode toRef() {
		return null;
	}

	@Override
	public void printContent(StringBuilder out) {
		this.argument.printContent(out);
		out.append("` ");
		this.type.printContent(out);
	}

	public enum TypeArgumentSeparator implements SignType {

		BACKQUOTE() {

			@Override
			public String getSign() {
				return "`";
			}

		}

	}

}
