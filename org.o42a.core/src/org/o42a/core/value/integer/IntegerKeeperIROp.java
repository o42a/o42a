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
package org.o42a.core.value.integer;

import static java.lang.Integer.numberOfTrailingZeros;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.op.CodeDirs.codeDirs;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.ir.value.Val.VAL_INDEFINITE;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CondBlock;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.Int64recOp;
import org.o42a.codegen.code.op.Int8recOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.state.KeeperIROp;
import org.o42a.core.ir.object.state.KeeperOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.string.ID;


final class IntegerKeeperIROp extends KeeperIROp<IntegerKeeperIROp> {

	IntegerKeeperIROp(StructWriter<IntegerKeeperIROp> writer) {
		super(writer);
	}

	@Override
	public final IntegerKeeperIRType getType() {
		return (IntegerKeeperIRType) super.getType();
	}

	public final Int8recOp flags(ID id, Code code) {
		return int8(id, code, getType().flags());
	}

	public final Int64recOp value(ID id, Code code) {
		return int64(id, code, getType().value());
	}

	@Override
	protected void writeCond(KeeperOp keeper, CodeDirs dirs) {

		final Ref value = keeper.getKeeper().getValue();
		final ValueStruct<?, ?> valueStruct =
				value.valueStruct(value.getScope());
		final ValDirs valDirs = dirs.value(valueStruct, TEMP_VAL_HOLDER);

		writeValue(keeper, valDirs);

		valDirs.done();
	}

	@Override
	protected ValOp writeValue(KeeperOp keeper, ValDirs dirs) {

		final Block code = dirs.code();
		final Int8recOp flags = flags(null, code);
		final Int64recOp value = value(null, code);

		code.acquireBarrier();

		final CondBlock isSet =
				flags.load(null, code)
				.lowestBit(null, code)
				.branch(code, "is_set", "not_set");

		// A non-false value is already stored in keeper.
		final ValOp value1 =
				dirs.value().store(isSet, value.load(null, isSet));

		isSet.go(code.tail());

		// Value is either indefinite or false.
		final Block notSet = isSet.otherwise();

		final CondBlock eval =
				flags.load(null, notSet)
				.lshr(null, notSet, numberOfTrailingZeros(VAL_INDEFINITE))
				.lowestBit(null, notSet)
				.branch(notSet, "eval", "false");

		// Evaluate and store the value.
		final Block fail = eval.addBlock("fail");
		final ValDirs valDirs =
				codeDirs(dirs.getBuilder(), eval, fail.head())
				.value(dirs);
		final ValOp value21 =
				keeper.getKeeper()
				.getValue()
				.op(keeper.host())
				.writeValue(valDirs);

		value.store(
				eval,
				value21.rawValue(null, eval).load(null, eval),
				ATOMIC);
		eval.releaseBarrier();
		flags.store(eval, eval.int8((byte) VAL_CONDITION), ATOMIC);

		valDirs.done().code().go(notSet.tail());

		if (fail.exists()) {
			// Value evaluation failed. Store false.
			fail.releaseBarrier();
			flags.store(fail, fail.int8((byte) 0), ATOMIC);
			fail.go(notSet.tail());
		}

		// Value is false.
		final Block falseVal = eval.otherwise();
		final ValOp value22 =
				dirs.getValueStruct()
				.falseValue()
				.op(dirs.getBuilder(), falseVal);

		falseVal.go(notSet.tail());

		// Evaluation or false result.
		final ValOp value2 = value21.phi(null, notSet, value22);

		notSet.go(code.tail());

		// Net result.
		return value1.phi(null, code, value2);
	}

	@Override
	protected ObjectOp dereference(
			KeeperOp keeper,
			CodeDirs dirs,
			ObjHolder holder) {
		throw new UnsupportedOperationException();
	}

}
