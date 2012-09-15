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

		start(code);

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

	/**
	 * Starts the value or condition evaluation.
	 *
	 * <p>Acquires the memory barrier by default.</p>
	 *
	 * @param code the code to write the initial operations to.
	 */
	protected void start(Code code) {
		code.acquireBarrier();
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

	protected abstract void updateValue(Code code, ValOp newValue);

	protected abstract void updateCondition(Code code, boolean condition);

	private void eval(ValDirs dirs, Block code) {
		// Evaluate if value is indefinite, or false otherwise.
		loadIndefinite(code).goUnless(code, dirs.falseDir());

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
		final Block fail = code.addBlock("fail");
		final ValDirs valDirs =
				codeDirs(dirs.getBuilder(), code, fail.head()).value(dirs);

		final ValOp keeperValue = writeKeeperValue(valDirs);

		dirs.value().store(code, keeperValue);
		updateValue(code, keeperValue);
		updateCondition(code, true);

		valDirs.done();

		if (!fail.exists()) {
			return;
		}

		// Value evaluation failed. Store false.
		updateCondition(fail, false);
		fail.go(dirs.falseDir());
	}

	private void writeKeptValue(ValDirs dirs, Block code) {
		// Check the condition.
		loadCondition(code).goUnless(code, dirs.falseDir());
		// Return the value if condition is not false.
		dirs.value().store(code, loadValue(dirs, code));
	}

}
