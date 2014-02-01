/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.ir.value;

import static org.o42a.codegen.code.op.Atomicity.NOT_ATOMIC;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;
import static org.o42a.core.ir.value.Val.VAL_INDEFINITE;
import static org.o42a.core.ir.value.ValHolderFactory.NO_VAL_HOLDER;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.IROp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.impl.StackAllocatedValOp;
import org.o42a.core.ir.value.type.ValueIRDesc;
import org.o42a.core.value.ValueType;
import org.o42a.util.DataAlignment;
import org.o42a.util.string.ID;


public abstract class ValOp extends IROp {

	public static final ID VALUE_ID = ID.id("value");

	public static ValOp stackAllocatedVal(
			String name,
			Allocator allocator,
			CodeBuilder builder,
			ValueType<?> valueType,
			ValHolderFactory holderFactory) {

		final ID valId =
				allocator.getId().setLocal(name != null ? name : "value");

		return new StackAllocatedValOp(
				valId,
				allocator,
				builder,
				valueType,
				holderFactory);
	}

	private final ValueType<?> valueType;
	private ValueIRDesc desc;

	public ValOp(CodeBuilder builder, ValueType<?> valueType) {
		super(builder);
		this.valueType = valueType;
		assert valueType != null :
			"Value structure not specified";
	}

	public final ValueType<?> getValueType() {
		return this.valueType;
	}

	public final ValueIRDesc getDesc() {
		if (this.desc != null) {
			return this.desc;
		}
		return this.desc = getValueType().irDesc();
	}

	public final boolean isConstant() {
		return getConstant() != null;
	}

	public boolean isStackAllocated() {
		return ptr().isAllocatedOnStack();
	}

	public abstract Val getConstant();

	public abstract Allocator getAllocator();

	@Override
	public abstract ValType.Op ptr();

	public final ValFlagsOp flags(Code code) {
		return flags(null, code);
	}

	public final ValFlagsOp flags(ID id, Code code) {
		return ptr().flags(id, code, NOT_ATOMIC);
	}

	public final Int32recOp length(ID id, Code code) {
		return ptr().length(id, code);
	}

	public Int32op loadLength(ID id, Code code) {
		return length(null, code).load(id, code);
	}

	public final Int64recOp rawValue(ID id, Code code) {
		return ptr().rawValue(id, code);
	}

	public final AnyOp value(ID id, Code code) {

		final ID valueId;

		if (id == null) {
			valueId = getId().sub("value");
		} else {
			valueId = id;
		}

		return rawValue(valueId.detail("raw"), code).toAny(valueId, code);
	}

	public final AnyOp loadData(ID id, Block code) {

		final AnyOp value =
				value(id != null ? id.detail("value") : null, code);
		final BoolOp external = flags(code).external(null, code);

		final CondBlock whenExternal = external.branch(code, "external");
		final Block notExternal = whenExternal.otherwise();

		final AnyOp result1 =
				value.toRec(null, whenExternal).load(null, whenExternal);

		whenExternal.go(code.tail());

		final AnyOp result2 = notExternal.phi(null, value);

		notExternal.go(code.tail());

		return code.phi(id, result1, result2);
	}

	public final ValOp storeVoid(Code code) {
		assert getValueType().isVoid() :
			"Can not store VOID in " + getValueType() + " value";

		flags(code).store(code, VAL_CONDITION);

		return this;
	}

	public final ValOp storeFalse(Code code) {
		flags(code).storeFalse(code);
		return this;
	}

	public final ValOp storeIndefinite(Code code) {
		flags(code).store(code, code.int32(VAL_INDEFINITE));
		return this;
	}

	public final ValOp store(Code code, Val value) {
		assert (value.getValueType() == getValueType()
				|| !value.getCondition() && value.isVoid()) :
			"Can not store " + value + " in " + this;
		assert (value.getValueType() == getValueType()
				|| !value.getCondition() && value.isVoid()) :
			"Can not store " + value + " in " + this;

		flags(code).store(code, code.int32(value.getFlags()));

		if (!value.getCondition()) {
			return this;
		}
		if (!getDesc().hasValue()) {
			return this;
		}
		if (getDesc().hasLength()) {
			length(null, code).store(code, code.int32(value.getLength()));
		}

		final Ptr<AnyOp> pointer = value.getPointer();

		if (pointer != null) {
			value(null, code)
			.toRec(null, code)
			.store(code, pointer.op(null, code));
		} else {
			rawValue(null, code).store(code, code.int64(value.getValue()));
		}

		holder().hold(code);

		return this;
	}

