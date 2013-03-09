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

import static org.o42a.compiler.ip.phrase.PhraseInterpreter.expressionPhrase;
import static org.o42a.compiler.ip.phrase.PhraseInterpreter.prefixByAscendants;
import static org.o42a.compiler.ip.phrase.PhraseInterpreter.prefixByTypeParameters;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.TypeParametersNode;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.ref.type.TypeRefParameters;


final class PhrasePrefixVisitor
		extends AbstractExpressionVisitor<Phrase, Phrase> {

	static final PhrasePrefixVisitor PHRASE_PREFIX_VISITOR =
			new PhrasePrefixVisitor();

	private final TypeRefParameters typeParameters;

	private PhrasePrefixVisitor() {
		this.typeParameters = null;
	}

	PhrasePrefixVisitor(TypeRefParameters typeParameters) {
		this.typeParameters = typeParameters;
	}

	@Override
	public Phrase visitAscendants(AscendantsNode ascendants, Phrase p) {

		final Phrase phrase = prefixByAscendants(p, ascendants);

		if (phrase == null) {
			return phrase;
		}

		return applyTypeParameters(phrase);
	}

	@Override
	public Phrase visitTypeParameters(
			TypeParametersNode parameters,
			Phrase p) {

		final Phrase phrase = prefixByTypeParameters(p, parameters);

		if (phrase == null) {
			return null;
		}

		return applyTypeParameters(phrase.referBody());
	}

	@Override
	protected Phrase visitExpression(ExpressionNode expression, Phrase p) {
		return expressionPhrase(expression, p, this.typeParameters);
	}

	private Phrase applyTypeParameters(Phrase phrase) {
		if (this.typeParameters == null) {
			return phrase;
		}
		return phrase.setTypeParameters(
				this.typeParameters.toObjectTypeParameters());
	}

}
