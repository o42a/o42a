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

import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.phrase.BoundNode;
import org.o42a.ast.statement.AssignableNode;
import org.o42a.ast.statement.StatementNode;
import org.o42a.ast.type.TypeArgumentNode;


public interface ExpressionNode
		extends StatementNode, AssignableNode, BoundNode {

	<R, P> R accept(ExpressionNodeVisitor<R, P> visitor, P p);

	DeclarableNode toDeclarable();

	TypeArgumentNode toTypeArgument();

	BinaryNode toBinary();

}
