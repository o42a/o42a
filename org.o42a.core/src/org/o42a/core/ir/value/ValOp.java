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
import static org.o42a.codegen.code.op.Atomicity.NOT_ATOMIC;
import static org.o42a.core.ir.value.Val.*;

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
import org.o42a.core.ir.value.struct.ValueStructIR;
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
	private ValueStructIR<?, ?> valueStructIR;

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

	public final ValueStructIR<?, ?> getValueStructIR() {
		if (this.valueStructIR != null) {
			return this.valueStructIR;
		}
		return this.valueStructIR = this.valueStruct.ir(getGenerator());
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

	public final ValOp storeIndefinite(Code code) {
		flags(null, code).store(code, code.int32(INDEFINITE_FLAG));
		return this;
	}

	public final ValOp store(Code code, Val value) {
		assert (value.getValueType() == getValueType()
				|| !value.getCondition() && value.isVoid()) :
			"Can not store " + value + " in " + this;
		assert (value.getValueType() == getValueType()
				|| !value.getCondition() && value.isVoid()) :
			"Can not store " + value + " in " + this;

		flags(null, code).store(code, code.int32(value.getFlags()));

		if (!value.getCondition()) {
			return this;
		}
		if (!getValueStructIR().hasValue()) {
			return this;
		}
		if (getValueStructIR().hasLength()) {
			length(null, code).store(code, code.int32(value.getLength()));
		}

		final Ptr<AnyOp> pointer = value.getPointer();

		if (pointer != null) {
			value(null, code)
			.toPtr(null, code)
			.store(code, pointer.op(null, code));
		} else {
			rawValue(null, code).store(code, code.int64(value.getValue()));
		}

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

		assert getValueStruct().assignableFrom(value.getValueStruct()) :
			"Can not store " + value + " in " + this;

		flags(null, code).store(code, value.flags(null, code).load(null, code));
		if (!getValueStructIR().hasValue()) {
			return this;
		}
		if (getValueStructIR().hasLength()) {
			length(null, code)
			.store(code, value.length(null, code).load(null, code));
		}
		rawValue(null, code)
		.store(code, value.rawValue(null, code).load(null, code));

		return this;
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
		assert getValueStructIR().hasValue() :
			"Can not store value to " + getValueStruct();

		flags(null, code).store(code, code.int32(Val.CONDITION_FLAG));
		if (getValueStructIR().hasLength()) {
			length(null, code).store(code, code.int32(0));
		}
		value(null, code).toPtr(null, code).store(code, code.nullPtr());

		return this;
	}

	public final ValOp store(Code code, AnyOp pointer) {
		assert getValueStructIR().hasValue() :
			"Can not store value to " + getValueType();
		assert !getValueStructIR().hasLength() :
			"Can not store pointer without length to " + getValueType();

		value(null, code).toPtr(null, code).store(code, pointer);
		flags(null, code).store(code, code.int32(CONDITION_FLAG));

		return this;
	}

	public final ValOp store(
			Code code,
			AnyOp pointer,
			DataAlignment alignment,
			Int32op length) {
		assert getValueStructIR().hasValue() :
			"Can not store value to " + getValueType();
		assert getValueStructIR().hasLength() :
			"Can not store pointer to value of scalar type: "
			+ getValueType();

		flags(null, code).store(
				code,
				code.int32(
						Val.CONDITION_FLAG | Val.EXTERNAL_FLAG
						| (alignment.getShift() << 8)));
		length(null, code).store(code, length);
		value(null, code).toPtr(null, code).store(code, pointer);

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

	public final void use(Code code) {
		ptr().use(code);
	}

	public final void unuse(Code code) {
		ptr().unuse(code);
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

		final Int32op uflag = flags.lshr(
				flagId.detail("ush"),
				code,
				numberOfTrailingZeros(mask));

		return uflag.lowestBit(flagId, code);
	}

}
