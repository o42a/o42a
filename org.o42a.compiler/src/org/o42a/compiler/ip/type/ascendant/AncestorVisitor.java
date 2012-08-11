/*
    Compiler
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.compiler.ip.type.ascendant;

import static org.o42a.compiler.ip.Interpreter.unwrap;
import static org.o42a.compiler.ip.type.ascendant.AncestorTypeRef.ancestorTypeRef;
import static org.o42a.compiler.ip.type.ascendant.AncestorTypeRef.impliedAncestorTypeRef;

import org.o42a.ast.expression.AbstractExpressionVisitor;
import org.o42a.ast.expression.ExpressionNode;
import org.o42a.ast.expression.ParenthesesNode;
import org.o42a.ast.ref.ScopeRefNode;
import org.o42a.ast.ref.ScopeType;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.compiler.ip.ref.owner.Referral;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.ValueStructFinder;


public class AncestorVisitor
		extends AbstractExpressionVisitor<AncestorTypeRef, Distributor> {

	private final Interpreter ip;
	private final ValueStructFinder valueStruct;
	private final Referral referral;
	private final TypeConsumer typeConsumer;

	public AncestorVisitor(
			Interpreter ip,
			ValueStructFinder valueStruct,
			Referral referral,
			TypeConsumer typeConsumer) {
		this.ip = ip;
		this.valueStruct = valueStruct;
		this.referral = referral;
		this.typeConsumer = typeConsumer;
	}

	public final Interpreter ip() {
		return this.ip;
	}

	public final ValueStructFinder valueStruct() {
		return this.valueStruct;
	}

	public final Referral referral() {
		return this.referral;
	}

	public final TypeConsumer typeConsumer() {
		return this.typeConsumer;
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

		final Ref ref = expression.accept(
				referral().expressionVisitor(ip(), this.typeConsumer),
				p);

		if (ref == null) {
			return null;
		}

		return ancestorTypeRef(ref.toTypeRef(valueStruct()));
	}

}
