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
import static org.o42a.compiler.ip.ref.RefInterpreter.number;
import static org.o42a.compiler.ip.type.def.TypeDefinition.typeDefinition;

import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.NumberNode;
import org.o42a.ast.expression.*;
import org.o42a.ast.phrase.*;
import org.o42a.compiler.ip.type.def.TypeDefinition;
import org.o42a.core.ref.Ref;


final class PhrasePartVisitor
		extends AbstractPhrasePartVisitor<PhraseBuilder, PhraseBuilder> {

	public static final PhrasePartVisitor PHRASE_PART_VISITOR =
			new PhrasePartVisitor();

	private PhrasePartVisitor() {
	}

	@Override
	public PhraseBuilder visitName(NameNode name, PhraseBuilder p) {
		return p.name(name);
	}

	@Override
	public PhraseBuilder visitBraces(BracesNode braces, PhraseBuilder p) {
		return p.imperative(braces);
	}

	@Override
	public PhraseBuilder visitParentheses(
			ParenthesesNode parentheses,
			PhraseBuilder p) {
		return p.declarations(parentheses);
	}

	@Override
	public PhraseBuilder visitBrackets(
			BracketsNode brackets,
			PhraseBuilder p) {
		return p.arguments(brackets);
	}

	@Override
	public PhraseBuilder visitText(TextNode text, PhraseBuilder p) {
		if (!text.isDoubleQuoted()) {
			return p.string(text);
		}

		final Ref value = text.accept(p.ip().bodyExVisitor(), p.distribute());

		if (value != null) {
			return p.argument(value);
		}

		return p.emptyArgument(location(p, text));
	}

	@Override
	public PhraseBuilder visitNumber(NumberNode number, PhraseBuilder p) {

		final Ref integer = number(number, p.distribute());

		if (integer == null) {
			return p;
		}

		return p.argument(integer);
	}

	@Override
	public PhraseBuilder visitInterval(
			IntervalNode interval,
			PhraseBuilder p) {
		return p.interval(interval);
	}

	@Override
	public PhraseBuilder visitTypeDefinition(
			TypeDefinitionNode definition,
			PhraseBuilder p) {

		final TypeDefinition typeDefinition =
				typeDefinition(definition, p.getContext());

		if (typeDefinition == null) {
			return p;
		}

		return p.setTypeParameters(typeDefinition);
	}

	@Override
	protected PhraseBuilder visitPhrasePart(
			PhrasePartNode part,
			PhraseBuilder p) {
		p.getLogger().invalidClause(part);
		return p;
	}

}
