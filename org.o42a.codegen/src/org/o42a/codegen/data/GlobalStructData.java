package org.o42a.codegen.data;

import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


final class GlobalStructData<O extends StructOp>
		extends AbstractTypeData<O> {

	private final Global<O, ?> global;

	GlobalStructData(Global<O, ?> global, Type<O> instance) {
		super(
				global.getGenerator(),
				global.getId().removeLocal(),
				instance);
		this.global = global;
	}

	@Override
	public Global<O, ?> getGlobal() {
		return this.global;
	}

	@Override
	public Type<?> getEnclosing() {
		return null;
	}

	@Override
	public String toString() {
		return this.global.toString();
	}

	@Override
	protected DataAllocation<O> beginTypeAllocation(
			DataAllocator allocator) {
		return allocator.begin(getInstance().getAllocation(), this.global);
	}

	@Override
	protected void endTypeAllocation(DataAllocator allocator) {
		allocator.end(this.global);
	}

	@Override
	protected void postTypeAllocation() {
		super.postTypeAllocation();

		final Globals globals = getGenerator().getGlobals();

		globals.globalCreated(this);
	}

	@Override
	protected void write(DataWriter writer) {
		writer.begin(getPointer().getAllocation(), this.global);
		((Struct<O>) getInstance()).fill();
		writeIncluded(writer);
		writer.end(getPointer().getAllocation(), this.global);
	}

}