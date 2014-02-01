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
package org.o42a.ast.phrase;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.expression.*;


public abstract class AbstractPhrasePartVisitor<R, P>
		implements PhrasePartNodeVisitor<R, P> {

	@Override
	public R visitName(NameNode name, P p) {
		return visitPhrasePart(name, p);
	}

	@Override
	public R visitBraces(BracesNode braces, P p) {
		return visitPhrasePart(braces, p);
	}

	@Override
	public R visitParentheses(ParenthesesNode parentheses, P p) {
		return visitPhrasePart(parentheses, p);
	}

	@Override
	public R visitBrackets(BracketsNode brackets, P p) {
		return visitPhrasePart(brackets, p);
	}

	@Override
	public R visitText(TextNode text, P p) {
		return visitPhrasePart(text, p);
	}

	@Override
	public R visitNumber(NumberNode number, P p) {
		return visitPhrasePart(number, p);
	}

	@Override
	public R visitInterval(IntervalNode interval, P p) {
		return visitPhrasePart(interval, p);
	}

	@Override
	public R visitTypeDefinition(TypeDefinitionNode definition, P p) {
		return visitPhrasePart(definition, p);
	}

	protected abstract R visitPhrasePart(PhrasePartNode part, P p);

}
