package org.o42a.core.ir.object;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;


public class VmtIROp extends StructOp<VmtIROp> {

	VmtIROp(StructWriter<VmtIROp> writer) {
		super(writer);
	}

	@Override
	public final VmtIR getType() {
		return (VmtIR) super.getType();
	}

}
