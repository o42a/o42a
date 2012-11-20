/*
    Compiler
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import static org.o42a.compiler.ip.phrase.PhraseInterpreter.prefixByAscendants;
import static org.o42a.compiler.ip.phrase.PhraseInterpreter.prefixByValueType;
import static org.o42a.compiler.ip.ref.owner.Referral.BODY_REFERRAL;
import static org.o42a.compiler.ip.ref.owner.Referral.TARGET_REFERRAL;
import static org.o42a.compiler.ip.type.TypeConsumer.NO_TYPE_CONSUMER;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.ast.type.TypeParametersNode;
import org.o42a.common.ref.ArbitraryTypeParameters;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.compiler.ip.type.ascendant.AncestorTypeRef;
import org.o42a.core.Distributor;


final class PhrasePrefixVisitor
		extends AbstractExpressionVisitor<Phrase, Phrase> {

	private final ArbitraryTypeParameters typeParameters;
	private final TypeConsumer typeConsumer;

	PhrasePrefixVisitor(TypeConsumer typeConsumer) {
		this.typeConsumer = typeConsumer;
		this.typeParameters = null;
	}

	PhrasePrefixVisitor(ArbitraryTypeParameters typeParameters) {
		this.typeParameters = typeParameters;
		this.typeConsumer = NO_TYPE_CONSUMER;
	}

	@Override
	public Phrase visitAscendants(AscendantsNode ascendants, Phrase p) {
		return prefixByAscendants(p, ascendants);
	}

	@Override
	public Phrase visitTypeParameters(
			TypeParametersNode parameters,
			Phrase p) {

		final Phrase result =
				prefixByValueType(p, parameters, this.typeConsumer);

		if (result == null) {
			return null;
		}

		return result.referBody();
	}

	@Override
	protected Phrase visitExpression(ExpressionNode expression, Phrase p) {

		final Distributor distributor = p.distribute();
		final AncestorTypeRef ancestor =
				expression.accept(
						p.ip().typeIp().ancestorVisitor(
								this.typeParameters,
								this.typeParameters == null
								? TARGET_REFERRAL : BODY_REFERRAL,
								this.typeConsumer),
						distributor);

		if (ancestor == null || ancestor.isImplied()) {
			return p.setImpliedAncestor(location(p, expression))
					.setTypeParameters(this.typeParameters);
		}

		final Phrase phrase;

		if (this.typeParameters != null || ancestor.isBodyReferred()) {
			phrase = p.referBody();
		} else {
			phrase = p;
		}

		final Phrase result = phrase.setAncestor(ancestor.getAncestor());

		if (!ancestor.isMacroExpanding()) {
			return result;
		}

		return result.expandMacro();
	}

}
