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
package org.o42a.compiler.ip.file;

import static org.o42a.compiler.ip.Interpreter.PLAIN_IP;
import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.Interpreter.unwrap;
import static org.o42a.compiler.ip.SampleSpecVisitor.parseAscendants;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.type.AscendantsNode;
import org.o42a.core.Distributor;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;


final class SectionAscendantsVisitor
		extends AbstractExpressionVisitor<AscendantsDefinition, Distributor> {

	public static final
	SectionAscendantsVisitor DECLARATION_SECTION_ASCENDANTS_VISITOR =
			new SectionAscendantsVisitor(false);
	public static final
	SectionAscendantsVisitor OVERRIDER_SECTION_ASCENDANTS_VISITOR =
			new SectionAscendantsVisitor(true);

	private final boolean overrider;

	private SectionAscendantsVisitor(boolean overrider) {
		this.overrider = overrider;
	}

	@Override
	public AscendantsDefinition visitAscendants(
			AscendantsNode ascendants,
			Distributor p) {
		return parseAscendants(PLAIN_IP, ascendants, p);
	}

	@Override
	public AscendantsDefinition visitScopeRef(ScopeRefNode ref, Distributor p) {
		if (this.overrider) {
			return new AscendantsDefinition(location(p, ref), p);
		}
		return super.visitScopeRef(ref, p);
	}

	@Override
	public AscendantsDefinition visitParentheses(
			ParenthesesNode parentheses,
			Distributor p) {

		final ExpressionNode unwrapped = unwrap(parentheses);

		if (unwrapped != null) {
			return unwrapped.accept(this, p);
		}

		return super.visitParentheses(parentheses, p);
	}

	@Override
	protected AscendantsDefinition visitExpression(
			ExpressionNode expression,
			Distributor p) {

		final Ref ref = expression.accept(PLAIN_IP.derefExVisitor(), p);

		if (ref == null) {
			return null;
		}

		return new AscendantsDefinition(ref, p, ref.toTypeRef());
	}

}
