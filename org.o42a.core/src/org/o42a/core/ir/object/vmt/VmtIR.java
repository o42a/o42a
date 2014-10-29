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

import static org.o42a.codegen.data.Struct.structContent;
import static org.o42a.core.ir.object.vmt.VmtIRChain.VMT_IR_CHAIN_TYPE;
import static org.o42a.util.fn.Init.init;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.*;
import org.o42a.core.ir.field.FldIR;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Sample;
import org.o42a.util.fn.Init;
import org.o42a.util.string.ID;


public class VmtIR {

	private static final ID SIZE_ID = ID.rawId("size");
	private static final ID TERMINATOR_ID = ID.rawId("terminator");

	public static final ID VMT_ID = ID.id("vmt");

	public static VmtIR newVmtIR(ObjectIR objectIR) {
		return new VmtIR(objectIR);
	}

	public static VmtIR deriveVmtIR(ObjectIR objectIR) {

		final Obj object = objectIR.getObject();
		final Obj lastDefinition = object.type().getLastDefinition();

		if (!object.is(lastDefinition)) {
			// Field definition is derived.
			// Use the derived VMT.
			return lastDefinition.ir(objectIR.getGenerator()).getVmtIR();
		}

		final Sample sample = object.type().getSample();

		if (sample != null) {
			// Reuse sample VMT if every VMT record is derived.
			final VmtIR sampleVmtIR =
					sample.getObject()
					.ir(objectIR.getGenerator())
					.getVmtIR();

			if (deriveVmtRecords(objectIR, sampleVmtIR)) {
				return sampleVmtIR;
			}

			return null;
		}

		// An explicitly inherited object.
		final Obj ancestor = objectIR.staticAncestor();

		if (ancestor != null) {
			// Reuse static ancestor VMT if every VMT record is inherited.
			final VmtIR ancestorVmtIR =
					ancestor.ir(objectIR.getGenerator()).getVmtIR();

			if (deriveVmtRecords(objectIR, ancestorVmtIR)) {
				return ancestorVmtIR;
			}
		}

		return null;
	}

	private static boolean deriveVmtRecords(
			ObjectIR objectIR,
			VmtIR ascendantVmtIR) {
		for (ObjectIRBody bodyIR : objectIR.bodies()) {
			for (FldIR<?, ?> fld : bodyIR.vmtFields()) {

				final VmtRecord vmtRecord = fld.vmtRecord();

				if (vmtRecord != null && !vmtRecord.derive(ascendantVmtIR)) {
					return false;
				}
			}
		}
		return true;
	}

	private final ID id;
	private final ObjectIR objectIR;
	private final Init<VmtIRStruct> instance = init(this::allocateInstance);
	private byte allocationStatus;

	private VmtIR(ObjectIR objectIR) {
		this.id = objectIR.getId().detail(VMT_ID);
		this.objectIR = objectIR;
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final VmtIR allowAllocationBy(ObjectIR objectIR) {
		if (getObjectIR() != objectIR) {
			return this;// VMT is reused by another object IR.
		}
		assert this.allocationStatus >= 0 :
			"Can not allow allocation of `" + this + "` again";
		this.allocationStatus = 1;
		return this;
	}

	public final ID getId() {
		return this.id;
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final Ptr<VmtIROp> ptr() {
		return getInstance().pointer(getGenerator());
	}

	public final void allocate() {
		getInstance();
	}

	public final Obj getSampleDeclaration() {
		return getObjectIR().getSampleDeclaration();
	}

	public final VmtIRChain terminator() {
		return getInstance().terminator();
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

	final VmtIRStruct getInstance() {
		return this.instance.get();
	}

	private VmtIRStruct allocateInstance() {
		assert this.allocationStatus >= 0 :
			"`" + this + "` already allocated";
		assert this.allocationStatus > 0:
			"Allocation of `" + this + "` is denied";

		this.allocationStatus = -1;

		final Generator generator = getGenerator();
		final VmtIRStruct struct = new VmtIRStruct(this);

		if (getObjectIR().isSampleDeclaration()) {
			generator.newGlobal().struct(struct);
			return struct;
		}

		final VmtIR sampleVmtIR =
				this.objectIR.getSampleDeclaration().ir(generator).getVmtIR();
		final Content<VmtIRStruct> content = structContent();

		generator.newGlobal().instance(
				getId(),
				sampleVmtIR.getInstance(),
				struct,
				content);

		return struct;
	}

	private void allocateVmtRecords(SubData<VmtIROp> data) {
		for (ObjectIRBody bodyIR : getObjectIR().bodies()) {
			for (FldIR<?, ?> fld : bodyIR.vmtFields()) {

				final VmtRecord vmtRecord = fld.vmtRecord();

				if (vmtRecord != null) {
					vmtRecord.allocateMethods(data);
				}
			}
		}
	}

	private void fillVmtRecords() {
		for (ObjectIRBody bodyIR : getObjectIR().bodies()) {
			for (FldIR<?, ?> fld : bodyIR.vmtFields()) {

				final VmtRecord vmtRecord = fld.vmtRecord();

				if (vmtRecord != null) {
					vmtRecord.fillMethods();
				}
			}
		}
	}

	public static final class VmtIRStruct extends Struct<VmtIROp> {

		private final VmtIR vmtIR;
		private Int32rec size;
		private VmtIRChain terminator;

		private VmtIRStruct(VmtIR vmtIR) {
			super(vmtIR.getId());
			this.vmtIR = vmtIR;
		}

		public VmtIR getVmtIR() {
			return this.vmtIR;
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
			this.vmtIR.allocateVmtRecords(data);
		}

		@Override
		protected void fill() {
			size()
			.setConstant(true)
			.setLowLevel(true)
			.setValue(() -> layout(getGenerator()).size());

			this.vmtIR.fillVmtRecords();
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
