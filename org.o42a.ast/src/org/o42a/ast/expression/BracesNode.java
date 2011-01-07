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

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.statement.AbstractStatementNode;
import org.o42a.ast.statement.StatementNodeVisitor;


public class BracesNode
		extends AbstractStatementNode
		implements BlockNode<BracesNode.Brace> {

	private final SignNode<Brace> opening;
	private final SentenceNode[] content;
	private final SignNode<Brace> closing;

	public BracesNode(
			SignNode<Brace> opening,
			SentenceNode[] content,
			SignNode<Brace> closing) {
		super(opening, firstNode(content), lastNode(content), closing);
		this.opening = opening;
		this.content = content;
		this.closing = closing;
	}

	@Override
	public SignNode<Brace> getOpening() {
		return this.opening;
	}

	@Override
	public SentenceNode[] getContent() {
		return this.content;
	}

	@Override
	public SignNode<Brace> getClosing() {
		return this.closing;
	}

	@Override
	public <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return visitor.visitBraces(this, p);
	}

	@Override
	public <R, P> R accept(ClauseNodeVisitor<R, P> visitor, P p) {
		return visitor.visitBraces(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		out.append('{');
		for (SentenceNode sentence : this.content) {
			sentence.printContent(out);
		}
		out.append('}');
	}

	public enum Brace implements SignType {

		OPENING_BRACE() {
			@Override
			public String getSign() {
				return "{";
			}
		},
		CLOSING_BRACE() {
			@Override
			public String getSign() {
				return "}";
			}
		}

	}

}
