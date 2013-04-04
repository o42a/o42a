/*
    Root Object Definition
    Copyright (C) 2012,2013 Ruslan Lopatin

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
import org.o42a.codegen.code.op.Fp64op;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.root.adapter.BuiltinConverter;


@SourcePath(relativeTo = IntegerValueTypeObject.class, value = "@float.o42a")
public class IntegerToFloat extends BuiltinConverter<Long, Double> {

	public IntegerToFloat(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	protected Double convert(
			LocationInfo location,
			Resolver resolver,
			Long value) {
		return value.doubleValue();
	}

	@Override
	protected ValOp convert(ValDirs targetDirs, ValOp value) {

		final Block code = targetDirs.code();
		final Fp64op floatValue =
				value.rawValue(null, code)
				.load(null, code)
				.toFp64(null, code);
		final ValOp targetValue = targetDirs.value();

		return targetValue.store(code, floatValue);
	}

}
