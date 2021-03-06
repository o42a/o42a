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


public interface PhrasePartNodeVisitor<R, P> {

	default R visitName(NameNode name, P p) {
		return visitPhrasePart(name, p);
	}

	default R visitBraces(BracesNode braces, P p) {
		return visitPhrasePart(braces, p);
	}

	default R visitParentheses(ParenthesesNode parentheses, P p) {
		return visitPhrasePart(parentheses, p);
	}

	default R visitBrackets(BracketsNode brackets, P p) {
		return visitPhrasePart(brackets, p);
	}

	default R visitText(TextNode text, P p) {
		return visitPhrasePart(text, p);
	}

	default R visitNumber(NumberNode number, P p) {
		return visitPhrasePart(number, p);
	}

	default R visitInterval(IntervalNode interval, P p) {
		return visitPhrasePart(interval, p);
	}

	default R visitTypeDefinition(TypeDefinitionNode definition, P p) {
		return visitPhrasePart(definition, p);
	}

	R visitPhrasePart(PhrasePartNode part, P p);

}
