/*
    Parser
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
package org.o42a.parser.grammar.field;

import static org.o42a.ast.field.DeclarationTarget.*;
import static org.o42a.parser.Grammar.iface;
import static org.o42a.parser.Grammar.initializer;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.type.InterfaceNode;
import org.o42a.ast.type.TypeParameterNode;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class DeclaratorParser implements Parser<DeclaratorNode> {

	private static final DefinitionAssignmentParser DEFINITION_ASSIGNMENT =
			new DefinitionAssignmentParser();

	private final DeclarableNode declarable;

	public DeclaratorParser(DeclarableNode declarable) {
		this.declarable = declarable;
	}

	@Override
	public DeclaratorNode parse(ParserContext context) {

		final SignNode<DeclarationTarget> definitionAssignment =
				context.parse(DEFINITION_ASSIGNMENT);

		if (definitionAssignment == null) {
			return null;
		}

		int c = context.next();
		final InterfaceNode iface;

		switch (c) {
		case '`':
			iface = context.parse(iface());
			break;
		case '(':
			iface = parseTypeParameters(context);
			break;
		default:
			iface = null;
		}

		final ExpressionNode definition = context.parse(initializer());

		if (definition == null) {
			context.getLogger().missingValue(context.current());
		}

		return new DeclaratorNode(
				this.declarable,
				definitionAssignment,
				iface,
				definition);
	}

	private InterfaceNode parseTypeParameters(ParserContext context) {

		final InterfaceNode iface = context.parse(iface());

		if (iface == null) {
			return null;
		}

		final TypeParameterNode[] parameters = iface.getParameters();

		if (parameters.length == 1) {
			return iface;
		}

		if (iface.getParameters().length > 1) {
			context.getLogger().error(
					"redundant_type_parameter",
					iface.getParameters()[1],
					"Too many type parameters");
		}

		return null;
	}

	private static final class DefinitionAssignmentParser
			implements Parser<SignNode<DeclarationTarget>> {

		@Override
		public SignNode<DeclarationTarget> parse(ParserContext context) {

			final SourcePosition start = context.current().fix();
			final boolean override;

			switch (context.next()) {
			case ':':
				if (context.next() != '=') {
					return null;
				}
				override = false;
				break;
			case '=':
				override = true;
				break;
			default:
				return null;// not a declaration
			}

			final DeclarationTarget type;

			switch (context.next()) {
			case '>':
				if (override) {
					type = OVERRIDE_PROTOTYPE;
				} else {
					type = PROTOTYPE;
				}
				context.acceptAll();
				break;
			case '<':
				if (context.next() == '>') {
					if (override) {
						type = OVERRIDE_ABSTRACT;
					} else {
						type = ABSTRACT;
					}
					context.acceptAll();
					break;
				}
				if (override) {
					type = OVERRIDE_INPUT;
				} else {
					type = INPUT;
				}
				context.acceptButLast();
				break;
			default:
				if (override) {
					type = OVERRIDE_VALUE;
				} else {
					type = VALUE;
				}
				context.acceptButLast();
			}

			final SignNode<DeclarationTarget> assignment = new SignNode<>(
					start,
					context.firstUnaccepted().fix(),
					type);

			return context.acceptComments(false, assignment);
		}

	}

}
