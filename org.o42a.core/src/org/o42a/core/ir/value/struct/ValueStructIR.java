/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.impl.DefaultValueIR;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public abstract class ValueStructIR<S extends ValueStruct<S, T>, T> {

	private final Generator generator;
	private final S valueStruct;
	private ValueStorageIR tempStorage;
	private ValueStorageIR initialStorage;
	private ValueStorageIR assignmentStorage;

	public ValueStructIR(Generator generator, S valueStruct) {
		this.generator = generator;
		this.valueStruct = valueStruct;
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final ValueType<S> getValueType() {
		return this.valueStruct.getValueType();
	}

	public final S getValueStruct() {
		return this.valueStruct;
	}

	public boolean hasValue() {
		return true;
	}

	public boolean hasLength() {
		return false;
	}

	public final ValueStorageIR getTempStorage() {
		if (this.tempStorage != null) {
			return this.tempStorage;
		}
		return this.tempStorage = createTempStorage();
	}

	public final ValueStorageIR getInitialStorage() {
		if (this.initialStorage != null) {
			return this.initialStorage;
		}
		return this.initialStorage = createInitialStorage();
	}

	public final ValueStorageIR getAssignmentStorage() {
		if (this.assignmentStorage != null) {
			return this.assignmentStorage;
		}
		return this.assignmentStorage = createTempStorage();
	}

	public abstract Val val(T value);

	public abstract Ptr<ValType.Op> valPtr(T value);

	public ValueIR valueIR(ObjectIR objectIR) {
		return new DefaultValueIR(getValueStruct(), objectIR);
	}

	@Override
	public String toString() {
		if (this.valueStruct == null) {
			return super.toString();
		}
		return this.valueStruct + " IR";
	}

	protected ValueStorageIR createTempStorage() {
		return new TempStorageIR();
	}

	protected ValueStorageIR createInitialStorage() {
		return new InitialStorageIR();
	}

	protected ValueStorageIR createAssignmentStorage() {
		return new AssignmentStorageIR();
	}

	private final class TempStorageIR implements ValueStorageIR {

		@Override
		public final ValueStructIR<?, ?> getValueStructIR() {
			return ValueStructIR.this;
		}

		@Override
		public void useVal(Code code, ValOp target, ValOp value) {
		}

		@Override
		public void unuseVal(Code code, ValOp target) {
		}

	}

	private final class InitialStorageIR implements ValueStorageIR {

		@Override
		public final ValueStructIR<?, ?> getValueStructIR() {
			return ValueStructIR.this;
		}

		@Override
		public void useVal(Code code, ValOp target, ValOp value) {
			if (hasLength()
					&& (value == null
					|| !value.ptr().getAllocClass().isStatic())) {
				target.use(code);
			}
		}

		@Override
		public void unuseVal(Code code, ValOp value) {
		}


	}

	private final class AssignmentStorageIR implements ValueStorageIR {

		@Override
		public final ValueStructIR<?, ?> getValueStructIR() {
			return ValueStructIR.this;
		}

		@Override
		public void useVal(Code code, ValOp target, ValOp value) {
			if (hasLength()
					&& (value == null
					|| !value.ptr().getAllocClass().isStatic())) {
				target.use(code);
			}
		}

		@Override
		public void unuseVal(Code code, ValOp value) {
			if (hasLength()) {
				value.unuse(code);
			}
		}

	}

}
