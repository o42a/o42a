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
import org.o42a.codegen.data.*;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public class VmtIR extends Struct<VmtIROp> {

	private static final ID SIZE_ID = ID.rawId("size");
	private static final ID TERMINATOR_ID = ID.rawId("terminator");

	public static final ID VMT_ID = ID.id("vmt");

	private final ObjectIR objectIR;

	private Int32rec size;
	private VmtIRChain terminator;

	public VmtIR(ObjectIR objectIR) {
		super(objectIR.getId().detail(VMT_ID));
		this.objectIR = objectIR;
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final Obj getSampleDeclaration() {
		return getObjectIR().getSampleDeclaration();
	}

	public final Int32rec size() {
		return this.size;
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
		this.size = data.addInt32(SIZE_ID);
		this.terminator = data.addInstance(
				TERMINATOR_ID,
				VMT_IR_CHAIN_TYPE,
				new VmtTerminator(data));
		for (ObjectIRBody bodyIR : getObjectIR().getBodyIRs()) {
			for (Fld<?> fld : bodyIR.getDeclaredFields()) {
				fld.allocateMethods(data);
			}
		}
	}

	@Override
	protected void fill() {
		size()
		.setConstant(true)
		.setLowLevel(true).setValue(() -> layout(getGenerator()).size());
		for (ObjectIRBody bodyIR : getObjectIR().getBodyIRs()) {
			for (Fld<?> fld : bodyIR.getDeclaredFields()) {
				fld.fillMethods();
			}
		}
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
