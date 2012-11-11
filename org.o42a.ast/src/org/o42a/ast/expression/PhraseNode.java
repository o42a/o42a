/*
    Abstract Syntax Tree
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.clause.ClauseIdNodeVisitor;
import org.o42a.ast.phrase.PhrasePartNode;


public class PhraseNode
		extends AbstractExpressionNode
		implements ClauseIdNode {

	private final ExpressionNode prefix;
	private final PhrasePartNode[] clauses;

	public PhraseNode(
			ExpressionNode prefix,
			PhrasePartNode[] clauses) {
		super(prefix.getStart(), lastNode(clauses).getEnd());
		this.prefix = prefix;
		this.clauses = clauses;
	}

	public final ExpressionNode getPrefix() {
		return this.prefix;
	}

	public final PhrasePartNode[] getClauses() {
		return this.clauses;
	}

	@Override
	public void printContent(StringBuilder out) {
		if (this.prefix != null) {
			this.prefix.printContent(out);
		}

		boolean prevName = true;

		for (PhrasePartNode clause : this.clauses) {
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
	public final <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitPhrase(this, p);
	}

	@Override
	public final <R, P> R accept(ClauseIdNodeVisitor<R, P> visitor, P p) {
		return visitor.visitPhrase(this, p);
	}

}
