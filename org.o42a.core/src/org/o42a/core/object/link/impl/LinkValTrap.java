package org.o42a.core.object.link.impl;

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.value.ValHolder;
import org.o42a.core.ir.value.ValOp;


final class LinkValTrap extends ValHolder {

	private final ValOp value;

	public LinkValTrap(ValOp value) {
		this.value = value;
	}

	@Override
	public void set(Code code) {
	}

	@Override
	public void hold(Code code) {
		this.value.useObjectPointer(code);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return "LinkValTrap[" + this.value + ']';
	}

}
