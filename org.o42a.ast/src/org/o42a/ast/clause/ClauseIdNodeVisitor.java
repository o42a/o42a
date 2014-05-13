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
package org.o42a.ast.clause;

import org.o42a.ast.atom.StringNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.phrase.IntervalNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.statement.AssignmentNode;


public interface ClauseIdNodeVisitor<R, P> {

	default R visitMemberRef(MemberRefNode ref, P p) {
		return visitClauseId(ref, p);
	}

	default R visitScopeRef(ScopeRefNode ref, P p) {
		return visitClauseId(ref, p);
	}

	default R visitBrackets(BracketsNode brackets, P p) {
		return visitClauseId(brackets, p);
	}

	default R visitString(StringNode string, P p) {
		return visitClauseId(string, p);
	}

	default R visitBraces(BracesNode braces, P p) {
		return visitClauseId(braces, p);
	}

	default R visitUnary(UnaryNode unary, P p) {
		return visitClauseId(unary, p);
	}

	default R visitBinary(BinaryNode binary, P p) {
		return visitClauseId(binary, p);
	}

	default R visitAssignment(AssignmentNode assignment, P p) {
		return visitClauseId(assignment, p);
	}

	default R visitInterval(IntervalNode interval, P p) {
		return visitClauseId(interval, p);
	}

	R visitClauseId(ClauseIdNode clauseId, P p);

}
