/*
    Compiler
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.AncestorTypeRef.ancestorTypeRef;
import static org.o42a.compiler.ip.AncestorTypeRef.impliedAncestorTypeRef;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.Interpreter.unwrap;

import org.o42a.ast.expression.*;
import org.o42a.ast.ref.RefNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.core.Distributor;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;


public class AncestorVisitor
		extends AbstractExpressionVisitor<AncestorTypeRef, Distributor> {

	public static AncestorTypeRef parseAncestor(
			Interpreter ip,
			AscendantsNode ascendants,
			Distributor distributor) {

		final AscendantNode firstAscendant = ascendants.getAscendants()[0];

		if (firstAscendant.getSeparator() == null) {
			return firstAscendant.getAscendant().accept(
					ip.ancestorVisitor(),
					distributor);
		}

		return firstAscendant.getAscendant().accept(
				ip.staticAncestorVisitor(),
				distributor);
	}

	public static AscendantsDefinition parseAscendants(
			Interpreter ip,
			AscendantsNode node,
			Distributor distributor) {

		AscendantsDefinition ascendants = new AscendantsDefinition(
				location(distributor, node),
				distributor);
		final AscendantNode[] ascendantNodes = node.getAscendants();
		final AncestorTypeRef ancestor = parseAncestor(ip, node, distributor);

		if (!ancestor.isImplied()) {
			ascendants = ascendants.setAncestor(ancestor.getAncestor());
		}

		for (int i = 1; i < ascendantNodes.length; ++i) {

			final RefNode sampleNode = ascendantNodes[i].getAscendant();

			if (sampleNode != null) {

				final Ref sampleRef =
						sampleNode.accept(ip.refVisitor(), distributor);

				if (sampleRef != null) {
					ascendants = ascendants.addSample(
							sampleRef.toStaticTypeRef());
				}
			}
		}

		return ascendants;
	}

	private final Interpreter ip;

	AncestorVisitor(Interpreter ip) {
		this.ip = ip;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	@Override
	public AncestorTypeRef visitParentheses(
			ParenthesesNode parentheses,
			Distributor p) {

		final ExpressionNode unwrapped = unwrap(parentheses);

		if (unwrapped != null) {
			return unwrapped.accept(this, p);
		}

		return super.visitParentheses(parentheses, p);
	}

	@Override
	public AncestorTypeRef visitScopeRef(ScopeRefNode ref, Distributor p) {
		if (ref.getType() == ScopeType.IMPLIED) {
			return impliedAncestorTypeRef();
		}
		return super.visitScopeRef(ref, p);
	}

	@Override
	protected AncestorTypeRef visitExpression(
			ExpressionNode expression,
			Distributor p) {
		return ancestorTypeRef(expression.accept(ip().expressionVisitor(), p));
	}

}
