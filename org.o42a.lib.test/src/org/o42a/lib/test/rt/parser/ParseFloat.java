/*
    Test Framework
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
package org.o42a.lib.test.rt.parser;

import static org.o42a.core.member.MemberId.memberName;

import org.o42a.codegen.code.Code;
import org.o42a.common.adapter.FloatByString;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;


final class ParseFloat extends FloatByString {

	ParseFloat(Parser parser) {
		super(parser, "float", "parser/float.o42a");
	}

	@Override
	protected Ascendants createAscendants() {

		final Scope enclosingScope = getScope().getEnclosingScope();
		final Path ancestorPath = enclosingScope.getEnclosingScopePath().append(
				memberName("rt-float")
				.key(enclosingScope.getEnclosingScope()));

		return new Ascendants(this).setAncestor(
				ancestorPath.target(this, enclosingScope.distribute())
				.toTypeRef());
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {

		final Value<?> value = super.calculateValue(scope);

		if (!value.getLogicalValue().isTrue()) {
			return value;
		}

		return getValueType().runtimeValue();
	}

	@Override
	protected strictfp void parse(Code code, ValOp result, ObjectOp input) {
		code.debug("Run-time float");
		super.parse(code, result, input);
	}

}
