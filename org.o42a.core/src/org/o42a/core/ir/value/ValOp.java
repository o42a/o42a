/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static java.lang.Integer.numberOfTrailingZeros;
import static org.o42a.codegen.code.op.Atomicity.ATOMIC;
import static org.o42a.codegen.code.op.Atomicity.NOT_ATOMIC;
import static org.o42a.core.ir.value.Val.*;
import static org.o42a.core.ir.value.ValStoreMode.ASSIGNMENT_VAL_STORE;
import static org.o42a.core.ir.value.ValStoreMode.TEMP_VAL_STORE;
import static org.o42a.core.ir.value.ValType.VAL_TYPE;
import static org.o42a.core.ir.value.ValUseFunc.VAL_USE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Ptr;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.IROp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.impl.StackAllocatedValOp;
import org.o42a.core.ir.value.struct.ValueStorageIR;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.util.DataAlignment;


public abstract class ValOp extends IROp {

	public static ValOp stackAllocatedVal(
			String name,
			AllocationCode code,
			CodeBuilder builder,
			ValueStruct<?, ?> valueStruct) {

		final CodeId valId =
				code.getId().setLocal(name != null ? name : "value");

		return new StackAllocatedValOp(valId, code, builder, valueStruct);
	}

	private final ValueStruct<?, ?> valueStruct;
	private ValStoreMode storeMode = ASSIGNMENT_VAL_STORE;

	public ValOp(CodeBuilder builder, ValueStruct<?, ?> valueStruct) {
		super(builder);
		this.valueStruct = valueStruct;
	}

	public final ValueType<?> getValueType() {

		final ValueStruct<?, ?> valueStruct = getValueStruct();

		return valueStruct != null ? valueStruct.getValueType() : null;
	}

	public final ValueStruct<?, ?> getValueStruct() {
		return this.valueStruct;
	}

	public final boolean isConstant() {
		return getConstant() != null;
	}

	public boolean isStackAllocated() {
		return ptr().isAllocatedOnStack();
	}

	public abstract Val getConstant();

	@Override
	public abstract ValType.Op ptr();

	public final ValStoreMode getStoreMode() {
		if (isStackAllocated()) {
			return TEMP_VAL_STORE;
		}
		return this.storeMode;
	}

	public final ValOp setStoreMode(ValStoreMode storeMode) {
		assert storeMode != null :
			"Value store mode not specified";
		this.storeMode = storeMode;
		return this;
	}

	public final Int32recOp flags(CodeId id, Code code) {
		return ptr().flags(id, code);
	}

	public final BoolOp loadCondition(CodeId id, Code code) {

		final Int32op flags = flags(null, code).load(null, code);

		return flags.lowestBit(
				id != null ? id : getId().sub("condition_flag"),
				code);
	}

	public final BoolOp loadIndefinite(CodeId id, Code code) {
		return loadIndefinite(id, code, Atomicity.NOT_ATOMIC);
	}

	public final BoolOp loadIndefinite(
			CodeId id,
			Code code,
			Atomicity atomicity) {
		return loadFlag(id, "indefinite", code, INDEFINITE_FLAG, atomicity);
	}

	public final BoolOp loadExternal(CodeId id, Code code) {
		return loadFlag(id, "external", code, EXTERNAL_FLAG, NOT_ATOMIC);
	}

	public final BoolOp loadStatic(CodeId id, Code code) {
		return loadFlag(id, "static", code, STATIC_FLAG, NOT_ATOMIC);
	}

	public Int32op loadAlignmentShift(CodeId id, Code code) {

		final CodeId ashiftId;

		if (id == null) {
			ashiftId = getId().sub("alignment_shift");
		} else {
			ashiftId = id;
		}

		final Int32op flags = flags(null, code).load(null, code);
		final Int32op ualignment = flags.and(
				ashiftId.detail("ush"),
				code,
				code.int32(ALIGNMENT_MASK));

		return ualignment.lshr(
				ashiftId,
				code,
				numberOfTrailingZeros(ALIGNMENT_MASK));
	}