	public final ValOp store(Code code, ValOp value) {
		if (this == value || ptr() == value.ptr()) {
			return this;
		}

		final Val constant = value.getConstant();

		if (constant != null) {
			return store(code, constant);
		}

		assert getValueType().is(value.getValueType()) :
			"Can not store " + value + " in " + this;

		flags(code).store(code, value.flags(code).get());
		if (!getDesc().hasValue()) {
			return this;
		}
		if (getDesc().hasLength()) {
			length(null, code)
			.store(code, value.length(null, code).load(null, code));
		}
		rawValue(null, code)
		.store(code, value.rawValue(null, code).load(null, code));

		holder().hold(code);

		return this;
	}

	public final ValOp store(Code code, Int64op value) {
		assert getValueType().is(ValueType.INTEGER) :
			"Can not store integer in " + getValueType() + " value";

		rawValue(null, code).store(code, value);
		flags(code).store(code, VAL_CONDITION);

		return this;
	}

	public final ValOp store(Code code, Fp64op value) {
		assert getValueType().is(ValueType.FLOAT) :
			"Can not store floating-point number in "
			+ getValueType() + " value";

		value(null, code).toFp64(null, code).store(code, value);
		flags(code).store(code, VAL_CONDITION);

		return this;
	}

	public final ValOp storeNull(Code code) {
		assert getDesc().hasValue() :
			"Can not store value to " + getValueType();

		flags(code).store(code, VAL_CONDITION);
		if (getDesc().hasLength()) {
			length(null, code).store(code, code.int32(0));
		}
		value(null, code).toRec(null, code).store(code, code.nullPtr());

		return this;
	}

	public final ValOp store(Code code, AnyOp pointer) {
		storeNoHold(code, pointer);
		holder().hold(code);
		return this;
	}

	public final ValOp set(Code code, AnyOp pointer) {
		storeNoHold(code, pointer);
		holder().set(code);
		return this;
	}

	public final ValOp store(
			Code code,
			AnyOp pointer,
			DataAlignment alignment,
			Int32op length) {
		assert getDesc().hasValue() :
			"Can not store value to " + getValueType();
		assert getDesc().hasLength() :
			"Can not store pointer to value of scalar type: "
			+ getValueType();

		flags(code).store(
				code,
				Val.VAL_CONDITION | Val.VAL_EXTERNAL
				| (alignment.getShift() << 8));
		length(null, code).store(code, length);
		value(null, code).toRec(null, code).store(code, pointer);

		holder().hold(code);

		return this;
	}

	public final ValOp store(
			Code code,
			AnyOp pointer,
			Int32op length) {
		assert getDesc().hasValue() :
			"Can not store value to " + getValueType();
		assert getDesc().hasLength() :
			"Can not store pointer to value of scalar type: "
			+ getValueType();

		flags(code).store(code, Val.VAL_CONDITION);
		length(null, code).store(code, length);
		value(null, code).toRec(null, code).store(code, pointer);

		holder().hold(code);

		return this;
	}

	public final ValOp phi(Code code) {

		final Val constant = getConstant();

		if (constant != null) {
			return code.phi(null, ptr())
					.op(getBuilder(), constant);
		}

		return code.phi(null, ptr())
				.op(null, getBuilder(), getValueType(), NO_VAL_HOLDER);
	}

	public final ValOp phi(ID id, Code code, ValOp other) {
		return code.phi(id, ptr(), other.ptr())
				.op(null, getBuilder(), getValueType(), NO_VAL_HOLDER);
	}

	public final void go(Block code, DefDirs dirs) {
		go(code, dirs.dirs());
	}

	public final void go(Block code, ValDirs dirs) {
		go(code, dirs.dirs());
	}

	public final void go(Block code, CodeDirs dirs) {

		final Val constant = getConstant();

		if (constant != null) {
			if (!constant.getCondition()) {
				code.go(dirs.falseDir());
			}
			return;
		}

		flags(code).condition(null, code).goUnless(code, dirs.falseDir());
	}

	public abstract ValHolder holder();

	public final void useRefCounted(Code code) {
		ptr().useRefCounted(code);
	}

	public final void unuseRefCounted(Code code) {
		ptr().unuseRefCounted(code);
	}

	public final void useObjectPointer(Code code) {
		ptr().useObjectPointer(code);
	}

	public final void unuseObjectPointer(Code code) {
		ptr().unuseObjectPointer(code);
	}

	public final void useArrayPointer(Code code) {
		ptr().useArrayPointer(code);
	}

	public final void unuseArrayPointer(Code code) {
		ptr().unuseArrayPointer(code);
	}

	private final void storeNoHold(Code code, AnyOp pointer) {
		assert getDesc().hasValue() :
			"Can not store value to " + getValueType();
		assert !getDesc().hasLength() :
			"Can not store pointer without length to " + getValueType();
		value(null, code).toRec(null, code).store(code, pointer);
		flags(code).store(code, VAL_CONDITION);
	}

}
