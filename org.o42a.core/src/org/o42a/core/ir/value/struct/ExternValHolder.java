/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.ir.value.struct;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Disposal;
import org.o42a.core.ir.value.ValHolder;
import org.o42a.core.ir.value.ValOp;


public final class ExternValHolder extends ValHolder {

	private final ValOp value;
	private UnuseExternVal disposal;
	private final boolean volatileHolder;

	public ExternValHolder(ValOp value, boolean volatileHolder) {
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
		this.value.useRefCounted(code);
		addDisposal();
	}

	@Override
	public String toString() {
		if (!this.volatileHolder) {
			return "ExternValHolder[" + this.value + ']';
		}
		return "VolatileExternValHolder[" + this.value + ']';
	}

	private void addDisposal() {
		if (this.disposal == null) {
			this.disposal = new UnuseExternVal(this.value);
			this.value.getAllocator().allocation().addDisposal(this.disposal);
		}
	}

	private static final class UnuseExternVal implements Disposal {

		private final ValOp value;

		public UnuseExternVal(ValOp value) {
			this.value = value;
		}

		@Override
		public void dispose(Code code) {
			this.value.unuseRefCounted(code);
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return "UnuseExternVal[" + this.value + ']';
		}

	}

}
