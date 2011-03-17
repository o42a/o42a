package org.o42a.codegen.data;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


final class TypeInstanceData<O extends StructOp>
		extends AbstractInstanceData<O> {

	private final Global<?, ?> global;
	private final Type<?> enclosing;

	TypeInstanceData(
			SubData<?> enclosing,
			CodeId id,
			Type<O> instance,
			Content<?> content) {
		super(enclosing.getGenerator(), id, instance, content);
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

	@SuppressWarnings("unchecked")
	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.enter(
				getEnclosing().getAllocation(),
				getInstance().getAllocation(),
				this));
		getInstance().allocateInstance(this);
		allocator.exit(getEnclosing().getAllocation(), this);
		this.content.allocated(getInstance());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void write(DataWriter writer) {
		writer.enter(getPointer().getAllocation(), this);
		this.content.fill(getInstance());
		writeIncluded(writer);
		writer.exit(getPointer().getAllocation(), this);
	}

}