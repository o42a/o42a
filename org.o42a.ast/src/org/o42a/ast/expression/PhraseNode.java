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

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.statement.ClauseKeyNode;
import org.o42a.ast.statement.ClauseKeyNodeVisitor;


public class PhraseNode
		extends AbstractExpressionNode
		implements ClauseKeyNode {

	private final ExpressionNode prefix;
	private final ClauseNode[] clauses;

	public PhraseNode(
			ExpressionNode prefix,
			ClauseNode[] clauses) {
		super(prefix.getStart(), lastNode(clauses).getEnd());
		this.prefix = prefix;
		this.clauses = clauses;
	}

	public final ExpressionNode getPrefix() {
		return this.prefix;
	}

	public final ClauseNode[] getClauses() {
		return this.clauses;
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.prefix != null) {
			this.prefix.printContent(out);
		}

		boolean prevName = true;

		for (ClauseNode clause : this.clauses) {
			if (clause instanceof NameNode) {
				if (prevName) {
					out.append(" _ ");
				} else {
					prevName = true;
				}
			} else {
				prevName = false;
			}
			clause.printContent(out);
		}
	}

	@Override
	public <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitPhrase(this, p);
	}

	@Override
	public final <R, P> R accept(ClauseKeyNodeVisitor<R, P> visitor, P p) {
		return visitor.visitPhrase(this, p);
	}

}
