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
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.codegen.code.op.Int32recOp;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.ValType;
import org.o42a.core.ir.value.impl.DefaultValueIR;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.util.DataAlignment;


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
		public final void storeVal(Code code, ValOp target, Val value) {
			storeVal(code, target, value, false);
		}

		@Override
		public final void atomicStoreVal(Code code, ValOp target, Val value) {
			storeVal(code, target, value, true);
		}

		@Override
		public final void storeCopy(Code code, ValOp target, ValOp value) {
			storeCopy(code, target, value, false);
		}

		@Override
		public final void atomicStoreCopy(
				Code code,
				ValOp target,
				ValOp value) {
			storeCopy(code, target, value, true);
		}

		@Override
		public void storePtr(Code code, ValOp target, AnyOp pointer) {
			assert hasValue() :
				"Can not store value to " + getValueStruct();
			assert !hasLength() :
				"Can not store pointer without length to " + getValueStruct();
			target.value(null, code)
			.toPtr(null, code)
			.store(code, pointer);
			target.flags(null, code)
			.store(code, code.int32(Val.CONDITION_FLAG));
		}

		@Override
		public void storePtr(
				Code code,
				ValOp target,
				AnyOp pointer,
				DataAlignment alignment,
				Int32op length) {
			assert hasValue() :
				"Can not store value to " + getValueStruct();
			assert hasLength() :
				"Can not store pointer to value of scalar type: "
				+ getValueStruct();
			target.value(null, code)
			.toPtr(null, code)
			.store(code, code.nullPtr());
			target.flags(null, code)
			.store(code, code.int32(
					Val.CONDITION_FLAG | Val.EXTERNAL_FLAG
					| (alignment.getShift() << 8)));
			target.length(null, code).store(code, length);
		}

		@Override
		public void storeNull(Code code, ValOp target) {
			assert hasValue() :
				"Can not store value to " + getValueStruct();
			target.value(null, code)
			.toPtr(null, code)
			.store(code, code.nullPtr());
			target.flags(null, code)
			.store(code, code.int32(Val.CONDITION_FLAG));
			if (hasLength()) {
				target.length(null, code).store(code, code.int32(0));
			}
		}

		private void storeVal(
				Code code,
				ValOp target,
				Val value,
				boolean atomic) {

			final Int32recOp flags = target.flags(null, code);
			final Int32op flagsValue = code.int32(value.getFlags());

			if (atomic) {
				flags.atomicStore(code, flagsValue);
			} else {
				flags.store(code, flagsValue);
			}
			if (!value.getCondition()) {
				return;
			}
			if (hasLength()) {
				target.length(null, code)
				.store(code, code.int32(value.getLength()));
			}
			if (hasValue()) {

				final Ptr<AnyOp> pointer = value.getPointer();

				if (pointer != null) {
					target.value(null, code)
					.toPtr(null, code)
					.store(code, pointer.op(null, code));
				} else {
					target.rawValue(null, code).store(
							code,
							code.int64(value.getValue()));
				}
			}
		}

		private void storeCopy(
				Code code,
				ValOp target,
				ValOp value,
				boolean atomic) {

			final Int32recOp targetFlags = target.flags(null, code);
			final Int32op sourceFlags =
					value.flags(null, code).load(null, code);

			if (atomic) {
				targetFlags.atomicStore(code, sourceFlags);
			} else {
				targetFlags.store(code, sourceFlags);
			}
			if (hasLength()) {
				target.length(null, code).store(
						code,
						value.length(null, code).load(null, code));
			}
			if (hasValue()) {
				target.rawValue(null, code).store(
						code,
						value.rawValue(null, code).load(null, code));
			}
		}

	}

	private final class InitialStorageIR implements ValueStorageIR {

		@Override
		public void storeVal(Code code, ValOp target, Val value) {
			getTempStorage().storeVal(code, target, value);
		}

		@Override
		public void atomicStoreVal(Code code, ValOp target, Val value) {
			getTempStorage().atomicStoreVal(code, target, value);
		}

		@Override
		public void storeCopy(Code code, ValOp target, ValOp value) {
			getTempStorage().storeCopy(code, target, value);
			if (hasLength() && !value.ptr().getAllocClass().isStatic()) {
				target.use(code);
			}
		}

		@Override
		public void atomicStoreCopy(Code code, ValOp target, ValOp value) {
			getTempStorage().atomicStoreCopy(code, target, value);
			if (hasLength() && !value.ptr().getAllocClass().isStatic()) {
				target.use(code);
			}
		}

		@Override
		public void storePtr(Code code, ValOp target, AnyOp pointer) {
			getTempStorage().storePtr(code, target, pointer);
		}

		@Override
		public void storePtr(
				Code code,
				ValOp target,
				AnyOp pointer,
				DataAlignment alignment,
				Int32op length) {
			getTempStorage().storePtr(
					code,
					target,
					pointer,
					alignment,
					length);
			if (hasLength()) {
				target.use(code);
			}
		}

		@Override
		public void storeNull(Code code, ValOp target) {
			assert hasValue() :
				"Can not store value to " + getValueStruct();
			target.value(null, code)
			.toPtr(null, code)
			.store(code, code.nullPtr());
			target.flags(null, code)
			.store(code, code.int32(Val.CONDITION_FLAG));
		}

	}

	private final class AssignmentStorageIR implements ValueStorageIR {

		@Override
		public void storeVal(Code code, ValOp target, Val value) {
			if (hasLength()) {
				target.unuse(code);
			}
			getInitialStorage().storeVal(code, target, value);
		}

		@Override
		public void atomicStoreVal(Code code, ValOp target, Val value) {
			if (hasLength()) {
				target.unuse(code);
			}
			getInitialStorage().atomicStoreVal(code, target, value);
		}

		@Override
		public void storeCopy(Code code, ValOp target, ValOp value) {
			if (hasLength()) {
				target.unuse(code);
			}
			getInitialStorage().storeCopy(code, target, value);
		}

		@Override
		public void atomicStoreCopy(Code code, ValOp target, ValOp value) {
			if (hasLength()) {
				target.unuse(code);
			}
			getInitialStorage().atomicStoreCopy(code, target, value);
		}

		@Override
		public void storePtr(Code code, ValOp target, AnyOp pointer) {
			getInitialStorage().storePtr(code, target, pointer);
		}

		@Override
		public void storePtr(
				Code code,
				ValOp target,
				AnyOp pointer,
				DataAlignment alignment,
				Int32op length) {
			if (hasLength()) {
				target.unuse(code);
			}
			getInitialStorage().storePtr(
					code,
					target,
					pointer,
					alignment,
					length);
		}

		@Override
		public void storeNull(Code code, ValOp target) {
			if (hasLength()) {
				target.unuse(code);
			}
			getTempStorage().storeNull(code, target);
		}

	}

}
