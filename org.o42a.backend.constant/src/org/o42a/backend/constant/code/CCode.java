/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.backend.constant.code;

import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.backend.constant.data.ConstBackend.underlying;

import org.o42a.backend.constant.code.func.CFunc;
import org.o42a.backend.constant.code.op.*;
import org.o42a.backend.constant.code.rec.AnyRecCOp;
import org.o42a.backend.constant.code.rec.StructRecCOp;
import org.o42a.backend.constant.code.signature.CSignature;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.backend.constant.data.struct.CType;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.backend.FuncAllocation;
import org.o42a.codegen.code.backend.MultiCodePos;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.data.backend.DataAllocation;


public abstract class CCode<C extends Code> implements CodeWriter {

	private final ConstBackend backend;
	private final CFunction<?> function;
	private final C code;
	private final C underlying;
	private CCodePos head;
	private CCodePos tail;

	CCode(
			ConstBackend backend,
			CFunction<?> function,
			C code,
			C underlying) {
		this.backend = backend;
		this.function = function != null ? function : (CFunction<?>) this;
		this.code = code;
		this.underlying = underlying;
	}

	public final ConstBackend getBackend() {
		return this.backend;
	}

	public final CFunction<?> getFunction() {
		return this.function;
	}

	public final C code() {
		return this.code;
	}

	public final C getUnderlying() {
		return this.underlying;
	}

	@Override
	public CodePos head() {
		if (this.head != null) {
			return this.head;
		}
		return this.head = new CCodePos(getUnderlying().head());
	}

	@Override
	public CodePos tail() {

		final CodePos underlyingTail = getUnderlying().tail();

		if (this.tail != null && this.tail.getUnderlying() == underlyingTail) {
			return this.tail;
		}

		return this.tail = new CCodePos(underlyingTail);
	}

	@Override
	public boolean created() {
		return getUnderlying().created();
	}

	@Override
	public boolean exists() {
		return getUnderlying().exists();
	}

	@Override
	public void done() {
		getUnderlying().done();
	}

	@Override
	public final CodeId getId() {
		return getUnderlying().getId();
	}

	@Override
	public final <F extends Func<F>> CFunc<F> caller(
			CodeId id,
			FuncAllocation<F> allocation) {

		final F underlyingFunc =
				cast(allocation).getUnderlyingPtr().op(id, getUnderlying());

		return new CFunc<F>(this, underlyingFunc);
	}

	@Override
	public final CBlock block(Code code, CodeId id) {
		return new CBlock(this, code, getUnderlying().addBlock(id));
	}

	@Override
	public final CAllocation allocationBlock(AllocationCode code, CodeId id) {
		return new CAllocation(this, code, getUnderlying().allocate(id));
	}

	@Override
	public final Int8cOp int8(byte value) {
		return new Int8cOp(this, getUnderlying().int8(value));
	}

	@Override
	public final Int16cOp int16(short value) {
		return new Int16cOp(this, getUnderlying().int16(value));
	}

	@Override
	public final Int32cOp int32(int value) {
		return new Int32cOp(this, getUnderlying().int32(value));
	}

	@Override
	public final Int64cOp int64(long value) {
		return new Int64cOp(this, getUnderlying().int64(value));
	}

	@Override
	public final Fp32cOp fp32(float value) {
		return new Fp32cOp(this, getUnderlying().fp32(value));
	}

	@Override
	public final Fp64cOp fp64(double value) {
		return new Fp64cOp(this, getUnderlying().fp64(value));
	}

	@Override
	public final BoolCOp bool(boolean value) {
		return new BoolCOp(this, getUnderlying().bool(value));
	}

	@Override
	public final RelCOp nullRelPtr() {
		return new RelCOp(this, getUnderlying().nullRelPtr());
	}

	@Override
	public final AnyCOp nullPtr() {
		return new AnyCOp(this, getUnderlying().nullPtr());
	}

	@Override
	public final DataCOp nullDataPtr() {
		return new DataCOp(this, getUnderlying().nullDataPtr());
	}

	@Override
	public final <S extends StructOp<S>> S nullPtr(DataAllocation<S> type) {

		final ContainerCDAlloc<S> typeAlloc = (ContainerCDAlloc<S>) type;
		final CType<S> underlyingType = typeAlloc.getUnderlyingInstance();
		final S underlyingPtr = getUnderlying().nullPtr(underlyingType);

		return underlyingType.getOriginal().op(
				new CStruct<S>(
						this,
						underlyingPtr,
						underlyingType.getOriginal()));
	}

