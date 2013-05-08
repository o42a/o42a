/*
    Parser
    Copyright (C) 2011-2013 Ruslan Lopatin

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
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.MacroExpressionNode;
import org.o42a.ast.type.TypeNode;
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
			type = parseMacroExpansion(context);
			break;
		case '(':
			type = context.parse(DECLARATIVE.parentheses());
			break;
		default:
			type = parseRef(context);
		}

		if (type == null) {
			missingInterface(context);
			return null;
		}

		return type;
	}

	private TypeNode parseMacroExpansion(ParserContext context) {

		final RefNode macroRef = context.parse(ref());

		if (macroRef == null) {
			return null;
		}

		final TypeNode type = parseMacroExpression(context, macroRef);

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

		final MemberRefNode memberRef = ref.toMemberRef();

		if (memberRef != null) {

			final SignNode<Qualifier> qualifier = memberRef.getQualifier();

			if (qualifier != null && qualifier.getType() == Qualifier.MACRO) {

				final TypeNode type = parseMacroExpression(context, ref);

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

	private TypeNode parseMacroExpression(
			ParserContext context,
			RefNode prefix) {

		final ExpressionNode expression =
				context.parse(simpleExpression(prefix));

		if (expression == null) {
			return null;
		}

		final TypeNode type = expression.toType();

		if (type != null) {
			return type;
		}

		return new MacroExpressionNode(expression);
	}

	private void missingInterface(ParserContext context) {
		context.getLogger().error(
				"missing_interface",
				context.current(),
				"Interface reference is missing");
	}

}
