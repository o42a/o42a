/*
    Compiler Core
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
package org.o42a.core.ir.value.type;

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.ValueType;


public abstract class ValueOp implements HostValueOp {

	private final ValueIR valueIR;
	private final ObjectOp object;

	public ValueOp(ValueIR valueIR, ObjectOp object) {
		this.valueIR = valueIR;
		this.object = object;
	}

	public final ValueType<?> getValueType() {
		return getValueIR().getValueType();
	}

	public final Generator getGenerator() {
		return object().getGenerator();
	}

	public final CodeBuilder getBuilder() {
		return object().getBuilder();
	}

	public final ValueIR getValueIR() {
		return this.valueIR;
	}

	public final ObjectOp object() {
		return this.object;
	}

	@Override
	public final void writeCond(CodeDirs dirs) {

		final ValDirs valDirs =
				dirs.nested().value(getValueType(), TEMP_VAL_HOLDER);

		writeValue(valDirs);
		valDirs.done();
	}

	@Override
	public abstract ValOp writeValue(ValDirs dirs);

	@Override
	public final void assign(CodeDirs dirs, HostOp value) {
		state(dirs).assign(
				dirs,
				value.materialize(dirs, tempObjHolder(dirs.getAllocator())));
	}

	public abstract StateOp state(CodeDirs dirs);

	@Override
	public String toString() {
		return "ValueOp[" + this.object + ']';
	}

	/**
	 * Checks whether the object value is known to be false.
	 *
	 * @param code code to perform the check inside of.
	 * @param falseDir position to jump to if the value is false.
	 * @param value value to check.
	 * @param flags value flags to check.
	 */
	protected void checkFalse(
			Block code,
			CodePos falseDir,
			ValType.Op value,
			ValFlagsOp flags) {
		flags.condition(null, code).goUnless(code, falseDir);
	}

	protected final ValOp defaultWrite(ValDirs dirs) {

		final DefDirs defDirs = dirs.nested().def();

		object().objectType(defDirs.code()).writeValue(defDirs);
		defDirs.done();

		return defDirs.result();
	}

}
