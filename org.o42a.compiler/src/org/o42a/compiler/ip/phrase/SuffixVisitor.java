/*
    Compiler
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.ast.expression.*;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.TypeArgumentsNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.access.AccessDistributor;
import org.o42a.compiler.ip.type.TypeConsumer;


final class SuffixVisitor
		extends AbstractExpressionVisitor<PhraseBuilder, AccessDistributor> {

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
	public PhraseBuilder visitAscendants(
			AscendantsNode ascendants,
			AccessDistributor p) {

		final PhraseBuilder phrase = new PhraseBuilder(
				ip(),
				location(p, this.node),
				p,
				this.typeConsumer);

		return phrase.prefixByAscendants(ascendants)
				.noDeclarations()
				.suffix(this.node);
	}

	@Override
	public PhraseBuilder visitTypeArguments(
			TypeArgumentsNode arguments,
			AccessDistributor p) {

		final PhraseBuilder phrase = new PhraseBuilder(
				ip(),
				location(p, this.node),
				p,
				this.typeConsumer);

		return phrase.prefixByTypeArguments(arguments)
				.noDeclarations()
				.suffix(this.node);
	}

	@Override
	public PhraseBuilder visitPhrase(PhraseNode phrase, AccessDistributor p) {

		final PhraseBuilder prefixed =
				this.phraseIp.phraseWithPrefix(phrase, p, this.typeConsumer);

		if (prefixed == null) {
			return null;
		}

		return prefixed.suffix(this.node).addParts(phrase);
	}

	@Override
	protected PhraseBuilder visitExpression(
			ExpressionNode expression,
			AccessDistributor p) {

		final PhraseBuilder phrase = new PhraseBuilder(
				ip(),
				location(p, this.node),
				p,
				this.typeConsumer);

		return phrase.expressionPhrase(expression, null).suffix(this.node);
	}

}
