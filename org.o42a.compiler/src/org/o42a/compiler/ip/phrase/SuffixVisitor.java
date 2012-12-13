/*
    Compiler
    Copyright (C) 2012 Ruslan Lopatin

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
import static org.o42a.compiler.ip.phrase.PhraseInterpreter.addParts;

import org.o42a.ast.expression.*;
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
	public SuffixedByPhrase visitPhrase(PhraseNode phrase, Distributor p) {

		final Ref prefix = prefix(p);

		if (prefix == null) {
			return null;
		}

		final Phrase prefixed =
				this.phraseIp.prefixedPhrase(phrase, p, this.typeConsumer);

		if (prefixed == null) {
			return null;
		}

		final SuffixedByPhrase result = suffixByPhrase(prefixed, prefix);

		addParts(prefixed, phrase);

		return result;
	}

	@Override
	protected SuffixedByPhrase visitExpression(
			ExpressionNode expression,
			Distributor p) {

		final Ref prefix = prefix(p);

		if (prefix == null) {
			return null;
		}

		final ExpressionNodeVisitor<Ref, Distributor> visitor =
				ip().targetExVisitor(this.typeConsumer);
		final Ref suffix = expression.accept(visitor, p);

		if (suffix == null) {
			return null;
		}

		final Phrase phrase = new Phrase(
				ip(),
				location(p, this.node),
				p,
				this.typeConsumer);

		phrase.setAncestor(suffix.toTypeRef());

		return suffixByPhrase(phrase, prefix);
	}

	private SuffixedByPhrase suffixByPhrase(Phrase phrase, Ref prefix) {
		return phrase.suffix(location(phrase, this.node.getSign()), prefix);
	}


	private Ref prefix(Distributor distributor) {
		return this.node.getLeftOperand().accept(
				ip().targetExVisitor(),
				distributor);
	}
}
