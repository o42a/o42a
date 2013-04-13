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
package org.o42a.core.ir.object.state;

import static org.o42a.core.ir.field.object.FldCtrOp.FLD_CTR_TYPE;
import static org.o42a.core.ir.op.CodeDirs.codeDirs;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CondBlock;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.core.ir.field.object.FldCtrOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.string.ID;


public abstract class KeeperEval<O extends KeeperIROp<O>> {

	private static final ID FLD_CTR_ID = ID.id("fld_ctr");

	private final IndefIsFalse indefIsFalse;
	private final KeeperOp<O> keeper;

	public KeeperEval(IndefIsFalse indefIsFalse, KeeperOp<O> keeper) {
		this.indefIsFalse = indefIsFalse;
		this.keeper = keeper;
	}

	public final Generator getGenerator() {
		return keeper().getGenerator();
	}

	public final CompilerContext getContext() {
		return keeper().getContext();
	}

	public final KeeperOp<O> keeper() {
		return this.keeper;
	}

	public final void writeCond(CodeDirs dirs) {

		final Ref value = keeper().getKeeper().getValue();
		final ValDirs valDirs =
				dirs.value(value.getValueType(), TEMP_VAL_HOLDER);

		writeValue(valDirs);

		valDirs.done();
	}

	public final ValOp writeValue(ValDirs dirs) {
		return this.indefIsFalse.writeKeeperValue(this, dirs);
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

	protected enum IndefIsFalse {

		INDEF_IS_FALSE() {

			@Override
			ValOp writeKeeperValue(KeeperEval<?> eval, ValDirs dirs) {

				final ValOp value = dirs.value();
				final Block code = dirs.code();

				eval.start(code);

				final CondBlock isSet =
						eval.loadCondition(code)
						.branch(code, "is_set", "not_set");

				// A non-false value is already stored in keeper.
				value.store(isSet, eval.loadValue(dirs, isSet));
				isSet.go(code.tail());

				// Value is either indefinite or false.
				final Block notSet = isSet.otherwise();

				// Evaluate if value is indefinite, or false otherwise.
				eval.loadIndefinite(notSet).goUnless(notSet, dirs.falseDir());
				eval.eval(dirs, notSet);
				notSet.go(code.tail());

				return value;
			}

		},

		INDEF_NOT_FALSE() {

			@Override
			ValOp writeKeeperValue(KeeperEval<?> eval, ValDirs dirs) {

				final ValOp value = dirs.value();
				final Block code = dirs.code();

				eval.start(code);

				final CondBlock indef =
						eval.loadIndefinite(code)
						.branch(code, "indef", "def");
				final Block def = indef.otherwise();

				// A value is already defined.
				// Value is false.
				eval.loadCondition(def).goUnless(def, dirs.falseDir());
				// Value is not false.
				value.store(def, eval.loadValue(dirs, def));
				def.go(code.tail());

				// Value is indefinite. Evaluate it.
				eval.eval(dirs, indef);
				indef.go(code.tail());

				return value;
			}

		};

		abstract ValOp writeKeeperValue(KeeperEval<?> eval, ValDirs dirs);
	}

}
