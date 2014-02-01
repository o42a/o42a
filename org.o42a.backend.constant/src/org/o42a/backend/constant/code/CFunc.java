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

import static org.o42a.backend.constant.data.ConstBackend.cast;
import static org.o42a.codegen.data.AllocPlace.constantAllocPlace;

import org.o42a.backend.constant.code.op.*;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;
import org.o42a.util.string.ID;


public final class CFunc<F extends Func<F>>
		extends PtrCOp<F, FuncPtr<F>>
		implements FuncCaller<F> {

	private final Signature<F> signature;

	public CFunc(OpBE<F> backend, Signature<F> signature) {
		super(backend);
		this.signature = signature;
	}

	public CFunc(OpBE<F> backend, Signature<F> signature, FuncPtr<F> constant) {
		super(backend, constant);
		this.signature = signature;
	}

	public final boolean hasSideEffects() {

		final FuncPtr<F> constant = getConstant();

		return constant == null || constant.hasSideEffects();
	}

	@Override
	public final Signature<F> getSignature() {
		return this.signature;
	}

	@Override
	public F create(OpBE<F> backend, FuncPtr<F> constant) {
		return getSignature().op(
				new CFunc<>(backend, getSignature(), constant));
	}

	@Override
	public final void call(final Code code, final Op... args) {
		new BaseInstrBE(cast(code)) {
			@Override
			public void prepare() {
				if (hasSideEffects()) {
					// The function call with side effects is always emitted.
					alwaysEmit();
				}
				use(backend());
				useArgs(this, args);
			}
			@Override
			protected void emit() {
				backend().underlying().caller().call(
						part().underlying(),
						underlyingArgs(args));
			}
		};
	}

	@Override
	public final Int8cOp callInt8(
			final ID id,
			final Code code,
			final Op... args) {
		return new Int8cOp(new OpBE<Int8op>(id, cast(code)) {
			@Override
			public void prepare() {
				if (hasSideEffects()) {
					// The function call with side effects is always emitted.
					alwaysEmit();
				}
				use(backend());
				useArgs(this, args);
			}
			@Override
			protected Int8op write() {
				return backend().underlying().caller().callInt8(
						getId(),
						part().underlying(),
						underlyingArgs(args));
			}
		});
	}

	@Override
	public final Int16cOp callInt16(
			final ID id,
			final Code code,
			final Op... args) {
		return new Int16cOp(new OpBE<Int16op>(id, cast(code)) {
			@Override
			public void prepare() {
				if (hasSideEffects()) {
					// The function call with side effects is always emitted.
					alwaysEmit();
				}
				use(backend());
				useArgs(this, args);
			}
			@Override
			protected Int16op write() {
				return backend().underlying().caller().callInt16(
						getId(),
						part().underlying(),
						underlyingArgs(args));
			}
		});
	}

	@Override
	public final Int32cOp callInt32(
			final ID id,
			final Code code,
			final Op... args) {
		return new Int32cOp(new OpBE<Int32op>(id, cast(code)) {
			@Override
			public void prepare() {
				if (hasSideEffects()) {
					// The function call with side effects is always emitted.
					alwaysEmit();
				}
				use(backend());
				useArgs(this, args);
			}
			@Override
			protected Int32op write() {
				return backend().underlying().caller().callInt32(
						getId(),
						part().underlying(),
						underlyingArgs(args));
			}
		});
	}

	@Override
	public final Int64cOp callInt64(
			final ID id,
			final Code code,
			final Op... args) {
		return new Int64cOp(new OpBE<Int64op>(id, cast(code)) {
			@Override
			public void prepare() {
				if (hasSideEffects()) {
					// The function call with side effects is always emitted.
					alwaysEmit();
				}
				use(backend());
				useArgs(this, args);
			}
			@Override
			protected Int64op write() {
				return backend().underlying().caller().callInt64(
						getId(),
						part().underlying(),
						underlyingArgs(args));
			}
		});
	}

	@Override
	public final Fp32cOp callFp32(
			final ID id,
			final Code code,
			final Op... args) {
		return new Fp32cOp(new OpBE<Fp32op>(id, cast(code)) {
			@Override
			public void prepare() {
				if (hasSideEffects()) {
					// The function call with side effects is always emitted.
					alwaysEmit();
				}
				use(backend());
				useArgs(this, args);
			}
			@Override
			protected Fp32op write() {
				return backend().underlying().caller().callFp32(
						getId(),
						part().underlying(),
						underlyingArgs(args));
			}
		});
	}

	@Override
	public final Fp64cOp callFp64(
			final ID id,
			final Code code,
			final Op... args) {
		return new Fp64cOp(new OpBE<Fp64op>(id, cast(code)) {
			@Override
			public void prepare() {
				if (hasSideEffects()) {
					// The function call with side effects is always emitted.
					alwaysEmit();
				}
				use(backend());
				useArgs(this, args);
			}
			@Override
			protected Fp64op write() {
				return backend().underlying().caller().callFp64(
						getId(),
						part().underlying(),
						underlyingArgs(args));
			}
		});
	}

	@Override
	public final BoolCOp callBool(
			final ID id,
			final Code code,
			final Op... args) {
		return new BoolCOp(new OpBE<BoolOp>(id, cast(code)) {
			@Override
			public void prepare() {
				if (hasSideEffects()) {
					// The function call with side effects is always emitted.
					alwaysEmit();
				}
				use(backend());
				useArgs(this, args);
			}
			@Override
			protected BoolOp write() {
				return backend().underlying().caller().callBool(
						getId(),
						part().underlying(),
						underlyingArgs(args));
			}
		});
	}

	@Override
	public final AnyCOp callAny(
			final ID id,
			final Code code,
			final Op... args) {
		return new AnyCOp(
				new OpBE<AnyOp>(id, cast(code)) {
					@Override
					public void prepare() {
						if (hasSideEffects()) {
							// The function call with side effects
							// is always emitted.
							alwaysEmit();
						}
						use(backend());
						useArgs(this, args);
					}
					@Override
					protected AnyOp write() {
						return backend().underlying().caller().callAny(
								getId(),
								part().underlying(),
								underlyingArgs(args));
					}
				},
				null);
	}

	@Override
	public final DataCOp callData(
			final ID id,
			final Code code,
			final Op... args) {
		return new DataCOp(
				new OpBE<DataOp>(id, cast(code)) {
					@Override
					public void prepare() {
						if (hasSideEffects()) {
							// The function call with side effects
							// is always emitted.
							alwaysEmit();
						}
						use(backend());
						useArgs(this, args);
					}
					@Override
					protected DataOp write() {
						return backend().underlying().caller().callData(
								getId(),
								part().underlying(),
								underlyingArgs(args));
					}
				},
				null);
	}

	@Override
	public <S extends StructOp<S>> S callPtr(
			final ID id,
			final Code code,
			final Type<S> type,
			final Op... args) {
		return type.op(new CStruct<>(
				new OpBE<S>(id, cast(code)) {
					@Override
					public void prepare() {
						if (hasSideEffects()) {
							// The function call with side effects
							// is always emitted.
							alwaysEmit();
						}
						use(backend());
						useArgs(this, args);
					}
					@Override
					protected S write() {
						return backend().underlying().caller().callPtr(
								getId(),
								part().underlying(),
								getBackend().underlying(type),
								underlyingArgs(args));
					}
				},
				null,
				type));
	}

	@Override
	public AnyCOp toAny(ID id, Code code) {

		final ID resultId = code.getOpNames().castId(id, ANY_ID, this);

		return new AnyCOp(
				new OpBE<AnyOp>(resultId, cast(code)) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected AnyOp write() {
						return backend().underlying().toAny(
								getId(),
								part().underlying());
					}
				},
				constantAllocPlace());
	}

	private void useArgs(InstrBE instr, final Op[] args) {
		if (args.length == 0) {
			return;
		}
		for (Op arg : args) {
			instr.use(cast(arg).backend());
		}
	}

	private Op[] underlyingArgs(final Op[] args) {
		if (args.length == 0) {
			return args;
		}

		final Op[] underlying = new Op[args.length];

		for (int i = 0; i < args.length; ++i) {
			underlying[i] = cast(args[i]).backend().underlying();
		}

		return underlying;
	}

}
