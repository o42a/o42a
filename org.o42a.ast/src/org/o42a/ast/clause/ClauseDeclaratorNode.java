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
package org.o42a.ast.clause;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.atom.SignType;
import org.o42a.ast.statement.AbstractStatementNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.ast.statement.StatementNodeVisitor;


public class ClauseDeclaratorNode extends AbstractStatementNode {

	private final SignNode<Parenthesis> opening;
	private final ClauseKeyNode clauseKey;
	private final ReusedClauseNode[] reused;
	private final SignNode<Continuation> continuation;
	private final SignNode<Parenthesis> closing;
	private final StatementNode content;

	public ClauseDeclaratorNode(
			SignNode<Parenthesis> opening,
			ClauseKeyNode clauseKey,
			ReusedClauseNode[] reused,
			SignNode<Continuation> continuation,
			SignNode<Parenthesis> closing,
			StatementNode content) {
		super(
				opening.getStart(),
				end(
						opening,
						clauseKey,
						lastNode(reused),
						continuation,
						closing,
						content));
		this.opening = opening;
		this.clauseKey = clauseKey;
		this.reused = reused;
		this.continuation = continuation;
		this.closing = closing;
		this.content = content;
	}

	public final SignNode<Parenthesis> getOpening() {
		return this.opening;
	}

	public final ClauseKeyNode getClauseKey() {
		return this.clauseKey;
	}

	public final ReusedClauseNode[] getReused() {
		return this.reused;
	}

	public final boolean requiresContinuation() {
		return this.continuation != null;
	}

	public final SignNode<Continuation> getContinuation() {
		return this.continuation;
	}

	public final SignNode<Parenthesis> getClosing() {
		return this.closing;
	}

	public final StatementNode getContent() {
		return this.content;
	}

	@Override
	public <R, P> R accept(StatementNodeVisitor<R, P> visitor, P p) {
		return visitor.visitClauseDeclarator(this, p);
	}

	@Override
	public void printContent(StringBuilder out) {
		this.opening.printContent(out);
		if (this.clauseKey != null) {
			this.clauseKey.printContent(out);
		}
		for (ReusedClauseNode reused : this.reused) {
			reused.printContent(out);
		}
		if (this.continuation != null) {
			this.continuation.printContent(out);
		}
		if (this.closing != null) {
			this.closing.printContent(out);
		} else {
			out.append(Parenthesis.CLOSING.getSign());
		}
		if (this.content != null) {
			out.append(' ');
			this.content.printContent(out);
		}
	}

	public enum Parenthesis implements SignType {

		OPENING("<"),
		CLOSING(">");

		private final String sign;

		Parenthesis(String sign) {
			this.sign = sign;
		}

		@Override
		public String getSign() {
			return this.sign;
		}

	}

	public enum Continuation implements SignType {

		ELLIPSIS;

		@Override
		public String getSign() {
			return "...";
		}

	}

}
