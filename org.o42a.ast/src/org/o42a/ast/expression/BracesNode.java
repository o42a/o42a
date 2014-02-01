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

import org.o42a.ast.atom.BraceSign;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.clause.ClauseIdNodeVisitor;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.phrase.PhrasePartNodeVisitor;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.statement.AbstractStatementNode;
import org.o42a.ast.statement.StatementNodeVisitor;


public class BracesNode
		extends AbstractStatementNode
		implements BlockNode<BraceSign>, PhrasePartNode, ClauseIdNode {

	private final SignNode<BraceSign> opening;
	private final SentenceNode[] content;
	private final SignNode<BraceSign> closing;

	public BracesNode(
			SignNode<BraceSign> opening,
			SentenceNode[] content,
			SignNode<BraceSign> closing) {
		super(opening, firstNode(content), lastNode(content), closing);
		this.opening = opening;
		this.content = content;
		this.closing = closing;
	}

	@Override
	public SignNode<BraceSign> getOpening() {
		return this.opening;
	}

	@Override
	public SentenceNode[] getContent() {
		return this.content;
	}

	@Override
	public SignNode<BraceSign> getClosing() {
		return this.closing;
	}

	@Override
	public <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return visitor.visitBraces(this, p);
	}

	@Override
	public <R, P> R accept(PhrasePartNodeVisitor<R, P> visitor, P p) {
		return visitor.visitBraces(this, p);
	}

	@Override
	public <R, P> R accept(ClauseIdNodeVisitor<R, P> visitor, P p) {
		return visitor.visitBraces(this, p);
	}

	@Override
	public final ClauseIdNode toClauseId() {
		return this;
	}

	@Override
	public final ExpressionNode toExpression() {
		return null;
	}

	@Override
	public final RefNode toRef() {
		return null;
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.content.length == 0) {
			out.append("{}");
			return;
		}
		out.append('{');
		if (this.content.length == 1) {
			this.content[0].printContent(out);
		} else {
			for (SentenceNode sentence : this.content) {
				out.append("\n  ");
				sentence.printContent(out);
			}
			out.append('\n');
		}
		out.append('}');
	}

}
