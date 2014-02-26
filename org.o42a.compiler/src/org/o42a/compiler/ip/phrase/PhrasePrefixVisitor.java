/*
    Compiler
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.type.TypeArgumentsNode;
import org.o42a.core.ref.type.TypeRefParameters;


final class PhrasePrefixVisitor
		extends AbstractExpressionVisitor<PhraseBuilder, PhraseBuilder> {

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
	public PhraseBuilder visitTypeArguments(
			TypeArgumentsNode arguments,
			PhraseBuilder p) {

		final PhraseBuilder phrase = p.prefixByTypeArguments(arguments);

		if (phrase == null) {
			return null;
		}

		return applyTypeParameters(phrase);
	}

	@Override
	protected PhraseBuilder visitExpression(
			ExpressionNode expression,
			PhraseBuilder p) {
		return p.expressionPhrase(expression, this.typeParameters);
	}

	private PhraseBuilder applyTypeParameters(PhraseBuilder phrase) {
		if (this.typeParameters == null) {
			return phrase;
		}
		return phrase.setTypeParameters(
				this.typeParameters.toObjectTypeParameters());
	}

}
