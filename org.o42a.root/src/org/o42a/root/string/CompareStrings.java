/*
    Root Object Definition
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
package org.o42a.root.string;

import static org.o42a.root.string.CompareFunc.COMPARE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.Int64op;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.ValueType;
import org.o42a.root.operator.BinaryResult;


@SourcePath(relativeTo = Strings.class, value = "compare.o42a")
final class CompareStrings extends BinaryResult<Long, String, String> {

	CompareStrings(Obj owner, AnnotatedSources sources) {
		super(
				owner,
				sources,
				"what",
				ValueType.STRING,
				"with",
				ValueType.STRING);
	}

	@Override
	protected Long calculate(Resolver resolver, String left, String right) {
		return Long.valueOf(left.compareTo(right));
	}

	@Override
	protected ValOp write(ValDirs dirs, ValOp leftVal, ValOp rightVal) {

		final Code code = dirs.code();
		final FuncPtr<CompareFunc> funcPtr =
				code.getGenerator()
				.externalFunction()
				.noSideEffects()
				.link("o42a_str_compare", COMPARE);
		final CompareFunc func = funcPtr.op(null, code);
		final Int64op result = func.compare(code, leftVal, rightVal);

		return dirs.value().store(code, result);
	}

}
