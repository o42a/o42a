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
package org.o42a.core.ir.value;

import static java.lang.Integer.numberOfTrailingZeros;
import static org.o42a.core.ir.value.Val.*;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CondBlock;
import org.o42a.codegen.code.op.*;
import org.o42a.core.ir.value.ValType.Op;


public final class ValFlagsOp {

	private final Int32recOp ptr;
	private final Atomicity atomicity;
	private final Code loadInset;
	private Int32op flags;

	ValFlagsOp(CodeId id, Code code, Op op, Atomicity atomicity) {
		this.ptr = op.int32(id, code, op.getType().flags());
		this.loadInset = code.inset("load_flags");
		this.atomicity = atomicity;
	}

	private ValFlagsOp(ValFlagsOp prototype, Int32op flags) {
		this.ptr = prototype.ptr;
		this.atomicity = prototype.atomicity;
		this.loadInset = prototype.loadInset;
		this.flags = flags;
	}

	public final CodeId getId() {
		return this.ptr.getId();
	}

	public final Int32recOp ptr() {
		return this.ptr;
	}

	public final Int32op get() {
		if (this.flags != null) {
			return this.flags;
		}
		return this.flags = this.ptr.load(null, this.loadInset, this.atomicity);
	}

	public final BoolOp condition(CodeId id, Code code) {
		return get().lowestBit(
				id != null ? id : getId().sub("condition_flag"),
				code);
	}

	public final BoolOp indefinite(CodeId id, Code code) {
		return get(id, "indefinite", code, VAL_INDEFINITE);
	}

	public final BoolOp external(CodeId id, Code code) {
		return get(id, "external", code, VAL_EXTERNAL);
	}

	public final BoolOp staticStore(CodeId id, Code code) {
		return get(id, "static", code, VAL_STATIC);
	}

	public final BoolOp assigning(CodeId id, Code code) {
		return get(id, "assigning", code, VAL_ASSIGN);
	}

	public final Int32op alignmentShift(CodeId id, Code code) {

		final CodeId ashiftId;

		if (id == null) {
			ashiftId = getId().sub("alignment_shift");
		} else {
			ashiftId = id;
		}

		final Int32op ualignment = get().and(
				ashiftId.detail("ush"),
				code,
				code.int32(VAL_ALIGNMENT_MASK));

		return ualignment.lshr(
				ashiftId,
				code,
				numberOfTrailingZeros(VAL_ALIGNMENT_MASK));
	}

	public final Int32op alignment(CodeId id, Code code) {

		final Int32op shift = alignmentShift(
				id != null ? id.detail("shift") : null,
				code);

		return code.int32(1).shl(
				id != null ? id : getId().sub("alignment"),
				code,
				shift);
	}

	public final Int32op charMask(CodeId id, Block code) {

		final Int32op alignment =
				alignment(id != null ? id.detail("alignment") : null, code);
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

	public final void storeFalse(Code code) {
		store(code, 0);
	}

	public final void store(Code code, int flags) {
		this.ptr.store(code, code.int32(flags), this.atomicity);
	}

	public final void store(Code code, Int32op flags) {
		this.ptr.store(code, flags, this.atomicity);
	}

	public final ValFlagsOp atomicRMW(
			CodeId id,
			Code code,
			RMWKind kind,
			int operand) {
		return new ValFlagsOp(
				this,
				this.ptr.atomicRMW(id, code, kind, code.int32(operand)));
	}

	@Override
	public String toString() {
		if (this.ptr == null) {
			return super.toString();
		}
		return this.ptr.toString();
	}

	private final BoolOp get(
			CodeId id,
			String defaultId,
			Code code,
			int mask) {

		final CodeId flagId;

		if (id == null) {
			flagId = getId().sub(defaultId);
		} else {
			flagId = id;
		}

		final Int32op uflag = get().lshr(
				flagId.detail("ush"),
				code,
				numberOfTrailingZeros(mask));

		return uflag.lowestBit(flagId, code);
	}

}
