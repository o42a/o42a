package org.o42a.core.ir.def;

import org.o42a.core.ir.op.HostOp;
import org.o42a.util.fn.Cancelable;


final class VoidEval extends InlineEval {

	static final VoidEval INSTANCE = new VoidEval();

	private VoidEval() {
		super(null);
	}

	@Override
	public void write(DefDirs dirs, HostOp host) {
		dirs.returnValue(dirs.getBuilder().voidVal(dirs.code()));
	}

	@Override
	public String toString() {
		return "VOID";
	}

	@Override
	protected Cancelable cancelable() {
		return null;
	}

}
