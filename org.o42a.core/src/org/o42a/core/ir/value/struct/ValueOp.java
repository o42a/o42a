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

import static org.o42a.core.ir.value.ValStoreMode.ASSIGNMENT_VAL_STORE;
import static org.o42a.core.ir.value.ValStoreMode.INITIAL_VAL_STORE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.CondBlock;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public abstract class ValueOp {

	private final ValueIR<?> valueIR;
	private final ObjectOp object;

	public ValueOp(ValueIR<?> valueIR, ObjectOp object) {
		this.valueIR = valueIR;
		this.object = object;
	}

	public final Generator getGenerator() {
		return object().getGenerator();
	}

	public final CodeBuilder getBuilder() {
		return object().getBuilder();
	}

	public final ValueIR<?> getValueIR() {
		return this.valueIR;
	}

	public final ValueType<?> getValueType() {
		return getValueStruct().getValueType();
	}

	public final ValueStruct<?, ?> getValueStruct() {
		return getValueIR().getValueStruct();
	}

	public final ObjectOp object() {
		return this.object;
	}

	public final void writeCond(CodeDirs dirs) {

		final ValDirs valDirs = dirs.value(getValueStruct());

		writeValue(valDirs);
		valDirs.done();
	}

	public final void writeCond(CodeDirs dirs, ObjectOp body) {
		assert body == null
				|| body.value().getValueStruct().assertIs(getValueStruct());

		final ValDirs valDirs = dirs.value(getValueStruct());

		writeValue(valDirs, body);
		valDirs.done();
	}

	public final ValOp writeValue(ValDirs dirs) {
		assert dirs.getValueType() == getValueType() :
			"Wrong value type: " + getValueType()
			+ ", but " + dirs.getValueType() + " expected";

		if (getValueType().isStateless()) {
			return writeValue(dirs, null);
		}

		final Block code = dirs.code();
		final ValOp value =
				object().objectType(code).ptr().data(code).value(code).op(
						getBuilder(),
						getValueStruct());
		final CondBlock indefinite = value.loadIndefinite(null, code).branch(
				code,
				"val_indefinite",
				"val_definite");
		final Block definite = indefinite.otherwise();

		definite.dump(this + " value is definite: ", value.ptr());
		value.go(definite, dirs);
		definite.go(code.tail());

		evaluateAndStoreValue(indefinite, value, dirs);

		indefinite.dump(this + " value calculated: ", value.ptr());
		indefinite.go(code.tail());

		return value;
	}

	public abstract ValOp writeValue(ValDirs dirs, ObjectOp body);

	public final ValOp writeClaim(ValDirs dirs) {
		return writeClaim(dirs, null);
	}

	public abstract ValOp writeClaim(ValDirs dirs, ObjectOp body);

	public final ValOp writeProposition(ValDirs dirs) {
		return writeProposition(dirs, null);
	}

	public abstract ValOp writeProposition(ValDirs dirs, ObjectOp body);

	public abstract void assign(CodeDirs dirs, ObjectOp value);

	@Override
	public String toString() {
		return "ValueOp[" + this.object + ']';
	}

	private void evaluateAndStoreValue(
			Block code,
			ValOp value,
			ValDirs resultDirs) {

		final Block falseCode = code.addBlock("eval_false");
		final ValDirs valDirs =
				getBuilder().dirs(
						code,
						falseCode.head())
				.value(value);

		value.setStoreMode(INITIAL_VAL_STORE);
		writeValue(valDirs, null);

		valDirs.done();
		value.setStoreMode(ASSIGNMENT_VAL_STORE);

		if (falseCode.exists()) {
			value.storeFalse(falseCode);
			falseCode.go(resultDirs.falseDir());
		}
	}

}
