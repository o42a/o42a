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

import org.o42a.ast.expression.BracesNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.module.InclusionNode;
import org.o42a.ast.module.SubTitleNode;


public interface StatementNodeVisitor<R, P>
		extends ExpressionNodeVisitor<R, P> {

	R visitBraces(BracesNode braces, P p);

	R visitAssignment(AssignmentNode assignment, P p);

	R visitSelfAssignment(SelfAssignmentNode assignment, P p);

	R visitDeclarator(DeclaratorNode declarator, P p);

	R visitClauseDeclarator(ClauseDeclaratorNode declarator, P p);

	R visitNamedBlock(NamedBlockNode block, P p);

	R visitEllipsis(EllipsisNode ellipsis, P p);

	R visitSubTitle(SubTitleNode subTitle, P p);

	R visitInclusion(InclusionNode inclusion, P p);

}