	public Int32op loadAlignment(CodeId id, Code code) {

		final Int32op shift = loadAlignmentShift(
				id != null ? id.detail("shift") : null,
				code);

		return code.int32(1).shl(
				id != null ? id : getId().sub("alignment"),
				code,
				shift);
	}

	public Int32op loadCharMask(CodeId id, Block code) {

		final Int32op alignment =
				loadAlignment(id != null ? id.detail("alignment") : null, code);
		final BoolOp is4bytes = alignment.ge(
				alignment.getId().detail("4bytes"),
				code,
				code.int32(4));
		final CondBlock when4bytes = is4bytes.branch(code, "4bytes");
		final Block not4bytes = when4bytes.otherwise();

		final Int32op result1 = when4bytes.int32(-1);

		when4bytes.go(code.tail());

		final Int32op result2 = not4bytes.int32(-1).shl(
				null,
				not4bytes,
				alignment.shl(null, not4bytes, 3))
				.comp(null, not4bytes);

		not4bytes.go(code.tail());

		return code.phi(
				id != null ? id : getId().sub("char_mask"),
				result1,
				result2);
	}

	public final Int32recOp length(CodeId id, Code code) {
		return ptr().length(id, code);
	}

	public Int32op loadLength(CodeId id, Code code) {
		return length(null, code).load(id, code);
	}

	public final Int64recOp rawValue(CodeId id, Code code) {
		return ptr().rawValue(id, code);
	}

	public final AnyOp value(CodeId id, Code code) {

		final CodeId valueId;

		if (id == null) {
			valueId = getId().sub("value");
		} else {
			valueId = id;
		}

		return rawValue(valueId.detail("raw"), code).toAny(valueId, code);
	}

	public final AnyOp loadData(CodeId id, Block code) {

		final AnyOp value =
				value(id != null ? id.detail("value") : null, code);
		final BoolOp external =
				loadExternal(id != null ? id.detail("external") : null, code);

		final CondBlock whenExternal = external.branch(code, "external");
		final Block notExternal = whenExternal.otherwise();

		final AnyOp result1 =
				value.toPtr(null, whenExternal).load(null, whenExternal);

		whenExternal.go(code.tail());

		final AnyOp result2 = notExternal.phi(null, value);

		notExternal.go(code.tail());

		return code.phi(id, result1, result2);
	}

	public final ValOp storeVoid(Code code) {
		assert getValueStruct().isVoid() :
			"Can not store VOID in " + getValueStruct() + " value";

		flags(null, code).store(code, code.int32(CONDITION_FLAG));

		return this;
	}

	public final ValOp storeFalse(Code code) {
		flags(null, code).store(code, code.int32(0));
		return this;
	}

	public final ValOp storeFalse(Block code, ValAtomicity atomicity) {

		final Int32recOp flags = flags(null, code);

		if (!atomicity.isVarAssignment()) {
			flags.store(code, code.int32(0), atomicity.toAtomicity());
			return this;
		}

		assert getValueType().isLink() && getValueType().isVariable() :
			"Not a variable " + getValueType();

		final Int64recOp value = rawValue(null, code);
		final Int64op expected =
				value.load(code.id("expected"), code, atomicity.toAtomicity());
		final Int64op old = value.testAndSet(
				code.id("old"),
				code,
				expected,
				code.int64(-1));
		final Block skip = code.addBlock("skip");

		expected.eq(code.id("assigned"), code, old)
		.goUnless(code, skip.head());

		flags.store(code, code.int32(0), atomicity.toAtomicity());

		skip.go(code.tail());

		return this;
	}

	public final ValOp storeIndefinite(Code code) {
		flags(null, code).store(
				code,
				code.int32(INDEFINITE_FLAG));
		return this;
	}

	public final ValOp store(Code code, Val value) {
		assert (value.getValueType() == getValueType()
				|| !value.getCondition() && value.isVoid()) :
			"Can not store " + value + " in " + this;
		return storeVal(code, value, Atomicity.NOT_ATOMIC);
	}

