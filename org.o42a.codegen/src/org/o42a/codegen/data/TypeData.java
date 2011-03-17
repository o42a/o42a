package org.o42a.codegen.data;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


final class TypeData<O extends StructOp>
		extends AbstractTypeData<O> {

	TypeData(Generator generator, Type<O> type) {
		super(generator, type.codeId(generator).removeLocal(), type);
	}

	@Override
	public Global<?, ?> getGlobal() {
		return null;
	}

	@Override
	public Type<?> getEnclosing() {
		return null;
	}

	@Override
	protected DataAllocation<O> beginTypeAllocation(
			DataAllocator allocator) {
		return allocator.begin(getInstance());
	}

	@Override
	protected void endTypeAllocation(DataAllocator allocator) {
		allocator.end(getInstance());
	}

	@Override
	protected void write(DataWriter writer) {
		throw new UnsupportedOperationException(
				"Type " + getId() + " itself can not be written out. "
				+ "Write an instance instead.");
	}

}