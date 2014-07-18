package org.o42a.core.ir.object;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public class VmtIR extends Struct<VmtIROp> {

	static final ID VMT_ID = ID.id("vmt");

	private final ObjectIRBody bodyIR;

	public VmtIR(ObjectIRBody bodyIR) {
		super(buildId(bodyIR));
		this.bodyIR = bodyIR;
	}

	public final ObjectIRBody getBodyIR() {
		return this.bodyIR;
	}

	public final ObjectIR getObjectIR() {
		return getBodyIR().getObjectIR();
	}

	public final Obj getSampleDeclaration() {
		return getBodyIR().getSampleDeclaration();
	}

	@Override
	public VmtIROp op(StructWriter<VmtIROp> writer) {
		return new VmtIROp(writer);
	}

	@Override
	protected void allocate(SubData<VmtIROp> data) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fill() {
		// TODO Auto-generated method stub

	}

	private static ID buildId(ObjectIRBody bodyIR) {

		final ID prefix = bodyIR.getObjectIR().getId().detail(VMT_ID);

		if (bodyIR.isMain()) {
			return prefix;
		}

		return prefix.detail(
				bodyIR.getSampleDeclaration()
				.ir(bodyIR.getGenerator())
				.getId());
	}

}
