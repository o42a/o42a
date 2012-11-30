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

import org.o42a.ast.atom.StringNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.field.DeclarableAdapterNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;


public interface ClauseIdNodeVisitor<R, P> {

	R visitMemberRef(MemberRefNode ref, P p);

	R visitDeclarableAdapter(DeclarableAdapterNode adapter, P p);

	R visitScopeRef(ScopeRefNode ref, P p);

	R visitBrackets(BracketsNode brackets, P p);

	R visitStringLiteral(StringNode string, P p);

	R visitBraces(BracesNode braces, P p);

	R visitUnary(UnaryNode unary, P p);

	R visitBinary(BinaryNode binary, P p);

	R visitPhrase(PhraseNode phrase, P p);

}
