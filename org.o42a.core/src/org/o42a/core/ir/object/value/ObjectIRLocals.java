package org.o42a.core.ir.object.value;

import java.util.ArrayList;

import org.o42a.core.ir.object.ObjectValueIR;


public final class ObjectIRLocals {

	private final ObjectValueIR valueIR;

	public ObjectIRLocals(ObjectValueIR valueIR) {
		this.valueIR = valueIR;
	}

	private ArrayList<LocalIRFunc> locals;
	private boolean filled;

	public void addLocal(LocalIRFunc local) {
		if (this.locals == null) {
			this.locals = new ArrayList<LocalIRFunc>();
			this.locals.add(local);
			this.valueIR.getObjectIR().allocate();
		} else {
			this.locals.add(local);
		}
		if (this.filled) {
			local.build();
		}
	}

	public void build() {
		if (this.locals == null) {
			return;
		}
		for (LocalIRFunc local : this.locals) {
			local.build();
		}
	}

}
