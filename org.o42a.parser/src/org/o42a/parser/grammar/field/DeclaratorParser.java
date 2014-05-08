/*
    Parser
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
package org.o42a.parser.grammar.field;

import static org.o42a.ast.field.DeclarationTarget.*;
import static org.o42a.parser.Grammar.initializer;
import static org.o42a.parser.Grammar.phrase;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.PhraseNode;
import org.o42a.ast.field.DeclarableNode;
import org.o42a.ast.field.DeclarationTarget;
import org.o42a.ast.field.DeclaratorNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.parser.Parser;
import org.o42a.parser.ParserContext;
import org.o42a.util.io.SourcePosition;


public class DeclaratorParser implements Parser<DeclaratorNode> {

	private static final DefinitionAssignmentParser DEFINITION_ASSIGNMENT =
			new DefinitionAssignmentParser();

	private final DeclarableNode declarable;
	private final boolean full;

	public DeclaratorParser(DeclarableNode declarable, boolean full) {
		this.declarable = declarable;
		this.full = full;
	}

	@Override
	public DeclaratorNode parse(ParserContext context) {

		final int next = context.next();

		if (this.full || next == ':' || next == '=') {
			return parseFullDeclaration(context);
		}

		return parseShortDeclaration(context);
	}

	private DeclaratorNode parseFullDeclaration(ParserContext context) {

		final SignNode<DeclarationTarget> definitionAssignment =
				context.parse(DEFINITION_ASSIGNMENT);

		if (definitionAssignment == null) {
			return null;
		}

		final ExpressionNode definition = context.parse(initializer());

		if (definition == null) {
			context.getLogger().missingValue(context.current());
		}

		return new DeclaratorNode(
				this.declarable,
				definitionAssignment,
				definition);
	}

	private DeclaratorNode parseShortDeclaration(ParserContext context) {

		final ScopeRefNode impliedRef = new ScopeRefNode(
				this.declarable.getStart(),
				this.declarable.getEnd(),
				ScopeType.IMPLIED);
		final PhraseNode phrase = context.parse(phrase(impliedRef));

		if (phrase == null) {
			context.acceptButLast();
		}

		return new DeclaratorNode(this.declarable, null, phrase);
	}

	private static final class DefinitionAssignmentParser
			implements Parser<SignNode<DeclarationTarget>> {

		@Override
		public SignNode<DeclarationTarget> parse(ParserContext context) {

			final SourcePosition start = context.current().fix();
			final boolean override;
			final boolean staticField;

			switch (context.next()) {
			case '=':
				override = true;
				staticField = false;
				break;
			case ':':
				override = false;
				switch (context.next()) {
				case '=':
					staticField = false;
					break;
				case '-':
					context.acceptAll();
					return result(context, start, DeclarationTarget.ALIAS);
				case ':':
					staticField = true;
					if (context.next() != '=') {
						return null;
					}
					break;
				default:
					return null;
				}
				break;
			default:
				return null;// Not a declaration.
			}

			final DeclarationTarget target =
					parseTarget(context, override, staticField);

			if (target == null) {
				return null;
			}

			return result(context, start, target);
		}

		private SignNode<DeclarationTarget> result(
				ParserContext context,
				final SourcePosition start,
				final DeclarationTarget target) {

			final SignNode<DeclarationTarget> assignment = new SignNode<>(
					start,
					context.firstUnaccepted().fix(),
					target);

			return context.acceptComments(false, assignment);
		}

		private DeclarationTarget parseTarget(
				ParserContext context,
				boolean override,
				boolean staticField) {

			final DeclarationTarget target;

			switch (context.next()) {
			case '>':
				if (override) {
					target = OVERRIDE_PROTOTYPE;
				} else if (staticField) {
					target = STATIC_PROTOTYPE;
				} else {
					target = PROTOTYPE;
				}
				context.acceptAll();
				break;
			case '<':
				if (staticField) {
					return null;// Static field can not be abstract.
				}
				if (context.next() == '>') {
					if (override) {
						target = OVERRIDE_ABSTRACT;
					} else {
						target = ABSTRACT;
					}
					context.acceptAll();
					break;
				}
				if (override) {
					target = OVERRIDE_INPUT;
				} else {
					target = INPUT;
				}
				context.acceptButLast();
				break;
			default:
				if (override) {
					target = OVERRIDE_VALUE;
				} else if (staticField) {
					target = STATIC;
				} else {
					target = VALUE;
				}
				context.acceptButLast();
			}

			return target;
		}

	}

}
