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
import static org.o42a.codegen.data.AllocClass.CONSTANT_ALLOC_CLASS;

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

	CCode(ConstBackend backend, CFunction<?> function, C code) {
		this.backend = backend;
		this.function = function != null ? function : (CFunction<?>) this;
		this.code = code;
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

	public abstract CCodePart<?> nextPart();

	@Override
	public final CodeId getId() {
		return code().getId();
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
								part().underlying());
					}
				},
				alloc.getSignature(),
				alloc.getPointer());
	}

	@Override
	public final CodeWriter inset(Code code) {
		return recordCode(new CCodeInset(this, code));
	}

	@Override
	public final CAllocation allocation(AllocationCode code) {
		return recordCode(new CAllocation(this, code));
	}

	@Override
	public final CCodeBlock block(Block code) {
		return recordCode(new CCodeBlock(this, code));
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
						return part().underlying().nullRelPtr();
					}
				});
	}

	@Override
	public final AnyCOp nullPtr() {
		return new AnyCOp(
				new OpBE<AnyOp>(null, this) {
					@Override
					protected AnyOp write() {
						return part().underlying().nullPtr();
					}
				},
				CONSTANT_ALLOC_CLASS,
				getBackend().getGenerator().getGlobals().nullPtr());
	}

	@Override
	public final DataCOp nullDataPtr() {
		return new DataCOp(
				new OpBE<DataOp>(null, this) {
					@Override
					protected DataOp write() {
						return part().underlying().nullDataPtr();
					}
				},
				CONSTANT_ALLOC_CLASS,
				getBackend().getGenerator().getGlobals().nullDataPtr());
	}

	@Override
	public final <S extends StructOp<S>> S nullPtr(DataAllocation<S> type) {

		final ContainerCDAlloc<S> typeAlloc = (ContainerCDAlloc<S>) type;
		final Type<S> originalType = typeAlloc.getType();

		return originalType.op(new CStruct<S>(
				new OpBE<S>(null, this) {
					@Override
					protected S write() {

						final CType<S> underlyingType =
								typeAlloc.getUnderlyingInstance();

						return part().underlying().nullPtr(underlyingType);
					}
				},
				CONSTANT_ALLOC_CLASS,
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
						return part().underlying().nullPtr(
								getBackend().underlying(signature));
					}
				},
				signature,
				getBackend()
				.getGenerator()
				.getFunctions()
				.nullPtr(signature));
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
						return (O) part().underlying().phi(
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
						 return part().underlying().phi(
									getId(),
									cop1.backend().underlying(),
									cop2.backend().underlying());
					}
				},
				null);
	}

	public final CCodePart<?> op(InstrBE op) {
		return record(op);
	}

	@Override
	public String toString() {
		if (this.code == null) {
			return super.toString();
		}
		return this.code.toString();
	}

	final CCodePart<?> record(OpRecord op) {

		final CCodePart<?> part = nextPart();

		part.add(op);

		return part;
	}

	private final <CC extends CCode<?>> CC recordCode(CC code) {
		record(code.nextPart());
		return code;
	}

}
