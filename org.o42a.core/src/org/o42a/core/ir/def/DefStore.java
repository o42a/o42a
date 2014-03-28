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
package org.o42a.core.ir.def;

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.value.ValueType;


public class DefStore {

	private final ValDirs valDirs;
	private ValOp result;
	private Code singleResultInset;
	private boolean storeInstantly;

	public DefStore(ValDirs valDirs) {
		assert valDirs != null :
			"Value directions not specified";
		this.valDirs = valDirs;
	}

	public final ValueType<?> getValueType() {
		return valDirs().getValueType();
	}

	public final ValDirs valDirs() {
		return this.valDirs;
	}

	public final ValOp value() {
		return valDirs().value();
	}

	public void store(Code code, ValOp result) {
		assert getValueType().is(result.getValueType()) :
			"Wrong value type: " + result.getValueType()
			+ ", but " + getValueType() + " expected";
		if (this.result == null
				&& valueAccessibleBy(code)
				&& !holdableValue(result)) {
			result(result);
			this.singleResultInset = code.inset("store_def");
			return;
		}
		if (!this.storeInstantly) {
			if (this.singleResultInset != null) {
				value().store(this.singleResultInset, this.result);
				this.singleResultInset = null;
			}
			this.storeInstantly = true;
			result(value());
		}
		value().store(code, result);
	}

	protected boolean valueAccessibleBy(Code code) {
		if (!valDirs().value().isStackAllocated()) {
			return true;
		}
		return code.getAllocator() == valDirs().code().getAllocator();
	}

	protected final void result(ValOp result) {
		this.result = result;
	}

	final ValOp result() {
		return this.result;
	}

	private boolean holdableValue(ValOp result) {

		final ValOp value = valDirs().value();

		if (value == result) {
			return false;
		}

		return value.holder().holdable(result);
	}

}
