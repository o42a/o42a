/*
    Abstract Syntax Tree
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import org.o42a.ast.Position;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.sentence.SentenceNode;


public class ParenthesesNode
		extends AbstractExpressionNode
		implements BlockNode<ParenthesesNode.Parenthesis> {

	private final SignNode<Parenthesis> opening;
	private final SentenceNode[] content;
	private final SignNode<Parenthesis> closing;

	public ParenthesesNode(
			SignNode<Parenthesis> opening,
			SentenceNode[] content,
			SignNode<Parenthesis> closing) {
		super(opening, firstNode(content), lastNode(content), closing);
		this.opening = opening;
		this.content = content;
		this.closing = closing;
	}

	public ParenthesesNode(Position start, Position end) {
		super(start, end);
		this.opening = null;
		this.content = new SentenceNode[0];
		this.closing = null;
	}

	@Override
	public SignNode<Parenthesis> getOpening() {
		return this.opening;
	}

	@Override
	public SentenceNode[] getContent() {
		return this.content;
	}

	@Override
	public SignNode<Parenthesis> getClosing() {
		return this.closing;
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitParentheses(this, p);
	}

	@Override
	public <R, P> R accept(ClauseNodeVisitor<R, P> visitor, P p) {
		return visitor.visitParentheses(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		out.append('(');
		for (SentenceNode sentence : this.content) {
			sentence.printContent(out);
		}
		out.append(')');
	}

	public enum Parenthesis implements SignType {

		OPENING_PARENTHESIS() {
			@Override
			public String getSign() {
				return "(";
			}
		},
		CLOSING_PARENTHESIS() {
			@Override
			public String getSign() {
				return ")";
			}
		}

	}

}
