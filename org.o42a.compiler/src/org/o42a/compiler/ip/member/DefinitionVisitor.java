/*
    Compiler
    Copyright (C) 2010 Ruslan Lopatin

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

import static org.o42a.compiler.ip.ExpressionVisitor.EXPRESSION_VISITOR;
import static org.o42a.compiler.ip.Interpreter.arrayInitializer;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.Interpreter.phrase;
import static org.o42a.compiler.ip.RefVisitor.REF_VISITOR;
import static org.o42a.core.member.field.FieldDefinition.*;

import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.member.field.FieldDeclaration;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;


public class DefinitionVisitor
		extends AbstractExpressionVisitor<FieldDefinition, FieldDeclaration> {

	public static final DefinitionVisitor DEFINITION_VISITOR =
		new DefinitionVisitor();

	private DefinitionVisitor() {
	}

	@Override
	public FieldDefinition visitScopeRef(ScopeRefNode ref, FieldDeclaration p) {
		if (ref.getType() == ScopeType.IMPLIED) {
			return defaultDefinition(location(p, ref), p.distribute());
		}
		return super.visitScopeRef(ref, p);
	}

	@Override
	public FieldDefinition visitAscendants(
			AscendantsNode ascendants,
			FieldDeclaration p) {
		if (ascendants.getAscendants().length == 1) {
			return FieldDefinition.nameDefinition(
					ascendants.getAscendants()[0].getAscendant().accept(
							REF_VISITOR,
							p.distribute()).fixScope());
		}
		return phrase(ascendants, p.distribute()).toFieldDefinition(p);
	}

	@Override
	public FieldDefinition visitPhrase(PhraseNode phrase, FieldDeclaration p) {
		return phrase(phrase, p.distribute()).toFieldDefinition(p);
	}

	@Override
	public FieldDefinition visitBrackets(
			BracketsNode brackets,
			FieldDeclaration p) {

		final ArrayInitializer arrayInitializer =
			arrayInitializer(p.getContext(), brackets, p);

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
			arrayInitializer(p.getContext(), array, p);

		if (arrayInitializer == null) {
			return null;
		}

		return arrayDefinition(arrayInitializer);
	}

	@Override
	protected FieldDefinition visitRef(RefNode ref, FieldDeclaration p) {

		final Ref definition = ref.accept(EXPRESSION_VISITOR, p.distribute());

		if (definition == null) {
			return null;
		}

		return nameDefinition(definition);
	}

	@Override
	protected FieldDefinition visitExpression(
			ExpressionNode expression,
			FieldDeclaration p) {

		final Ref definition =
			expression.accept(EXPRESSION_VISITOR, p.distribute());

		if (definition == null) {
			return null;
		}

		return valueDefinition(definition);
	}

}