	@Override
	public final <F extends Func<F>> CFunc<F> nullPtr(Signature<F> signature) {

		final CSignature<F> underlyingSignature =
				getBackend().underlying(signature);
		final F underlyingPtr = getUnderlying().nullPtr(underlyingSignature);

		return new CFunc<F>(this, underlyingPtr);
	}

	@Override
	public final void go(CodePos pos) {
		getUnderlying().go(underlying(pos));
	}

	@Override
	public final void go(BoolOp condition, CodePos truePos, CodePos falsePos) {
		underlying(condition).go(
				getUnderlying(),
				underlying(truePos),
				underlying(falsePos));
	}

	@Override
	public final MultiCodePos comeFrom(CodeWriter[] alts) {

		final CodeWriter[] underlyingAlts = new CodeWriter[alts.length];

		for (int i = 0; i < alts.length; ++i) {
			underlyingAlts[i] = underlying(alts[i]);
		}

		return getUnderlying().writer().comeFrom(underlyingAlts);
	}

	@Override
	public final void goToOneOf(MultiCodePos target) {
		getUnderlying().writer().goToOneOf(target);
	}

	@Override
	public final AnyRecCOp allocatePtr(CodeId id) {
		return new AnyRecCOp(this, getUnderlying().writer().allocatePtr(id));
	}

	@Override
	public final <S extends StructOp<S>> StructRecCOp<S> allocatePtr(
			CodeId id,
			DataAllocation<S> typeAllocation) {

		final ContainerCDAlloc<S> typeAlloc =
				(ContainerCDAlloc<S>) typeAllocation;
		final StructRecOp<S> underlyingOp =
				getUnderlying().writer().allocatePtr(
						id,
						typeAlloc.getUnderlyingPtr().getAllocation());

		return new StructRecCOp<S>(
				this,
				underlyingOp,
				typeAlloc.getUnderlyingInstance().getType());
	}

	@Override
	public final <S extends StructOp<S>> S allocateStruct(
			CodeId id,
			DataAllocation<S> typeAllocation) {

		final ContainerCDAlloc<S> typeAlloc =
				(ContainerCDAlloc<S>) typeAllocation;
		final S underlyingOp = getUnderlying().writer().allocateStruct(
				id, typeAlloc.getUnderlyingPtr().getAllocation());
		final Type<S> type = typeAlloc.getUnderlyingInstance().getOriginal();

		return type.op(new CStruct<S>(this, underlyingOp, type));
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <O extends Op> O phi(CodeId id, O op) {
		if (op instanceof StructOp<?>) {

			final CStruct<?> cop = cast((StructOp<?>) op);
			final StructOp<?> underlyingPHI =
					getUnderlying().phi(id, cop.getUnderlying());
			@SuppressWarnings("rawtypes")
			final CStruct res = cop;

			return (O) res.create(this, underlyingPHI);
		}

		final COp<O> cop = cast(op);
		final O underlyingPHI = getUnderlying().phi(id, cop.getUnderlying());

		return cop.create(this, underlyingPHI);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <O extends Op> O phi(CodeId id, O op1, O op2) {
		if (op1 instanceof StructOp) {

			final CStruct<?> cop1 = cast((StructOp<?>) op1);
			final StructOp<?> underlyingPHI = getUnderlying().phi(
					id,
					cop1.getUnderlying(),
					underlying((StructOp<?>) op2));
			@SuppressWarnings("rawtypes")
			final CStruct res = cop1;

			return (O) res.create(this, underlyingPHI);
		}

		final COp<O> cop1 = cast(op1);
		final O underlyingPHI =
				getUnderlying().phi(id, cop1.getUnderlying(), underlying(op2));

		return cop1.create(this, underlyingPHI);
	}

	@Override
	public final void returnVoid() {
		beforeReturn();
		getUnderlying().returnVoid();
	}

	public final void beforeReturn() {
		getFunction().getCallback().beforeReturn(code());
	}

	@Override
	public String toString() {
		if (this.underlying == null) {
			return super.toString();
		}
		return this.underlying.toString();
	}

}
