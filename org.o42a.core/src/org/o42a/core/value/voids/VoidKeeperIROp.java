package org.o42a.core.value.voids;

import static java.lang.Integer.numberOfTrailingZeros;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.ir.value.Val.VAL_INDEFINITE;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Int8recOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.state.KeeperEval;
import org.o42a.core.ir.object.state.KeeperIROp;
import org.o42a.core.ir.object.state.KeeperOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.util.string.ID;


final class VoidKeeperIROp extends KeeperIROp<VoidKeeperIROp> {

	VoidKeeperIROp(StructWriter<VoidKeeperIROp> writer) {
		super(writer);
	}

	@Override
	public final VoidKeeperIRType getType() {
		return (VoidKeeperIRType) super.getType();
	}

	public final Int8recOp flags(ID id, Code code) {
		return int8(id, code, getType().flags());
	}

	@Override
	protected void writeCond(KeeperOp keeper, CodeDirs dirs) {
		new Eval(dirs.code(), keeper, this).writeCond(dirs);
	}

	@Override
	protected ValOp writeValue(KeeperOp keeper, ValDirs dirs) {
		return new Eval(dirs.code(), keeper, this).writeValue(dirs);
	}

	@Override
	protected ObjectOp dereference(
			KeeperOp keeper,
			CodeDirs dirs,
			ObjHolder holder) {
		throw new UnsupportedOperationException();
	}

	private static final class Eval extends KeeperEval {

		private final Int8recOp flags;

		Eval(Code code, KeeperOp keeper, VoidKeeperIROp op) {
			super(keeper);
			this.flags = op.flags(null, code);
		}

		@Override
		protected BoolOp loadCondition(Code code) {
			return this.flags.load(null, code).lowestBit(null, code);
		}

		@Override
		protected BoolOp loadIndefinite(Code code) {
			return this.flags.load(null, code)
					.lshr(null, code, numberOfTrailingZeros(VAL_INDEFINITE))
					.lowestBit(null, code);
		}

		@Override
		protected ValOp loadValue(ValDirs dirs, Code code) {
			return voidValue().op(dirs.getBuilder(), code);
		}

		@Override
		protected void storeValue(Code code, ValOp newValue) {
		}

		@Override
		protected void storeCondition(Code code, boolean condition) {
			this.flags.store(
					code,
					code.int8(condition ? (byte) VAL_CONDITION : 0),
					ATOMIC);
		}

	}

}
