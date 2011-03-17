package org.o42a.codegen.data;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


final class StructData<O extends StructOp>
		extends AbstractTypeData<O> {

	private final Global<?, ?> global;
	private final Type<?> enclosing;

	StructData(SubData<?> enclosing, Struct<O> instance, CodeId name) {
		super(enclosing.getGenerator(), name, instance);
		this.global = enclosing.getGlobal();
		this.enclosing = enclosing.getInstance();
	}

	@Override
	public Global<?, ?> getGlobal() {
		return this.global;
	}

	@Override
	public Type<?> getEnclosing() {
		return this.enclosing;
	}

	@Override
	protected DataAllocation<O> beginTypeAllocation(
			DataAllocator allocator) {
		return allocator.enter(
				getEnclosing().getAllocation(),
				getInstance().getAllocation(),
				this);
	}

	@Override
	protected void endTypeAllocation(DataAllocator allocator) {
		allocator.exit(getEnclosing().getAllocation(), this);
	}

	@Override
	protected void write(DataWriter writer) {
		writer.enter(getPointer().getAllocation(), this);
		((Struct<O>) getInstance()).fill();
		writeIncluded(writer);
		writer.exit(getPointer().getAllocation(), this);
	}

}