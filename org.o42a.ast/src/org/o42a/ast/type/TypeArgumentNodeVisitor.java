/*
    Abstract Syntax Tree
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.ast.type;

import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.ref.RefNodeVisitor;


public interface TypeArgumentNodeVisitor<R, P> extends RefNodeVisitor<R, P> {

	R visitAscendants(AscendantsNode ascendants, P p);

	R visitParentheses(ParenthesesNode parentheses, P p);

}