/*
    Compiler
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.compiler.ip.AncestorVisitor.ANCESTOR_VISITOR;
import static org.o42a.compiler.ip.AncestorVisitor.impliedAncestor;
import static org.o42a.compiler.ip.AncestorVisitor.noAncestor;
import static org.o42a.compiler.ip.ExpressionVisitor.EXPRESSION_VISITOR;
import static org.o42a.compiler.ip.Interpreter.location;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.AscendantsNode;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.TypeRef;


final class PhrasePrefixVisitor
		extends AbstractExpressionVisitor<Phrase, Phrase> {

	static final PhrasePrefixVisitor PHRASE_PREFIX_VISITOR =
		new PhrasePrefixVisitor();

	private PhrasePrefixVisitor() {
	}

	@Override
	public Phrase visitAscendants(AscendantsNode ascendants, Phrase p) {
		return PhraseInterpreter.prefix(p, ascendants);
	}

	@Override
	protected Phrase visitExpression(ExpressionNode expression, Phrase p) {

		final Distributor distributor = p.distribute();
		final TypeRef ancestor =
			expression.accept(ANCESTOR_VISITOR, distributor);

		if (ancestor == null
				|| ancestor == impliedAncestor(distributor.getContext())) {
			return p.setImpliedAncestor(location(p, expression));
		}
		if (ancestor == noAncestor(distributor.getContext())) {

			final Ref sampleRef =
				expression.accept(EXPRESSION_VISITOR, distributor);

			if (sampleRef == null) {
				return p.setImpliedAncestor(location(p, expression));
			}

			return p.addSamples(sampleRef.toStaticTypeRef());
		}

		return p.setAncestor(ancestor);
	}

}
