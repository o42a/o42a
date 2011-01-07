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
package org.o42a.ast.statement;

import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.ref.ScopeRefNode;


public abstract class AbstractClauseKeyVisitor<R, P>
		extends AbstractDeclarableVisitor<R, P>
		implements ClauseKeyNodeVisitor<R, P> {

	@Override
	public R visitScopeRef(ScopeRefNode ref, P p) {
		return visitClauseKey(ref, p);
	}

	@Override
	public R visitPhrase(PhraseNode phrase, P p) {
		return visitClauseKey(phrase, p);
	}

	@Override
	protected R visitDeclarable(DeclarableNode declarable, P p) {
		return visitClauseKey(declarable, p);
	}

	protected abstract R visitClauseKey(ClauseKeyNode clauseKey, P p);

}
