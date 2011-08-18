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

import org.o42a.backend.constant.code.func.FuncCCaller;
import org.o42a.backend.constant.code.op.COp;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.*;
import org.o42a.codegen.code.op.*;
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
	public CodeId getId() {
		return getUnderlying().getId();
	}

	@Override
	public <F extends Func<F>> FuncCaller<F> caller(
			CodeId id,
			FuncAllocation<F> allocation) {

		final F underlyingFunc =
				cast(allocation).getUnderlyingPtr().op(id, getUnderlying());

		return new FuncCCaller<F>(this, underlyingFunc);
	}

	@Override
	public CodeWriter block(Code code, CodeId id) {
		return new CBlock(this, code, getUnderlying().addBlock(id));
	}

	@Override
	public AllocationWriter allocationBlock(AllocationCode code, CodeId id) {
		return new CAllocation(this, code, getUnderlying().allocate(id));
	}

	@Override
	public Int8op int8(byte value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Int16op int16(short value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Int32op int32(int value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Int64op int64(long value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Fp32op fp32(float value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Fp64op fp64(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BoolOp bool(boolean value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RelOp nullRelPtr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AnyOp nullPtr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataOp nullDataPtr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends StructOp<S>> S nullPtr(DataAllocation<S> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <F extends Func<F>> FuncCaller<F> nullPtr(Signature<F> signature) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void go(CodePos pos) {
		getUnderlying().go(underlying(pos));
	}

	@Override
	public void go(BoolOp condition, CodePos truePos, CodePos falsePos) {
		underlying(condition).go(
				getUnderlying(),
				underlying(truePos),
				underlying(falsePos));
	}

	@Override
	public MultiCodePos comeFrom(CodeWriter[] alts) {

		final CodeWriter[] underlyingAlts = new CodeWriter[alts.length];

		for (int i = 0; i < alts.length; ++i) {
			underlyingAlts[i] = underlying(alts[i]);
		}

		return getUnderlying().writer().comeFrom(alts);
	}

	@Override
	public void goToOneOf(MultiCodePos target) {
		getUnderlying().writer().goToOneOf(target);
	}

	@Override
	public AnyRecOp allocatePtr(CodeId id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends StructOp<S>> StructRecOp<S> allocatePtr(
			CodeId id,
			DataAllocation<S> allocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <S extends StructOp<S>> S allocateStruct(
			CodeId id,
			DataAllocation<S> allocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <O extends Op> O phi(CodeId id, O op) {

		final COp<O> cop = cast(op);
		final O underlyingPHI = getUnderlying().phi(id, cop.getUnderlying());

		return cop.create(this, underlyingPHI);
	}

	@Override
	public <O extends Op> O phi(CodeId id, O op1, O op2) {

		final COp<O> cop1 = cast(op1);
		final O underlyingPHI =
				getUnderlying().phi(id, cop1.getUnderlying(), underlying(op2));

		return cop1.create(this, underlyingPHI);
	}

	@Override
	public void returnVoid() {
		beforeReturn();
		getUnderlying().returnVoid();
	}

	public void beforeReturn() {
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