	public final ValOp store(Block code, Val value, ValAtomicity atomicity) {
		assert (value.getValueType() == getValueType()
				|| !value.getCondition() && value.isVoid()) :
			"Can not store " + value + " in " + this;

		if (!atomicity.isVarAssignment()) {
			return storeVal(code, value, atomicity.toAtomicity());
		}

		return assignVal(code, value, atomicity.toAtomicity());
	}

	public final ValOp store(Code code, ValOp value) {
		if (this == value || ptr() == value.ptr()) {
			return this;
		}
		return storeCopy(code, value, NOT_ATOMIC);
	}

	public final ValOp store(Block code, ValOp value, ValAtomicity atomicity) {
		if (this == value || ptr() == value.ptr()) {
			return this;
		}

		final Val constant = value.getConstant();

		if (constant != null) {
			return store(code, constant, atomicity);
		}
		if (!atomicity.isVarAssignment()) {
			return storeCopy(code, value, atomicity.toAtomicity());
		}

		return assignCopy(code, value, atomicity.toAtomicity());
	}

	public final ValOp store(Code code, Int64op value) {
		assert getValueType() == ValueType.INTEGER :
			"Can not store integer in " + getValueStruct() + " value";

		rawValue(null, code).store(code, value);
		flags(null, code).store(code, code.int32(Val.CONDITION_FLAG));

		return this;
	}

	public final ValOp store(Code code, Fp64op value) {
		assert getValueType() == ValueType.FLOAT :
			"Can not store floating-point number in "
			+ getValueStruct() + " value";

		value(null, code).toFp64(null, code).store(code, value);
		flags(null, code).store(code, code.int32(Val.CONDITION_FLAG));

		return this;
	}

	public final ValOp storeNull(Code code) {

		final ValueStorageIR storage = getStoreMode().storage(this);

		assert storage.getValueStructIR().hasValue() :
			"Can not store value to " + getValueStruct();

		storage.unuseVal(code, this);
		value(null, code).toPtr(null, code).store(code, code.nullPtr());
		flags(null, code).store(code, code.int32(Val.CONDITION_FLAG));
		if (storage.getValueStructIR().hasLength()) {
			length(null, code).store(code, code.int32(0));
		}

		return this;
	}

	public final ValOp store(Code code, AnyOp pointer) {

		final ValueStorageIR storage = getStoreMode().storage(this);

		assert storage.getValueStructIR().hasValue() :
			"Can not store value to " + getValueType();
		assert !storage.getValueStructIR().hasLength() :
			"Can not store pointer without length to " + getValueType();

		return storePointer(code, pointer, NOT_ATOMIC);
	}

	public final ValOp store(
			Block code,
			AnyOp pointer,
			ValAtomicity atomicity) {

		final ValueStorageIR storage = getStoreMode().storage(this);

		assert storage.getValueStructIR().hasValue() :
			"Can not store value to " + getValueType();
		assert !storage.getValueStructIR().hasLength() :
			"Can not store pointer without length to " + getValueType();

		if (!atomicity.isVarAssignment()) {
			return storePointer(code, pointer, atomicity.toAtomicity());
		}

		return assignPointer(code, pointer, atomicity.toAtomicity());
	}

	public final ValOp store(
			Code code,
			AnyOp pointer,
			DataAlignment alignment,
			Int32op length) {

		final ValueStorageIR storage = getStoreMode().storage(this);

		assert storage.getValueStructIR().hasValue() :
			"Can not store value to " + getValueType();
		assert storage.getValueStructIR().hasLength() :
			"Can not store pointer to value of scalar type: "
			+ getValueType();

		storage.unuseVal(code, this);
		flags(null, code)
		.store(code, code.int32(
				Val.CONDITION_FLAG | Val.EXTERNAL_FLAG
				| (alignment.getShift() << 8)));
		length(null, code).store(code, length);
		value(null, code).toPtr(null, code).store(code, pointer);
		storage.unuseVal(code, this);

		return this;
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

		loadCondition(null, code).goUnless(code, dirs.falseDir());
	}

