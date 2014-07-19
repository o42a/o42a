/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.object.VmtIRChain.VMT_IR_CHAIN_TYPE;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public class VmtIR extends Struct<VmtIROp> {

	private static final ID TERMINATOR_ID = ID.rawId("terminator");

	static final ID VMT_ID = ID.id("vmt");

	private final ObjectIRBody bodyIR;

	private VmtIRChain terminator;

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

	public final VmtIRChain terminator() {
		return this.terminator;
	}

	@Override
	public VmtIROp op(StructWriter<VmtIROp> writer) {
		return new VmtIROp(writer);
	}

	@Override
	protected void allocate(SubData<VmtIROp> data) {
		this.terminator = data.addInstance(
				TERMINATOR_ID,
				VMT_IR_CHAIN_TYPE,
				new VmtTerminator(data));
		// TODO allocate VMT
	}

	@Override
	protected void fill() {
		// TODO fill VMT
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

	private static final class VmtTerminator implements Content<VmtIRChain> {

		private final SubData<VmtIROp> vmt;

		VmtTerminator(SubData<VmtIROp> vmt) {
			this.vmt = vmt;
		}

		@Override
		public void allocated(VmtIRChain instance) {
		}

		@Override
		public void fill(VmtIRChain instance) {
			instance.vmt()
			.setConstant(true)
			.setValue(this.vmt.getPointer().toData());
			instance.prev().setConstant(true).setNull();
		}

	}

}
