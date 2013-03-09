/*
    Compiler
    Copyright (C) 2012,2013 Ruslan Lopatin

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
import static org.o42a.compiler.ip.phrase.PhraseInterpreter.*;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.ast.expression.*;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.TypeParametersNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.phrase.part.SuffixedByPhrase;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;


final class SuffixVisitor
		extends AbstractExpressionVisitor<SuffixedByPhrase, Distributor> {

	private final PhraseInterpreter phraseIp;
	private final TypeConsumer typeConsumer;
	private final BinaryNode node;

	SuffixVisitor(
			PhraseInterpreter phraseIp,
			TypeConsumer typeConsumer,
			BinaryNode node) {
		this.phraseIp = phraseIp;
		this.typeConsumer = typeConsumer;
		this.node = node;
	}

	public final Interpreter ip() {
		return this.phraseIp.ip();
	}

	@Override
	public SuffixedByPhrase visitAscendants(
			AscendantsNode ascendants,
			Distributor p) {

		final Phrase phrase = new Phrase(
				ip(),
				location(p, this.node),
				p,
				this.typeConsumer);
		final Phrase prefixed = prefixByAscendants(phrase, ascendants);

		prefixed.declarations(emptyBlock(phrase));

		return suffixByPhrase(prefixed);
	}

	@Override
	public SuffixedByPhrase visitTypeParameters(
			TypeParametersNode parameters,
			Distributor p) {

		final Phrase phrase = new Phrase(
				ip(),
				location(p, this.node),
				p,
				this.typeConsumer);
		final Phrase prefixed = prefixByTypeParameters(phrase, parameters);

		prefixed.declarations(emptyBlock(phrase));

		return suffixByPhrase(prefixed);
	}

	@Override
	public SuffixedByPhrase visitPhrase(PhraseNode phrase, Distributor p) {

		final Phrase prefixed =
				this.phraseIp.phraseWithPrefix(phrase, p, this.typeConsumer);

		if (prefixed == null) {
			return null;
		}

		final SuffixedByPhrase result = suffixByPhrase(prefixed);

		addParts(prefixed, phrase);

		return result;
	}

	@Override
	protected SuffixedByPhrase visitExpression(
			ExpressionNode expression,
			Distributor p) {

		final Phrase phrase = new Phrase(
				ip(),
				location(p, this.node),
				p,
				this.typeConsumer);

		return suffixByPhrase(expressionPhrase(expression, phrase, null));
	}

	private SuffixedByPhrase suffixByPhrase(Phrase suffix) {

		final Ref prefix = this.node.getLeftOperand().accept(
				ip().targetExVisitor(),
				suffix.distribute());

		if (prefix == null) {
			return null;
		}

		return suffix.suffix(location(suffix, this.node.getSign()), prefix);
	}

}
