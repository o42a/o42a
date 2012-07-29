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
package org.o42a.compiler.ip;

import static org.o42a.compiler.ip.AncestorTypeRef.ancestorTypeRef;

import org.o42a.ast.expression.ExpressionNode;
import org.o42a.compiler.ip.ref.owner.Referral;
import org.o42a.compiler.ip.type.TypeConsumer;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.ValueStructFinder;


final class StaticAncestorVisitor extends AncestorVisitor {

	StaticAncestorVisitor(
			Interpreter ip,
			ValueStructFinder valueStructFinder,
			Referral referral,
			TypeConsumer typeConsumer) {
		super(ip, valueStructFinder, referral, typeConsumer);
	}

	@Override
	protected AncestorTypeRef visitExpression(
			ExpressionNode expression,
			Distributor p) {

		final Ref result = expression.accept(
				referral().expressionVisitor(ip(), typeConsumer()),
				p);

		if (result == null) {
			return null;
		}

		return ancestorTypeRef(result.toStaticTypeRef(valueStruct()));
	}

}
