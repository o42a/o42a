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
package org.o42a.core.ir.object.state;

import static org.o42a.core.ir.field.object.FldCtrOp.FLD_CTR_TYPE;
import static org.o42a.core.ir.op.CodeDirs.codeDirs;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CondBlock;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.core.ir.field.object.FldCtrOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.string.ID;


public abstract class KeeperEval {

	private static final ID FLD_CTR_ID = ID.id("fld_ctr");

	private final KeeperOp keeper;

	public KeeperEval(KeeperOp keeper) {
		this.keeper = keeper;
	}

	public final KeeperOp keeper() {
		return this.keeper;
	}

	public final void writeCond(CodeDirs dirs) {

		final Ref value = keeper().getKeeper().getValue();
		final ValueStruct<?, ?> valueStruct =
				value.valueStruct(value.getScope());
		final ValDirs valDirs = dirs.value(valueStruct, TEMP_VAL_HOLDER);

		writeValue(valDirs);

		valDirs.done();
	}

	public final ValOp writeValue(ValDirs dirs) {

		final ValOp value = dirs.value();
		final Block code = dirs.code();

		code.acquireBarrier();

		final CondBlock isSet =
				loadCondition(code).branch(code, "is_set", "not_set");

		// A non-false value is already stored in keeper.
		value.store(isSet, loadValue(dirs, isSet));
		isSet.go(code.tail());

		// Value is either indefinite or false.
		final Block notSet = isSet.otherwise();

		eval(dirs, notSet);
		notSet.go(code.tail());

		return value;
	}

	protected abstract BoolOp loadCondition(Code code);

	protected abstract BoolOp loadIndefinite(Code code);

	protected abstract ValOp loadValue(ValDirs dirs, Code code);

	protected ValOp writeKeeperValue(ValDirs dirs) {
		return keeper().getKeeper()
				.getValue()
				.op(keeper().host())
				.writeValue(dirs);
	}

	protected abstract void storeValue(Code code, ValOp newValue);

	protected abstract void storeCondition(Code code, boolean condition);

	private void eval(ValDirs dirs, Block code) {

		final ValOp value = dirs.value();
		final CondBlock syncEval =
				loadIndefinite(code).branch(code, "sync_eval", "false");

		syncEval(dirs, syncEval);
		syncEval.go(code.tail());

		// Value is false.
		final Block falseVal = syncEval.otherwise();

		value.storeFalse(falseVal);
		falseVal.go(code.tail());
	}

	private void syncEval(ValDirs dirs, Block code) {

		final FldCtrOp ctr =
				code.getAllocator()
				.allocation()
				.allocate(FLD_CTR_ID, FLD_CTR_TYPE);
		final CondBlock build =
				ctr.start(code, keeper()).branch(code, "build", "built");

		buildValue(dirs, build);

		ctr.finish(build, keeper());
		build.go(code.tail());

		final Block built = build.otherwise();

		writeKeptValue(dirs, built);
		built.go(code.tail());
	}

	private void buildValue(ValDirs dirs, Block code) {

		// Evaluate and store the value.
		final ValOp value = dirs.value();
		final Block fail = code.addBlock("fail");
		final ValDirs valDirs =
				codeDirs(dirs.getBuilder(), code, fail.head()).value(dirs);

		final ValOp keeperValue = writeKeeperValue(valDirs);

		value.store(code, keeperValue);
		storeValue(code, keeperValue);
		code.releaseBarrier();
		storeCondition(code, true);

		valDirs.done();

		if (!fail.exists()) {
			return;
		}

		// Value evaluation failed. Store false.
		fail.releaseBarrier();
		storeCondition(fail, false);

		value.storeFalse(fail);

		fail.go(code.tail());
	}

	private void writeKeptValue(ValDirs dirs, Block code) {

		final CondBlock hasValue =
				loadCondition(code).branch(code, "has_value", "false_value");

		final ValOp existingValue = loadValue(dirs, hasValue);

		dirs.value().store(hasValue, existingValue);
		hasValue.go(code.tail());

		final Block falseVal = hasValue.otherwise();

		dirs.value().storeFalse(falseVal);
		falseVal.go(code.tail());
	}

}
