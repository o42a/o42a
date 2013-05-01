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

import org.o42a.ast.atom.BracketSign;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.clause.ClauseIdNodeVisitor;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.phrase.PhrasePartNodeVisitor;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.TypeNode;


public class BracketsNode
		extends AbstractExpressionNode
		implements PhrasePartNode, ClauseIdNode {

	private final SignNode<BracketSign> opening;
	private final ArgumentNode[] arguments;
	private final SignNode<BracketSign> closing;

	public BracketsNode(
			SignNode<BracketSign> opening,
			ArgumentNode[] arguments,
			SignNode<BracketSign> closing) {
		super(
				start(opening, firstNode(arguments)),
				end(opening, lastNode(arguments), closing));
		this.opening = opening;
		this.arguments = arguments;
		this.closing = closing;
	}

	public BracketsNode(
			SignNode<BracketSign> opening,
			ArgumentNode argument,
			SignNode<BracketSign> closing) {
		super(
				start(opening, argument),
				end(opening, argument, closing));
		this.opening = opening;
		this.arguments = new ArgumentNode[] {argument};
		this.closing = closing;
	}

	public final SignNode<BracketSign> getOpening() {
		return this.opening;
	}


	public final ArgumentNode[] getArguments() {
		return this.arguments;
	}

	public final SignNode<BracketSign> getClosing() {
		return this.closing;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitBrackets(this, p);
	}

	@Override
	public <R, P> R accept(PhrasePartNodeVisitor<R, P> visitor, P p) {
		return visitor.visitBrackets(this, p);
	}

	@Override
	public <R, P> R accept(ClauseIdNodeVisitor<R, P> visitor, P p) {
		return visitor.visitBrackets(this, p);
	}

	@Override
	public final DeclarableNode toDeclarable() {
		return null;
	}

	@Override
	public final ClauseIdNode toClauseId() {
		return this;
	}

	@Override
	public final TypeNode toType() {
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
		out.append('[');
		for (ArgumentNode argument : this.arguments) {
			argument.printContent(out);
		}
		out.append(']');
	}

}
