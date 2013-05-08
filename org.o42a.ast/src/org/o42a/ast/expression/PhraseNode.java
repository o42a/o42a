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

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.clause.ClauseIdNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.phrase.PhrasePartNode;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.TypeArgumentNode;


public class PhraseNode extends AbstractExpressionNode {

	private final ExpressionNode prefix;
	private final PhrasePartNode[] parts;

	public PhraseNode(ExpressionNode prefix, PhrasePartNode... parts) {
		super(prefix.getStart(), lastNode(parts).getEnd());
		this.prefix = prefix;
		this.parts = parts;
	}

	public final ExpressionNode getPrefix() {
		return this.prefix;
	}

	public final PhrasePartNode[] getParts() {
		return this.parts;
	}

	@Override
	public final <R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p) {
		return visitor.visitPhrase(this, p);
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
		if (this.prefix != null) {
			this.prefix.printContent(out);
		}

		boolean prevName = true;

		for (PhrasePartNode clause : this.parts) {
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

}
