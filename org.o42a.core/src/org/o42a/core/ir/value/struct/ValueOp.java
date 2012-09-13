/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.ir.value.struct;

import static org.o42a.codegen.code.op.Atomicity.ACQUIRE_RELEASE;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.value.ValHolderFactory.NO_VAL_HOLDER;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.HostValueOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValFlagsOp;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public abstract class ValueOp implements HostValueOp {

	private final ValueIR valueIR;
	private final ObjectOp object;

	public ValueOp(ValueIR valueIR, ObjectOp object) {
		this.valueIR = valueIR;
		this.object = object;
	}

	public final ValueType<?> getValueType() {
		return getValueStruct().getValueType();
	}

	public final ValueStruct<?, ?> getValueStruct() {
		return getValueIR().getValueStruct();
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

	public final ValueStructIR<?, ?> getValueStructIR() {
		return getValueIR().getValueStructIR();
	}

	public final ObjectOp object() {
		return this.object;
	}

	@Override
	public final void writeCond(CodeDirs dirs) {

		final ValDirs valDirs =
				dirs.nested().value(getValueStruct(), TEMP_VAL_HOLDER);

		writeValue(valDirs);
		valDirs.done();
	}

	@Override
	public final ValOp writeValue(ValDirs dirs) {
		assert dirs.getValueType() == getValueType() :
			"Wrong value type: " + getValueType()
			+ ", but " + dirs.getValueType() + " expected";

		if (!getValueType().isStateful()) {
			return write(dirs);
		}

		final Block code = dirs.code();

		code.acquireBarrier();

		final Block definite = code.addBlock("definite");
		final ValType.Op value =
				object()
				.objectType(code)
				.ptr()
				.data(code)
				.value(code);
		final ValFlagsOp flags = value.flags(code, ATOMIC);

		flags.indefinite(null, code).goUnless(code, definite.head());

		write(dirs);
		code.dump(this + " value calculated: ", value);

		definite.dump(this + " value is definite: ", value);
		checkFalse(definite, dirs.falseDir(), value, flags);

		definite.go(code.tail());

		return value.op(null, getBuilder(), getValueStruct(), NO_VAL_HOLDER);
	}

	@Override
	public final void assign(CodeDirs dirs, HostOp value) {
		assign(
				dirs,
				value.materialize(dirs, tempObjHolder(dirs.getAllocator())));
	}

	public abstract void init(Block code, ValOp value);

	public abstract void initToFalse(Block code);

	public abstract void assign(CodeDirs dirs, ObjectOp value);

	@Override
	public String toString() {
		return "ValueOp[" + this.object + ']';
	}

	protected abstract ValOp write(ValDirs dirs);

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

	protected final void defaultInit(Block code, ValOp value) {

		final ValueStructIR<?, ?> valueStructIR = getValueStructIR();
		final ValType.Op objectValue =
				object().objectType(code).ptr().data(code).value(code);

		if (valueStructIR.hasValue()) {
			objectValue.rawValue(null, code).store(
					code,
					value.rawValue(null, code).load(null, code),
					ATOMIC);
			if (valueStructIR.hasLength()) {
				objectValue.length(null, code).store(
						code,
						value.length(null, code).load(null, code),
						ATOMIC);
			}
		}

		final Int32op valueFlags = value.flags(code).get();
		final ValFlagsOp objectValueFlags =
				objectValue.flags(code, ACQUIRE_RELEASE);

		objectValueFlags.store(code, valueFlags);
		if (valueStructIR.hasLength()) {
			objectValue.useRefCounted(code);
		}
	}

	protected final void defaultInitToFalse(Block code) {

		final ValType.Op objectValue =
				object().objectType(code).ptr().data(code).value(code);
		final ValFlagsOp flags = objectValue.flags(code, ACQUIRE_RELEASE);

		flags.storeFalse(code);
	}

	protected final ValOp defaultWrite(ValDirs dirs) {

		final DefDirs defDirs = dirs.nested().def();

		object().objectType(defDirs.code()).writeValue(defDirs);
		defDirs.done();

		return defDirs.result();
	}

}
