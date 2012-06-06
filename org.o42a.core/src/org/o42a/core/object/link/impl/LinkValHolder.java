package org.o42a.core.object.link.impl;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Disposal;
import org.o42a.core.ir.value.ValHolder;
import org.o42a.core.ir.value.ValOp;


final class LinkValHolder extends ValHolder {

	private final ValOp value;
	private final boolean volatileHolder;
	private UnuseLinkVal disposal;

	LinkValHolder(ValOp value, boolean volatileHolder) {
		this.value = value;
		this.volatileHolder = volatileHolder;
	}

	@Override
	public void set(Code code) {
		addDisposal();
	}

	@Override
	public void hold(Code code) {
		if (!this.volatileHolder) {
			return;
		}
		this.value.useObjectPointer(code);
		addDisposal();
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		if (!this.volatileHolder) {
			return "LinkValHolder[" + this.value + ']';
		}
		return "VolatileLinkValHolder" + this.value + ']';
	}

	private void addDisposal() {
		if (this.disposal == null) {
			this.disposal = new UnuseLinkVal(this.value);
			this.value.getAllocator().allocation().addDisposal(this.disposal);
		}
	}

	private static final class UnuseLinkVal implements Disposal {

		private final ValOp value;

		UnuseLinkVal(ValOp value) {
			this.value = value;
		}

		@Override
		public void dispose(Code code) {
			this.value.unuseObjectPointer(code);
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return "UnuseLinkVal[" + this.value + ']';
		}

	}

}