	public void use(Code code) {

		final FuncPtr<ValUseFunc> func =
				code.getGenerator()
				.externalFunction()
				.link("o42a_val_use", VAL_USE);

		func.op(null, code).call(code, this);
	}

	public void unuse(Code code) {

		final FuncPtr<ValUseFunc> func =
				code.getGenerator()
				.externalFunction()
				.link("o42a_val_unuse", VAL_USE);

		func.op(null, code).call(code, this);
	}

	private BoolOp loadFlag(
			CodeId id,
			String defaultId,
			Code code,
			int mask,
			Atomicity atomicity) {

		final CodeId flagId;

		if (id == null) {
			flagId = getId().sub(defaultId);
		} else {
			flagId = id;
		}

		final Int32op flags = flags(null, code).load(null, code, atomicity);

		final Int32op uexternal = flags.lshr(
				flagId.detail("ush"),
				code,
				numberOfTrailingZeros(mask));

		return uexternal.lowestBit(flagId, code);
	}

	private ValOp storePointer(Code code, AnyOp pointer, Atomicity atomicity) {
		value(null, code).toPtr(null, code)
		.store(code, pointer, atomicity);
		flags(null, code)
		.store(code, code.int32(Val.CONDITION_FLAG), atomicity);
		return this;
	}

	private ValOp assignPointer(
			Block code,
			AnyOp pointer,
			Atomicity atomicity) {

		final Block skip = code.addBlock("skip");
		final AnyRecOp rec = value(null, code).toPtr(null, code);
		final AnyOp expected = rec.load(code.id("expected"), code, ATOMIC);
		final AnyOp old =
				rec.testAndSet(code.id("old"), code, expected, pointer);

		old.eq(code.id("assigned"), code, expected)
		.goUnless(code, skip.head());

		flags(null, code).store(code, code.int32(CONDITION_FLAG), atomicity);

		skip.go(code.tail());

		return this;
	}

	private ValOp storeVal(Code code, Val value, Atomicity atomicity) {
		assert (value.getValueType() == getValueType()
				|| !value.getCondition() && value.isVoid()) :
			"Can not store " + value + " in " + this;

		final ValueStorageIR storage = getStoreMode().storage(this);

		flags(null, code).store(
				code,
				code.int32(value.getFlags()),
				atomicity);
		if (value.getCondition()) {
			if (storage.getValueStructIR().hasLength()) {
				length(null, code)
				.store(code, code.int32(value.getLength()), atomicity);
			}
		}
		if (storage.getValueStructIR().hasValue()) {

			final Ptr<AnyOp> pointer = value.getPointer();

			storage.unuseVal(code, this);
			if (pointer != null) {
				value(null, code)
				.toPtr(null, code)
				.store(code, pointer.op(null, code), atomicity);
			} else {
				rawValue(null, code)
				.store(code, code.int64(value.getValue()), atomicity);
			}
		}

		return this;
	}

