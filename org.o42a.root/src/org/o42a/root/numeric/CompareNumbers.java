/*
    Root Object Definition
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.root.numeric;

import org.o42a.codegen.code.Code;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.type.StaticsIR;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.ValueType;
import org.o42a.root.operator.BinaryResult;
import org.o42a.util.string.ID;


abstract class CompareNumbers<P extends Number>
		extends BinaryResult<Long, P, P> {

	static final ID GT_ID = ID.id("gt");
	static final ID GREATER_ID = ID.id("greater");
	static final ID NOT_GREATER_ID = ID.id("not_greater");

	CompareNumbers(
			Obj owner,
			AnnotatedSources sources,
			ValueType<P> operandType) {
		super(owner, sources, "what", operandType, "with", operandType);
	}

	@Override
	protected final Long calculate(Resolver resolver, P left, P right) {
		return compare(left, right);
	}

	protected abstract long compare(P left, P right);

	protected ValType.Op intVal(CodeBuilder builder, Code code, long value) {

		final StaticsIR<Long> staticsIR =
				ValueType.INTEGER.ir(builder.getGenerator()).staticsIR();

		return staticsIR.valPtr(value).op(null, code);
	}

}
