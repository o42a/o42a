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

import org.o42a.ast.atom.ParenthesisSign;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.phrase.PhrasePartNodeVisitor;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.type.TypeNode;
import org.o42a.ast.type.TypeNodeVisitor;
import org.o42a.util.io.SourcePosition;


public class ParenthesesNode
		extends AbstractExpressionNode
		implements BlockNode<ParenthesisSign>, PhrasePartNode, TypeNode {

	private final SignNode<ParenthesisSign> opening;
	private final SentenceNode[] content;
	private final SignNode<ParenthesisSign> closing;

	public ParenthesesNode(
			SignNode<ParenthesisSign> opening,
			SentenceNode[] content,
			SignNode<ParenthesisSign> closing) {
		super(opening, firstNode(content), lastNode(content), closing);
		this.opening = opening;
		this.content = content;
		this.closing = closing;
	}

	public ParenthesesNode(SourcePosition start, SourcePosition end) {
		super(start, end);
		this.opening = null;
		this.content = new SentenceNode[0];
		this.closing = null;
	}

	@Override
	public SignNode<ParenthesisSign> getOpening() {
		return this.opening;
	}

	@Override
	public SentenceNode[] getContent() {
		return this.content;
	}

	@Override
	public SignNode<ParenthesisSign> getClosing() {
		return this.closing;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitParentheses(this, p);
	}

	@Override
	public <R, P> R accept(PhrasePartNodeVisitor<R, P> visitor, P p) {
		return visitor.visitParentheses(this, p);
	}

	@Override
	public <R, P> R accept(TypeNodeVisitor<R, P> visitor, P p) {
		return visitor.visitParentheses(this, p);
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
	public final TypeNode toType() {
		return this;
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
		if (this.content.length == 0) {
			out.append("()");
			return;
		}
		out.append('(');
		if (this.content.length == 1) {
			this.content[0].printContent(out);
		} else {
			for (SentenceNode sentence : this.content) {
				out.append("\n  ");
				sentence.printContent(out);
			}
			out.append('\n');
		}
		out.append(')');
	}

}
