/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.backend.constant.code.rec.RecStore.autoRecStore;
import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.backend.constant.data.struct.StructStore.autoStructStore;
import static org.o42a.codegen.code.op.Op.PHI_ID;
import static org.o42a.codegen.data.AllocPlace.constantAllocPlace;

import java.lang.reflect.Array;

import org.o42a.backend.constant.code.op.*;
import org.o42a.backend.constant.code.rec.AnyRecCOp;
import org.o42a.backend.constant.code.rec.StructRecCOp;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.ContainerCDAlloc;
import org.o42a.backend.constant.data.func.CFAlloc;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.backend.constant.data.struct.CType;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.Fn;
import org.o42a.codegen.code.Signature;
import org.o42a.codegen.code.backend.CodeWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.codegen.data.backend.DataAllocation;
import org.o42a.codegen.data.backend.FuncAllocation;
import org.o42a.util.string.ID;


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

	public abstract CBlock<?> block();

	public abstract CCodePart<?> nextPart();

	@Override
	public final ID getId() {
		return code().getId();
	}

	@Override
	public final <F extends Fn<F>> CFunc<F> caller(
			ID id,
			FuncAllocation<F> allocation) {

		final CFAlloc<F> alloc = cast(allocation);

		return new CFunc<>(
				new OpBE<F>(id, this) {
					@Override
					public void prepare() {
					}
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
		return new CInset(this, code);
	}

	@Override
	public final Int8cOp int8(byte value) {
		return new Int8cOp(code().getOpNames().opId(null), this, value);
	}

	@Override
	public final Int16cOp int16(short value) {
		return new Int16cOp(code().getOpNames().opId(null), this, value);
	}

	@Override
	public final Int32cOp int32(int value) {
		return new Int32cOp(code().getOpNames().opId(null), this, value);
	}

	@Override
	public final Int64cOp int64(long value) {
		return new Int64cOp(code().getOpNames().opId(null), this, value);
	}

	@Override
	public final Fp32cOp fp32(float value) {
		return new Fp32cOp(code().getOpNames().opId(null), this, value);
	}

	@Override
	public final Fp64cOp fp64(double value) {
		return new Fp64cOp(code().getOpNames().opId(null), this, value);
	}

	@Override
	public final BoolCOp bool(boolean value) {
		return new BoolCOp(code().getOpNames().opId(null), this, value);
	}

	@Override
	public final RelCOp nullRelPtr() {
		return new RelCOp(
				new OpBE<RelOp>(code().getOpNames().opId(null), this) {
					@Override
					public void prepare() {
					}
					@Override
					protected RelOp write() {
						return part().underlying().nullRelPtr();
					}
				});
	}

	@Override
	public final AnyCOp nullPtr() {
		return new AnyCOp(
				new OpBE<AnyOp>(code().getOpNames().opId(null), this) {
					@Override
					public void prepare() {
					}
					@Override
					protected AnyOp write() {
						return part().underlying().nullPtr();
					}
				},
				constantAllocPlace(),
				getBackend().getGenerator().getGlobals().nullPtr());
	}

	@Override
	public final DataCOp nullDataPtr() {
		return new DataCOp(
				new OpBE<DataOp>(code().getOpNames().opId(null), this) {
					@Override
					public void prepare() {
					}
					@Override
					protected DataOp write() {
						return part().underlying().nullDataPtr();
					}
				},
				constantAllocPlace(),
				getBackend().getGenerator().getGlobals().nullDataPtr());
	}

	@Override
	public final <S extends StructOp<S>> S nullPtr(DataAllocation<S> type) {

		final ContainerCDAlloc<S> typeAlloc = (ContainerCDAlloc<S>) type;
		final Type<S> originalType = typeAlloc.getType();

		return originalType.op(new CStruct<>(
				new OpBE<S>(code().getOpNames().opId(null), this) {
					@Override
					public void prepare() {
					}
					@Override
					protected S write() {

						final CType<S> underlyingType =
								typeAlloc.getUnderlyingInstance();

						return part().underlying().nullPtr(underlyingType);
					}
				},
				autoStructStore(code()),
				originalType,
				getBackend().getGenerator().getGlobals().nullPtr(
						originalType)));
	}

	@Override
	public final <F extends Fn<F>> CFunc<F> nullPtr(
			final Signature<F> signature) {
		return new CFunc<>(
				new OpBE<F>(code().getOpNames().opId(null), this) {
					@Override
					public void prepare() {
					}
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
	public final AnyRecCOp allocatePtr(ID id) {
		return allocated(new AnyRecCOp(
				new OpBE<AnyRecOp>(id, this) {
					@Override
					public void prepare() {
					}
					@Override
					protected AnyRecOp write() {
						return part()
								.underlying()
								.writer()
								.allocatePtr(getId());
					}
				},
				autoRecStore(code())));
	}

	@Override
	public final <S extends StructOp<S>> StructRecCOp<S> allocatePtr(
			ID id,
			DataAllocation<S> typeAllocation) {

		final ContainerCDAlloc<S> typeAlloc =
				(ContainerCDAlloc<S>) typeAllocation;

		return allocated(new StructRecCOp<>(
				new OpBE<StructRecOp<S>>(id, this) {
					@Override
					public void prepare() {
					}
					@Override
					protected StructRecOp<S> write() {
						return part().underlying().writer().allocatePtr(
								getId(),
								typeAlloc.getUnderlyingPtr().getAllocation());
					}
				},
				autoRecStore(code()),
				typeAlloc.getType()));
	}

	@Override
	public final <S extends StructOp<S>> S allocateStruct(
			ID id,
			DataAllocation<S> typeAllocation) {

		final ContainerCDAlloc<S> typeAlloc =
				(ContainerCDAlloc<S>) typeAllocation;
		final Type<S> type = typeAlloc.getType();

		return type.op(allocated(new CStruct<>(
				new OpBE<S>(id, this) {
					@Override
					public void prepare() {
					}
					@Override
					protected S write() {
						return part().underlying().writer().allocateStruct(
								getId(),
								typeAlloc.getUnderlyingPtr().getAllocation());
					}
				},
				autoStructStore(code()),
				type)));
	}

	@Override
	@SuppressWarnings("unchecked")
	public final <O extends Op> O phi(ID id, O op) {

		final COp<O, ?> cop = cast(op);
		@SuppressWarnings("rawtypes")
		final COp res = cop;

		return (O) res.create(
				new AliasBE<>(id, this, cop.backend()),
				cop.getConstant());
	}

	@Override
	public final <O extends Op> O phi(ID id, O op1, O op2) {

		final COp<O, ?> cop1 = cast(op1);
		final COp<O, ?> cop2 = cast(op2);

		if (cop1.isConstant() && cop2.isConstant()) {
			if (cop1.getConstant().equals(cop2.getConstant())) {
				return phi(id, op1);
			}
		}

		final ID resultId =
				code().getOpNames().binaryId(id, PHI_ID, op1, op2);

		return cop1.create(
				new OpBE<O>(resultId, this) {
					@Override
					public void prepare() {
						use(cop1);
						use(cop2);
					}
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

	@Override
	public <O extends Op> O phi(ID id, final O[] ops) {

		final ID resultId = code().getOpNames().opId(id);
		@SuppressWarnings("unchecked")
		final COp<O, ?>[] cops = new COp[ops.length];

		for (int i = 0; i < ops.length; ++i) {
			cops[i] = cast(ops[i]);
		}

		return cops[0].create(
				new OpBE<O>(resultId, this) {
					@Override
					public void prepare() {
						for (COp<O, ?> cop : cops) {
							use(cop);
						}
					}
					@SuppressWarnings("unchecked")
					@Override
					protected O write() {

						O[] uops = null;

						for (int i = 0; i < cops.length; ++i) {

							final O uop = cops[i].backend().underlying();

							if (uops == null) {
								uops = (O[]) Array.newInstance(
										uop.getClass(),
										cops.length);
							}

							uops[i] = uop;
						}

						return part().underlying().phi(getId(), uops);
					}
				},
				null);
	}

	@Override
	public final void acquireBarrier() {
		new BaseInstrBE(this) {
			@Override
			public void prepare() {
				alwaysEmit();
			}
			@Override
			protected void emit() {
				part().underlying().acquireBarrier();
			}
		};
	}

	@Override
	public final void releaseBarrier() {
		new BaseInstrBE(this) {
			@Override
			public void prepare() {
				alwaysEmit();
			}
			@Override
			protected void emit() {
				part().underlying().releaseBarrier();
			}
		};
	}

	@Override
	public final void fullBarrier() {
		new BaseInstrBE(this) {
			@Override
			public void prepare() {
				alwaysEmit();
			}
			@Override
			protected void emit() {
				part().underlying().fullBarrier();
			}
		};
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

	final CCodePart<?> inset(CInset inset) {
		return record(inset.nextPart());
	}

	final CCodePart<?> record(OpRecord op) {

		final CCodePart<?> part = nextPart();

		part.add(op);

		return part;
	}

	private final <O extends AllocPtrCOp<P>, P extends AllocPtrOp<P>>
			O allocated(O op) {

		final CBlock<?> allocator =
				(CBlock<?>) code().getAllocator().writer();

		allocator.allocate(op);

		return op;
	}

}
