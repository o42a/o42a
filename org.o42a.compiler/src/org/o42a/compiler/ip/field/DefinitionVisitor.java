/*
    Compiler
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
package org.o42a.compiler.ip.field;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.core.member.field.FieldDefinition.impliedDefinition;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ExpressionNodeVisitor;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;


public final class DefinitionVisitor
		implements ExpressionNodeVisitor<FieldDefinition, FieldAccess> {

	private final Interpreter ip;
	private final TypeConsumer typeConsumer;

	public DefinitionVisitor(Interpreter ip, TypeConsumer typeConsumer) {
		this.ip = ip;
		this.typeConsumer = typeConsumer;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public FieldDefinition visitScopeRef(ScopeRefNode ref, FieldAccess p) {
		if (ref.getType() == ScopeType.IMPLIED) {
			return impliedDefinition(location(p, ref), p.distribute());
		}
		return visitRef(ref, p);
	}

	@Override
	public FieldDefinition visitExpression(
			ExpressionNode expression,
			FieldAccess p) {

		final Ref definition = expression.accept(
				ip().expressionVisitor(this.typeConsumer),
				p.distributeAccess());

		if (definition == null) {
			return null;
		}

		return definition.toFieldDefinition();
	}

}
