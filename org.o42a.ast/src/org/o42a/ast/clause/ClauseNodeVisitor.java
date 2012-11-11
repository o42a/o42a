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

import org.o42a.ast.atom.DecimalNode;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.expression.*;


public interface ClauseNodeVisitor<R, P> {

	R visitName(NameNode name, P p);

	R visitBraces(BracesNode braces, P p);

	R visitParentheses(ParenthesesNode parentheses, P p);

	R visitBrackets(BracketsNode brackets, P p);

	R visitText(TextNode text, P p);

	R visitDecimal(DecimalNode decimal, P p);

	R visitTypeDefinition(TypeDefinitionNode definition, P p);

}
