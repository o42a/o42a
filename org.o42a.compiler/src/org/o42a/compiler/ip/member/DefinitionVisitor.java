/*
    Compiler
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
package org.o42a.compiler.ip.member;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.core.member.field.FieldDefinition.arrayDefinition;
import static org.o42a.core.member.field.FieldDefinition.impliedDefinition;

import org.o42a.ast.expression.*;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;


public final class DefinitionVisitor
		extends AbstractExpressionVisitor<FieldDefinition, FieldDeclaration> {

	private final Interpreter ip;

	public DefinitionVisitor(Interpreter ip) {
		this.ip = ip;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public FieldDefinition visitScopeRef(ScopeRefNode ref, FieldDeclaration p) {
		if (ref.getType() == ScopeType.IMPLIED) {
			return impliedDefinition(location(p, ref), p.distribute());
		}
		return super.visitScopeRef(ref, p);
	}

	@Override
	public FieldDefinition visitBrackets(
			BracketsNode brackets,
			FieldDeclaration p) {

		final ArrayInitializer arrayInitializer =
				ip().arrayInitializer(p.getContext(), brackets, p);

		if (arrayInitializer == null) {
			return null;
		}

		return arrayDefinition(arrayInitializer);
	}

	@Override
	public FieldDefinition visitArray(
			ArrayNode array,
			FieldDeclaration p) {

		final ArrayInitializer arrayInitializer =
				ip().arrayInitializer(p.getContext(), array, p);

		if (arrayInitializer == null) {
			return null;
		}

		return arrayDefinition(arrayInitializer);
	}

	@Override
	protected FieldDefinition visitExpression(
			ExpressionNode expression,
			FieldDeclaration p) {

		final Ref definition =
				expression.accept(ip().expressionVisitor(), p.distribute());

		if (definition == null) {
			return null;
		}

		return definition.toFieldDefinition();
	}

}
