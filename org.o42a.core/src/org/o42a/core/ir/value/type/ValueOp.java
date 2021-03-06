/*
    Compiler Core
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
package org.o42a.core.ir.value.type;

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.value.ObjectCondFn.OBJECT_COND;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ObjectCondFn;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueType;


public abstract class ValueOp<H extends ObjectOp> implements HostValueOp {

	private final ValueIR valueIR;
	private final H object;

	public ValueOp(ValueIR valueIR, H object) {
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

	public final H object() {
		return this.object;
	}

	@Override
	public final void writeCond(CodeDirs dirs) {
		if (getValueType().isVoid()) {
			writeVoidValue(dirs);
		} else {
			defaultVoid(dirs);
		}
	}

	@Override
	public final ValOp writeValue(ValDirs dirs) {
		if (dirs.getValueType().isVoid() && getValueType().isVoid()) {
			writeVoidValue(dirs.dirs());
			return getBuilder().voidVal(dirs.code());
		}
		return writeTypedValue(dirs);
	}

	public abstract ValOp writeTypedValue(ValDirs dirs);

	@Override
	public final void assign(CodeDirs dirs, HostOp value) {
		state().assign(
				dirs,
				value.materialize(dirs, tempObjHolder(dirs.getAllocator())));
	}

	public abstract StateOp<H> state();

	@Override
	public String toString() {
		return "ValueOp[" + this.object + ']';
	}

	/**
	 * Writes a void value of this object.
	 *
	 * <p>The object's value can be of any type. In this case the memory
	 * occupied by calculated value should be freed in this method. The object's
	 * condition evaluation function ({@code o42a_obj_cond}) takes care of it.
	 * </p>
	 *
	 * @param dirs code directions.
	 */
	protected abstract void writeVoidValue(CodeDirs dirs);

	protected final void defaultVoid(CodeDirs dirs) {

		final ValDirs valDirs =
				dirs.nested().value(getValueType(), TEMP_VAL_HOLDER);

		writeTypedValue(valDirs);
		valDirs.done();
	}

	protected final void defaultCond(CodeDirs dirs) {

		final Block code = dirs.code();
		final ObjectCondFn condFn =
				getGenerator()
				.externalFunction()
				.link("o42a_obj_cond", OBJECT_COND)
				.op(null, code);

		condFn.call(dirs, object());
	}

	protected final ValOp defaultValue(ValDirs dirs) {

		final DefDirs defDirs = dirs.nested().def();

		object().objectData(defDirs.code()).writeValue(defDirs);
		defDirs.done();

		return defDirs.result();
	}

}
