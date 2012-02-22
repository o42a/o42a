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
package org.o42a.ast.clause;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.*;


public abstract class AbstractClauseVisitor<R, P>
		implements ClauseNodeVisitor<R, P> {

	@Override
	public R visitName(NameNode name, P p) {
		return visitClause(name, p);
	}

	@Override
	public R visitBraces(BracesNode braces, P p) {
		return visitClause(braces, p);
	}

	@Override
	public R visitParentheses(ParenthesesNode parentheses, P p) {
		return visitClause(parentheses, p);
	}

	@Override
	public R visitBrackets(BracketsNode brackets, P p) {
		return visitClause(brackets, p);
	}

	@Override
	public R visitText(TextNode text, P p) {
		return visitClause(text, p);
	}

	protected abstract R visitClause(ClauseNode clause, P p);

}
