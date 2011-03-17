package org.o42a.codegen.data;

import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.backend.DataAllocator;
import org.o42a.codegen.data.backend.DataWriter;


final class GlobalInstanceData<O extends StructOp>
		extends AbstractInstanceData<O> {

	private final Global<O, ?> global;

	GlobalInstanceData(
			Global<O, ?> global,
			Type<O> instance,
			Content<?> content) {
		super(
				global.getGenerator(),
				global.getId().removeLocal(),
				instance,
				content);
		this.global = global;
	}

	@Override
	public Global<?, ?> getGlobal() {
		return this.global;
	}

	@Override
	public Type<?> getEnclosing() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void allocate(DataAllocator allocator) {
		setAllocation(allocator.begin(getAllocation(), this.global));
		getInstance().allocateInstance(this);
		allocator.end(this.global);
		this.content.allocated(getInstance());
		getGenerator().getGlobals().addGlobal(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void write(DataWriter writer) {
		writer.begin(getPointer().getAllocation(), this.global);
		this.content.fill(getInstance());
		writeIncluded(writer);
		writer.end(getPointer().getAllocation(), this.global);
		getGenerator().getGlobals().addType(this);
	}

}