package org.o42a.core.value.voids;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.Int8rec;
import org.o42a.codegen.data.SubData;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.field.FldKind;
import org.o42a.core.ir.object.state.KeeperIRType;
import org.o42a.util.string.ID;


final class VoidKeeperIRType extends KeeperIRType<VoidKeeperIROp> {

	public static final VoidKeeperIRType VOID_KEEPER_IR_TYPE =
			new VoidKeeperIRType();

	private Int8rec flags;

	private VoidKeeperIRType() {
		super(ID.id("o42a_kpr_void"));
	}

	public final Int8rec flags() {
		return this.flags;
	}

	@Override
	public VoidKeeperIROp op(StructWriter<VoidKeeperIROp> writer) {
		return new VoidKeeperIROp(writer);
	}

	@Override
	protected void allocate(SubData<VoidKeeperIROp> data) {
		this.flags = data.addInt8("flags");
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0200 | FldKind.VOID_KEEPER.code());
	}

}
