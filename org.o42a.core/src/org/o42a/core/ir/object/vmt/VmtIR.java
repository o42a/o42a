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
package org.o42a.core.ir.object.vmt;

import static org.o42a.core.ir.object.vmt.VmtIRChain.VMT_IR_CHAIN_TYPE;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public class VmtIR extends Struct<VmtIROp> {

	private static final ID SIZE_ID = ID.rawId("size");
	private static final ID TERMINATOR_ID = ID.rawId("terminator");

	public static final ID VMT_ID = ID.id("vmt");

	public static VmtIR vmtIR(ObjectIR objectIR) {

		final Obj object = objectIR.getObject();

		assert object.assertFullyResolved();

		final Generator generator = objectIR.getGenerator();
		final Obj lastDefinition = object.type().getLastDefinition();

		if (!object.is(lastDefinition)) {
			return lastDefinition.ir(generator).getVmtIR();
		}

		final VmtIR vmtIR = new VmtIR(objectIR);

		if (objectIR.isSampleDeclaration()) {
			generator.newGlobal().struct(vmtIR);
		} else {

			final VmtIR sampleVmtIR =
					objectIR.getSampleDeclaration().ir(generator).getVmtIR();
			final Content<VmtIR> content = structContent();

			generator.newGlobal().instance(
					vmtIR.getId(),
					sampleVmtIR,
					vmtIR,
					content);
		}

		return vmtIR;
	}

	private final ObjectIR objectIR;

	private Int32rec size;
	private VmtIRChain terminator;

	private VmtIR(ObjectIR objectIR) {
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
		this.terminator = data.addNewInstance(
				TERMINATOR_ID,
				VMT_IR_CHAIN_TYPE,
				new VmtTerminator(data));

		for (ObjectIRBody bodyIR : getObjectIR().bodies()) {
			for (Fld<?, ?> fld : bodyIR.getFields()) {

				final VmtRecord vmtRecord = fld.vmtRecord();

				if (vmtRecord != null) {
					vmtRecord.allocateMethods(data);
				}
			}
		}
	}

	@Override
	protected void fill() {
		size()
		.setConstant(true)
		.setLowLevel(true)
		.setValue(() -> layout(getGenerator()).size());

		for (ObjectIRBody bodyIR : getObjectIR().bodies()) {
			for (Fld<?, ?> fld : bodyIR.getFields()) {

				final VmtRecord vmtRecord = fld.vmtRecord();

				if (vmtRecord != null) {
					vmtRecord.fillMethods();
				}
			}
		}
	}

	private static final class VmtTerminator implements Content<VmtIRChain> {

		private final SubData<VmtIROp> vmt;

		VmtTerminator(SubData<VmtIROp> vmt) {
			this.vmt = vmt;
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
