/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
import static org.o42a.core.ir.value.Val.*;
import static org.o42a.core.ir.value.ValStoreMode.ASSIGNMENT_VAL_STORE;
import static org.o42a.core.ir.value.ValStoreMode.TEMP_VAL_STORE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CondCode;
import org.o42a.codegen.code.op.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.op.*;
import org.o42a.core.value.ValueType;


public final class ValOp extends IROp implements CondOp {

	private final ValueType<?> valueType;
	private ValStoreMode storeMode = ASSIGNMENT_VAL_STORE;

	ValOp(CodeBuilder builder, ValType.Op ptr, ValueType<?> valueType) {
		super(builder, ptr);
		this.valueType = valueType;
	}

	public final ValueType<?> getValueType() {
		return this.valueType;
	}

	@Override
	public final ValType.Op ptr() {
		return (ValType.Op) super.ptr();
	}

	public final ValStoreMode getStoreMode() {
		if (ptr().isAllocatedOnStack()) {
			return this.storeMode = TEMP_VAL_STORE;
		}
		return this.storeMode;
	}

	public final void setStoreMode(ValStoreMode storeMode) {
		assert storeMode != null :
			"Value store mode not specified";
		this.storeMode = storeMode;
	}

	public final Int32recOp flags(CodeId id, Code code) {
		return ptr().flags(id, code);
	}

	@Override
	public final BoolOp loadCondition(CodeId id, Code code) {

		final Int32op flags = flags(null, code).load(null, code);

		return flags.lowestBit(
				id != null ? id : getId().sub("condition_flag"),
				code);
	}

	@Override
	public final BoolOp loadUnknown(CodeId id, Code code) {
		return loadFlag(id, "unknown_flag", code, UNKNOWN_FLAG);
	}

	public final BoolOp loadIndefinite(CodeId id, Code code) {
		return loadFlag(id, "indefinite_flag", code, INDEFINITE_FLAG);
	}

	public final BoolOp loadExternal(CodeId id, Code code) {
		return loadFlag(id, "external_flag", code, EXTERNAL_FLAG);
	}

	public final BoolOp loadStatic(CodeId id, Code code) {
		return loadFlag(id, "static_flag", code, STATIC_FLAG);
	}

	public Int32op loadAlignmentShift(CodeId id, Code code) {
		if (id == null) {
			id = getId().sub("alignment_shift");
		}

		final Int32op flags = flags(null, code).load(null, code);
		final Int32op ualignment = flags.and(
				id.detail("ush"),
				code,
				code.int32(ALIGNMENT_MASK));

		return ualignment.lshr(
				id,
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

	public Int32op loadCharMask(CodeId id, Code code) {

		final Int32op alignment =
			loadAlignment(id != null ? id.detail("alignment") : null, code);
		final BoolOp is4bytes = alignment.ge(
				alignment.getId().detail("4bytes"),
				code,
				code.int32(4));
		final CondCode when4bytes = is4bytes.branch(code, "4bytes");
		final Code not4bytes = when4bytes.otherwise();

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

	public Int32op loadDataLength(CodeId id, Code code) {

		final Int32op byteLength = length(
				id != null ? id.detail("length") : null,
				code).load(null, code);
		final Int32op alignmentShift = loadAlignmentShift(
					id != null ? id.detail("alignment_shift") : null,
					code);

		return byteLength.lshr(
				id != null ? id : getId().sub("data_len"),
				code,
				alignmentShift);
	}

	public final Int64recOp rawValue(CodeId id, Code code) {
		return ptr().rawValue(id, code);
	}

	public final AnyOp value(CodeId id, Code code) {
		if (id == null) {
			id = getId().sub("value");
		}
		return rawValue(id.detail("raw"), code).toAny(id, code);
	}

	public final AnyOp loadData(CodeId id, Code code) {

		final AnyOp value =
			value(id != null ? id.detail("value") : null, code);
		final BoolOp external =
			loadExternal(id != null ? id.detail("external") : null, code);

		final CondCode whenExternal = external.branch(code, "external");
		final Code notExternal = whenExternal.otherwise();

		final AnyOp result1 =
			value.toPtr(null, whenExternal).load(null, whenExternal);

		whenExternal.go(code.tail());

		final AnyOp result2 = notExternal.phi(null, value);

		notExternal.go(code.tail());

		return code.phi(id, result1, result2);
	}

	public final ValOp storeVoid(Code code) {
		assert getValueType() == ValueType.VOID :
			"Can not store VOID in " + getValueType() + " value";

		flags(null, code).store(code, code.int32(CONDITION_FLAG));

		return this;
	}

	public final ValOp storeFalse(Code code) {
		flags(null, code).store(code, code.int32(0));
		return this;
	}

	public final ValOp storeUnknown(Code code) {
		flags(null, code).store(code, code.int32(UNKNOWN_FLAG));
		return this;
	}

	public final ValOp storeIndefinite(Code code) {
		flags(null, code).store(
				code,
				code.int32(UNKNOWN_FLAG | INDEFINITE_FLAG));
		return this;
	}

	public ValOp store(Code code, Val value) {
		assert !value.getCondition() || value.getValueType() == getValueType() :
			"Can not store " + value + " in " + getValueType() + " value";

		getStoreMode().store(code, this, value);

		return this;
	}

	public final ValOp store(Code code, ValOp value) {
		if (this == value || ptr() == value.ptr()) {
			return this;
		}

		assert getValueType() == value.getValueType() :
			"Can not store " + value.getValueType() + " value "
			+ " in " + getValueType() + " value";

		getStoreMode().store(code, this, value);

		return this;
	}

	public final ValOp store(Code code, Int64op value) {
		assert getValueType() == ValueType.INTEGER :
			"Can not store integer in " + getValueType() + " value";

		rawValue(null, code).store(code, value);
		flags(null, code).store(code, code.int32(Val.CONDITION_FLAG));

		return this;
	}

	public final ValOp store(Code code, Fp64op value) {
		assert getValueType() == ValueType.FLOAT :
			"Can not store floating-point number in "
			+ getValueType() + " value";

		value(null, code).toFp64(null, code).store(code, value);
		flags(null, code).store(code, code.int32(Val.CONDITION_FLAG));

		return this;
	}

	public final void go(Code code, ValDirs dirs) {
		dirs.dirs().go(code, this);
	}

	@Override
	public final void go(Code code, CodeDirs dirs) {
		dirs.go(code, this);
	}

	private BoolOp loadFlag(
			CodeId id,
			String defaultId,
			Code code,
			int mask) {
		if (id == null) {
			id = getId().sub(defaultId);
		}

		final Int32op flags = flags(null, code).load(null, code);
		final Int32op uexternal = flags.lshr(
				id.detail("ush"),
				code,
				numberOfTrailingZeros(mask));

		return uexternal.lowestBit(id, code);
	}

}
