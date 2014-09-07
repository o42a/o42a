/*
    Root Object Definition
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.root.adapter.ToString;


@SourcePath(relativeTo = FloatValueTypeObject.class, value = "@string.o42a")
public class FloatToString extends ToString<Double> {

	public FloatToString(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	protected String convert(
			LocationInfo location,
			Resolver resolver,
			Double value) {
		return value.toString();
	}

	@Override
	protected ValOp convert(ValDirs stringDirs, ValOp value) {

		final Block code = stringDirs.code();
		final FuncPtr<FloatStringFn> fn =
				code.getGenerator().externalFunction().sideEffects().link(
						"o42a_float_to_str",
						FloatStringFn.FLOAT_STRING);

		return fn.op(null, code).convert(
				stringDirs,
				value.value(null, code).toFp64(null, code).load(null, code));
	}

}
