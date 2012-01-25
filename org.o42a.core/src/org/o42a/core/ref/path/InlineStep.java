package org.o42a.core.ref.path;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;


public abstract class InlineStep extends NormalStep {

	public abstract void after(InlineStep preceding);

	public abstract void writeLogicalValue(CodeDirs dirs, HostOp host);

	public abstract ValOp writeValue(ValDirs dirs, HostOp host);

	@Override
	public final InlineStep toInline() {
		return this;
	}

	@Override
	public final NormalAppender toAppender() {
		return null;
	}

}