	private ValOp assignVal(Block code, Val value, Atomicity atomicity) {
		assert getValueType().isLink() && getValueType().isVariable() :
			"Not a variable: " + getValueType();

		final ValueStorageIR storage = getStoreMode().storage(this);
		final Int32recOp flags = flags(null, code);
		final Int32recOp length = length(null, code);
		final ValOp preserved = code.getAllocator().allocation().allocate(
				code.id("preserved"),
				VAL_TYPE).op(getBuilder(), getValueStruct());
		final Block skip = code.addBlock("skip");
		final Ptr<AnyOp> pointer = value.getPointer();

		if (pointer != null && value.getCondition()) {

			final AnyRecOp rec = value(null, code).toPtr(null, code);
			final AnyOp expected = rec.load(
					code.id("expected"),
					code,
					atomicity);
			final AnyOp old = rec.testAndSet(
					code.id("old"),
					code,
					expected,
					pointer.op(null, code));

			old.eq(code.id("assigned"), code, expected)
			.goUnless(code, skip.head());

			preserved.flags(null, code).store(code, flags.load(null, code));
			if (storage.getValueStructIR().hasLength()) {
				preserved.length(null, code)
				.store(null, length.load(null, code));
			}
			preserved.value(null, code).toPtr(null, code).store(code, expected);
		} else {

			final Int64recOp rec = rawValue(null, code);
			final Int64op expected = rec.load(
					code.id("expected"),
					code,
					atomicity);
			final Int64op old = rec.testAndSet(
					code.id("old"),
					code,
					expected,
					value.getCondition()
					? code.int64(value.getValue())
					: code.int64(-1));

			old.eq(code.id("assigned"), code, expected)
			.goUnless(code, skip.head());

			preserved.flags(null, code).store(code, flags.load(null, code));
			if (storage.getValueStructIR().hasLength()) {
				preserved.length(null, code)
				.store(null, length.load(null, code));
			}
			preserved.rawValue(null, code).store(code, expected);
		}

		storage.unuseVal(code, preserved);

		flags.store(
				code,
				code.int32(value.getFlags()),
				atomicity);
		if (value.getCondition() && storage.getValueStructIR().hasLength()) {
			length.store(
					code,
					code.int32(value.getLength()),
					atomicity);
		}

		skip.go(code.tail());

		return this;
	}

	private final ValOp storeCopy(Code code, ValOp value, Atomicity atomicity) {

		final Val constant = value.getConstant();

		if (constant != null) {
			return storeVal(code, constant, atomicity);
		}

		assert getValueStruct().assignableFrom(value.getValueStruct()) :
			"Can not store " + value + " in " + this;

		final ValueStorageIR storage = getStoreMode().storage(this);

		storage.unuseVal(code, this);
		flags(null, code).store(
				code,
				value.flags(null, code).load(null, code),
				atomicity);
		if (storage.getValueStructIR().hasLength()) {
			length(null, code).store(
					code,
					value.length(null, code).load(null, code),
					atomicity);
		}
		if (storage.getValueStructIR().hasValue()) {
			rawValue(null, code).store(
					code,
					value.rawValue(null, code).load(null, code),
					atomicity);
		}
		storage.useVal(code, this, value);

		return this;
	}

	private final ValOp assignCopy(
			Block code,
			ValOp value,
			Atomicity atomicity) {
		assert getValueStruct().assignableFrom(value.getValueStruct()) :
			"Can not store " + value + " in " + this;
		assert getValueType().isLink() && getValueType().isVariable() :
			"Not a variable: " + getValueType();

		final ValueStorageIR storage = getStoreMode().storage(this);
		final Int32recOp flags = flags(null, code);
		final Int32recOp length = length(null, code);
		final Int64recOp rawValue = rawValue(null, code);
		final ValOp preserved = code.getAllocator().allocation().allocate(
				code.id("preserved"),
				VAL_TYPE).op(getBuilder(), getValueStruct());
		final Block skip = code.addBlock("skip");

		final Int64op expected = rawValue.load(
				code.id("expected"),
				code,
				atomicity);
		final Int64op old = rawValue.testAndSet(
				code.id("old"),
				code,
				expected,
				value.rawValue(null, code).load(null, code));

		old.eq(code.id("assigned"), code, expected).goUnless(code, skip.head());

		preserved.flags(null, code).store(code, flags.load(null, code));
		if (storage.getValueStructIR().hasLength()) {
			preserved.length(null, code).store(null, length.load(null, code));
		}
		preserved.rawValue(null, code).store(code, expected);
		storage.unuseVal(code, preserved);

		flags.store(code, value.flags(null, code).load(null, code), atomicity);
		if (storage.getValueStructIR().hasLength()) {
			length.store(
					code,
					value.length(null, code).load(null, code),
					atomicity);
		}
		storage.useVal(code, this, value);

		skip.go(code.tail());

		return this;
	}

}
