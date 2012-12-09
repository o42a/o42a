/*
    Parser
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.parser.grammar.type;

import static org.o42a.parser.Grammar.*;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.ref.MemberRefNode;
import org.o42a.ast.ref.MemberRefNode.Qualifier;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.type.*;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;


public class TypeParser implements Parser<TypeNode> {

	public static final TypeParser TYPE = new TypeParser();

	private TypeParser() {
	}

	@Override
	public TypeNode parse(ParserContext context) {

		final TypeNode type;
		final int c = context.next();

		switch (c) {
		case '&':
			type = context.parse(samples());
			break;
		case '#':
			type = parseMacroExpression(context);
			break;
		default:
			type = parseRef(context);
		}

		if (type == null) {
			missingInterface(context);
			return null;
		}

		final TypeParametersNode typeParameters =
				context.parse(typeParameters(type));

		if (typeParameters == null) {
			return type;
		}

		return typeParameters;
	}

	private TypeNode parseMacroExpression(ParserContext context) {

		final RefNode macroRef = context.parse(ref());

		if (macroRef == null) {
			return context.parse(macroExpansion());
		}

		final TypeNode type = parseTypeExpression(context, macroRef);

		if (type != null) {
			return type;
		}

		return macroRef;
	}

	private TypeNode parseRef(ParserContext context) {

		final RefNode ref = context.parse(ref());

		if (ref == null) {
			return null;
		}
		if (ref instanceof MemberRefNode) {

			final MemberRefNode memberRef = (MemberRefNode) ref;
			final SignNode<Qualifier> qualifier = memberRef.getQualifier();

			if (qualifier != null && qualifier.getType() == Qualifier.MACRO) {

				final TypeNode type = parseTypeExpression(context, ref);

				if (type != null) {
					return type;
				}
			}
		}

		final AscendantsNode ascendants = context.parse(ascendants(ref));

		if (ascendants != null) {
			return ascendants;
		}

		return ref;
	}

	private TypeNode parseTypeExpression(
			ParserContext context,
			RefNode prefix) {

		final ExpressionNode expression =
				context.parse(simpleExpression(prefix));

		if (expression == null) {
			return null;
		}
		if (expression instanceof TypeNode) {
			return (TypeNode) expression;
		}

		return new TypeExpressionNode(expression);
	}

	private void missingInterface(ParserContext context) {
		context.getLogger().error(
				"missing_interface",
				context.current(),
				"Interface reference is missing");
	}

}
