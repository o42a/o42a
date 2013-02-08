/*
    Compiler
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
package org.o42a.compiler.ip.phrase;

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.member.IntervalInterpreter.invalidIntervalBracket;
import static org.o42a.compiler.ip.phrase.ArgumentVisitor.ARGUMENT_VISITOR;
import static org.o42a.compiler.ip.ref.RefInterpreter.number;
import static org.o42a.compiler.ip.st.StInterpreter.contentBuilder;
import static org.o42a.compiler.ip.type.def.TypeDefinition.typeDefinition;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.phrase.*;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.compiler.ip.st.DefaultStatementVisitor;
import org.o42a.compiler.ip.type.def.TypeDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.LocationInfo;


final class PhrasePartVisitor
		extends AbstractPhrasePartVisitor<Phrase, Phrase> {

	public static final PhrasePartVisitor PHRASE_PART_VISITOR =
			new PhrasePartVisitor();

	private PhrasePartVisitor() {
	}

	@Override
	public Phrase visitName(NameNode name, Phrase p) {
		return p.name(location(p, name), name.getName()).getPhrase();
	}

	@Override
	public Phrase visitBraces(BracesNode braces, Phrase p) {
		return p.imperative(contentBuilder(
				new DefaultStatementVisitor(p.ip(), p.getContext()),
				braces)).getPhrase();
	}

	@Override
	public Phrase visitParentheses(ParenthesesNode parentheses, Phrase p) {
		return p.declarations(contentBuilder(
				new DefaultStatementVisitor(p.ip(), p.getContext()),
				parentheses)).getPhrase();
	}

	@Override
	public Phrase visitBrackets(BracketsNode brackets, Phrase p) {

		final ArgumentNode[] arguments = brackets.getArguments();

		if (arguments.length == 0) {
			return p.emptyArgument(location(p, brackets)).getPhrase();
		}

		Phrase phrase = p;

		for (ArgumentNode arg : arguments) {

			final ExpressionNode value = arg.getValue();

			if (value != null) {
				phrase = value.accept(ARGUMENT_VISITOR, phrase);
				continue;
			}
			if (arguments.length == 1) {
				return phrase.emptyArgument(
						location(phrase, brackets)).getPhrase();
			}
			phrase = phrase.emptyArgument(location(phrase, arg)).getPhrase();
		}

		return phrase;
	}

	@Override
	public Phrase visitText(TextNode text, Phrase p) {
		if (!text.isDoubleQuoted()) {
			return p.string(location(p, text), text.getText()).getPhrase();
		}

		final Ref value = text.accept(p.ip().bodyExVisitor(), p.distribute());

		if (value != null) {
			return p.argument(value).getPhrase();
		}

		return p.emptyArgument(location(p, text)).getPhrase();
	}

	@Override
	public Phrase visitNumber(NumberNode number, Phrase p) {

		final Ref integer = number(number, p.distribute());

		if (integer == null) {
			return p;
		}

		return p.argument(integer).getPhrase();
	}

	@Override
	public Phrase visitInterval(IntervalNode interval, Phrase p) {

		final BoundNode leftBoundNode = interval.getLeftBound();
		final LocationInfo leftLocation;
		final Ref leftBound;
		final boolean leftOpen;

		if (!interval.isLeftBounded()) {
			leftBound = null;
			leftOpen = true;
			if (!interval.isLeftOpen()) {
				invalidIntervalBracket(
						p.getLogger(),
						interval.getLeftBracket());
			}
			if (leftBoundNode != null) {
				leftLocation = location(p, leftBoundNode);
			} else {
				leftLocation = location(p, interval.getEllipsis().getStart());
			}
		} else {
			leftOpen = interval.isLeftOpen();
			leftBound = leftBoundNode.toExpression().accept(
					p.ip().targetExVisitor(),
					p.distribute());
			if (leftBound != null) {
				leftLocation = leftBound;
			} else {
				leftLocation = location(p, leftBoundNode);
			}
		}

		final BoundNode rightBoundNode = interval.getRightBound();
		final LocationInfo rightLocation;
		final Ref rightBound;
		final boolean rightOpen;

		if (!interval.isRightBounded()) {
			rightBound = null;
			rightOpen = true;
			if (!interval.isRightOpen()) {
				invalidIntervalBracket(
						p.getLogger(),
						interval.getRightBracket());
			}
			if (rightBoundNode != null) {
				rightLocation = location(p, rightBoundNode);
			} else {
				rightLocation = location(p, interval.getEllipsis().getEnd());
			}
		} else {
			rightOpen = interval.isRightOpen();
			rightBound = rightBoundNode.toExpression().accept(
					p.ip().targetExVisitor(),
					p.distribute());
			if (rightBound != null) {
				rightLocation = rightBound;
			} else {
				rightLocation = location(p, rightBoundNode);
			}
		}

		if (!interval.isLeftBounded()) {
			if (!interval.isRightBounded()) {
				return p.unboundedInterval(location(p, interval)).getPhrase();
			}
			return p.halfBoundedInterval(
					location(p, interval),
					rightBound,
					rightOpen,
					false)
					.getPhrase();
		} else if (!interval.isRightBounded()) {
			return p.halfBoundedInterval(
					location(p, interval),
					leftBound,
					leftOpen,
					true)
					.getPhrase();
		}

		return p.interval(
				leftLocation,
				leftBound,
				leftOpen,
				rightLocation,
				rightBound,
				rightOpen)
				.getPhrase();
	}

	@Override
	public Phrase visitTypeDefinition(
			TypeDefinitionNode definition,
			Phrase p) {

		final TypeDefinition typeDefinition =
				typeDefinition(definition, p.getContext());

		if (typeDefinition == null) {
			return p;
		}

		return p.setTypeParameters(typeDefinition);
	}

	@Override
	protected Phrase visitPhrasePart(PhrasePartNode part, Phrase p) {
		p.getLogger().invalidClause(part);
		return p;
	}

}
