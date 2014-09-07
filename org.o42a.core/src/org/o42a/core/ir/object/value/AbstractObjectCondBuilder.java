/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.ir.object.value;

import static org.o42a.core.ir.op.CodeDirs.codeDirs;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.*;
import org.o42a.core.ir.object.ObjBuilder;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.value.ValueType;


abstract class AbstractObjectCondBuilder
		implements FunctionBuilder<ObjectCondFn> {

	@Override
	public final void build(Function<ObjectCondFn> function) {

		final Block failure = function.addBlock("failure");
		final ObjBuilder builder = createBuilder(function, failure.head());
		final ValDirs dirs =
				codeDirs(builder, function, failure.head())
				.value(getValueType(), TEMP_VAL_HOLDER);

		builder.host().value().writeTypedValue(dirs);

		final Block success = dirs.done().code();

		success.bool(true).returnValue(success);

		if (failure.exists()) {
			failure.bool(false).returnValue(failure);
		}
	}

	protected abstract ValueType<?> getValueType();

	protected abstract ObjBuilder createBuilder(
			Function<ObjectCondFn> function,
			CodePos failureDir);

}
