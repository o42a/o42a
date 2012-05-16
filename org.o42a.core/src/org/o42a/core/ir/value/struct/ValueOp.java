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

import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.value.ValUseFunc.VAL_USE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.Int32recOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.ValUseFunc;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public abstract class ValueOp {

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

	public final void writeCond(CodeDirs dirs) {

		final ValDirs valDirs = dirs.nested().value(getValueStruct());

		writeValue(valDirs);
		valDirs.done();
	}

	public final ValOp writeValue(ValDirs dirs) {
		assert dirs.getValueType() == getValueType() :
			"Wrong value type: " + getValueType()
			+ ", but " + dirs.getValueType() + " expected";

		if (getValueType().isStateless()) {
			return write(dirs);
		}

		final Block code = dirs.code();

		code.acquireBarrier();

		final Block definite = code.addBlock("definite");
		final ValOp value =
				object()
				.objectType(code)
				.ptr()
				.data(code)
				.value(code)
				.op(getBuilder(), getValueStruct());

		value.loadIndefinite(null, code, ATOMIC)
		.goUnless(code, definite.head());
		write(dirs);
		code.dump(this + " value calculated: ", value);

		definite.dump(this + " value is definite: ", value);
		value.go(definite, dirs);
		definite.go(code.tail());

		return value;
	}

	public abstract void init(Block code, ValOp value);

	public abstract void initToFalse(Block code);

	public abstract void assign(CodeDirs dirs, ObjectOp value);

	@Override
	public String toString() {
		return "ValueOp[" + this.object + ']';
	}

	protected abstract ValOp write(ValDirs dirs);

	protected final void defaultInit(Block code, ValOp value) {

		final ValueStructIR<?, ?> valueStructIR = getValueStructIR();
		final ValType.Op objectVal =
				object()
				.objectType(code)
				.ptr()
				.data(code).value(code);

		if (valueStructIR.hasValue()) {
			objectVal.rawValue(null, code).store(
					code,
					value.rawValue(null, code).load(null, code),
					ATOMIC);
			if (valueStructIR.hasLength()) {
				objectVal.length(null, code).store(
						code,
						value.length(null, code).load(null, code),
						ATOMIC);
			}
		}

		final Int32recOp flags = objectVal.flags(null, code);

		code.releaseBarrier();

		flags.store(
				code,
				value.flags(null, code).load(null, code),
				ATOMIC);
		if (valueStructIR.hasLength()) {
			use(code, objectVal);
		}
	}

	protected final void defaultInitToFalse(Block code) {

		final ValType.Op objectVal =
				object()
				.objectType(code)
				.ptr()
				.data(code).value(code);
		final Int32recOp flags = objectVal.flags(null, code);

		code.releaseBarrier();

		flags.store(code, code.int32(0), ATOMIC);
	}

	protected final ValOp defaultWrite(ValDirs dirs) {

		final DefDirs defDirs = dirs.nested().def();

		object().objectType(defDirs.code()).writeValue(defDirs);
		defDirs.done();

		return defDirs.result();
	}

	public static final void use(Code code, ValType.Op value) {

		final FuncPtr<ValUseFunc> func =
				code.getGenerator()
				.externalFunction()
				.link("o42a_val_use", VAL_USE);

		func.op(null, code).call(code, value);
	}

	public static final void unuse(Code code, ValType.Op value) {

		final FuncPtr<ValUseFunc> func =
				code.getGenerator()
				.externalFunction()
				.link("o42a_val_unuse", VAL_USE);

		func.op(null, code).call(code, value);
	}

}
