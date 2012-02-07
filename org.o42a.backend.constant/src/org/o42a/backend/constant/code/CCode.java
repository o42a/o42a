/*
    Constant Handler Compiler Back-end
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
package org.o42a.backend.constant.code;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.op.*;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.func.CFAlloc;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.backend.constant.data.struct.CType;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.FuncAllocation;


public abstract class CCode<C extends Code> implements CodeWriter {

	private final ConstBackend backend;
	private final CFunction<?> function;
	private final C code;
	private final C underlying;

	CCode(
			ConstBackend backend,
			CFunction<?> function,
			C code,
			C underlying) {
		this.backend = backend;
		this.function = function != null ? function : (CFunction<?>) this;
		this.code = code;
		this.underlying = underlying;
		underlying.setOpNames(code.getOpNames());
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

		final CFAlloc<F> alloc = cast(allocation);

		return new CFunc<F>(
				new OpBE<F>(id, this) {
					@Override
					protected F write() {
						return alloc.getUnderlyingPtr().op(
								getId(),
								getUnderlying());
					}
				},
				alloc.getPointer());
	}

	@Override
	public final CodeWriter inset(Code code) {
		return recordInset(new CCodeInset(
				this,
				code,
				getUnderlying().inset(code.getId().getLocal())));
	}

	@Override
	public final CAllocation allocation(AllocationCode code) {

		final CodeId id = code.getId();

		return recordInset(new CAllocation(
				this,
				code,
				code.isDisposable()
				? getUnderlying().allocate(id.getLocal())
				: getUnderlying().undisposable(id.getLocal())));
	}

	@Override
	public final CCodeBlock block(Block code) {
		return new CCodeBlock(
				this,
				code,
				getUnderlying().addBlock(code.getId().getLocal()));
	}

	@Override
	public final Int8cOp int8(byte value) {
		return new Int8cOp(null, this, value);
	}

	@Override
	public final Int16cOp int16(short value) {
		return new Int16cOp(null, this, value);
	}

	@Override
	public final Int32cOp int32(int value) {
		return new Int32cOp(null, this, value);
	}

	@Override
	public final Int64cOp int64(long value) {
		return new Int64cOp(null, this, value);
	}

	@Override
	public final Fp32cOp fp32(float value) {
		return new Fp32cOp(null, this, value);
	}

	@Override
	public final Fp64cOp fp64(double value) {
		return new Fp64cOp(null, this, value);
	}

	@Override
	public final BoolCOp bool(boolean value) {
		return new BoolCOp(null, this, value);
	}

	@Override
	public final RelCOp nullRelPtr() {
		return new RelCOp(
				new OpBE<RelOp>(null, this) {
					@Override
					protected RelOp write() {
						return getUnderlying().nullRelPtr();
					}
				});
	}

	@Override
	public final AnyCOp nullPtr() {
		return new AnyCOp(
				new OpBE<AnyOp>(null, this) {
					@Override
					protected AnyOp write() {
						return getUnderlying().nullPtr();
					}
				},
				getBackend().getGenerator().getGlobals().nullPtr());
	}

	@Override
	public final DataCOp nullDataPtr() {
		return new DataCOp(
				new OpBE<DataOp>(null, this) {
					@Override
					protected DataOp write() {
						return getUnderlying().nullDataPtr();
					}
				},
				getBackend().getGenerator().getGlobals().nullDataPtr());
	}

	@Override
	public final <S extends StructOp<S>> S nullPtr(DataAllocation<S> type) {

		final ContainerCDAlloc<S> typeAlloc = (ContainerCDAlloc<S>) type;
		final Type<S> originalType =
				typeAlloc.getData().getInstance().getType();

		return originalType.op(new CStruct<S>(
				new OpBE<S>(null, this) {
					@Override
					protected S write() {

						final CType<S> underlyingType =
								typeAlloc.getUnderlyingInstance();

						return getUnderlying().nullPtr(underlyingType);
					}
				},
				originalType,
				getBackend().getGenerator().getGlobals().nullPtr(
						originalType)));
	}

	@Override
	public final <F extends Func<F>> CFunc<F> nullPtr(
			final Signature<F> signature) {
		return new CFunc<F>(
				new OpBE<F>(null, this) {
					@Override
					protected F write() {
						return getUnderlying().nullPtr(
								getBackend().underlying(signature));
					}
				},
				getBackend().getGenerator().getFunctions().nullPtr(
						signature));
	}

	@Override
	@SuppressWarnings("unchecked")
	public final <O extends Op> O phi(CodeId id, O op) {

		final COp<?, ?> cop = cast((StructOp<?>) op);
		@SuppressWarnings("rawtypes")
		final COp res = cop;

		return (O) res.create(
				new OpBE<O>(id, this) {
					@Override
					protected O write() {
						return (O) code().getUnderlying().phi(
								getId(),
								cop.backend().underlying());
					}
				},
				cop.getConstant());
	}

	@Override
	public final <O extends Op> O phi(CodeId id, O op1, O op2) {

		final COp<O, ?> cop1 = cast(op1);
		final COp<O, ?> cop2 = cast(op2);

		if (cop1.isConstant() && cop2.isConstant()) {
			if (cop1.getConstant().equals(cop2.getConstant())) {
				return phi(id, op1);
			}
		}

		return cop1.create(
				new OpBE<O>(id, this) {
					@Override
					protected O write() {
						 return getUnderlying().phi(
									getId(),
									cop1.backend().underlying(),
									cop2.backend().underlying());
					}
				},
				null);
	}

	public final <O extends InstrBE> O op(O op) {
		return record(op);
	}

	@Override
	public String toString() {
		if (this.underlying == null) {
			return super.toString();
		}
		return this.underlying.toString();
	}

	protected abstract OpRecords records();

	final <R extends OpRecord> R record(R op) {
		return records().add(op);
	}

	private final <I extends CInset<?>> I recordInset(I inset) {
		record(inset.records());
		return inset;
	}

}
