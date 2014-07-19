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

import org.o42a.backend.constant.code.op.*;
import org.o42a.backend.constant.code.signature.CSignature;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.backend.constant.data.func.CFAlloc;
import org.o42a.backend.constant.data.func.FunctionCFAlloc;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.codegen.data.Type;


public class CFunction<F extends Func<F>>
		extends CBlock<Function<F>>
		implements FuncWriter<F> {

	private final BeforeReturn beforeReturn;
	private FunctionCFAlloc<F> allocation;
	private Function<F> underlying;
	private byte status;

	CFunction(
			ConstBackend backend,
			Function<F> function,
			BeforeReturn beforeReturn) {
		super(backend, null, function);
		this.beforeReturn = beforeReturn;
	}

	public final Function<F> getUnderlying() {
		if (this.underlying != null) {
			return this.underlying;
		}
		this.underlying = createUnderlying();
		if (this.status < 0) {
			// Function already built, but not emitted yet. Emit it now.
			emit();
		}
		return this.underlying;
	}

	public final void beforeReturn(Code code, boolean dispose) {
		this.beforeReturn.beforeReturn(code, dispose);
	}

	@Override
	public final CFAlloc<F> getAllocation() {
		if (this.allocation != null) {
			return this.allocation;
		}
		return this.allocation = new FunctionCFAlloc<>(this);
	}

	@Override
	public final Int8cOp int8arg(final Code code, final Arg<Int8op> arg) {
		return new Int8cOp(new OpBE<Int8op>(arg.getId(), cast(code)) {
			@Override
			public void prepare() {
			}
			@Override
			protected Int8op write() {
				return getUnderlying().arg(
						part().underlying(),
						underlyingArg(arg));
			}
		});
	}

	@Override
	public final Int16cOp int16arg(final Code code, final Arg<Int16op> arg) {
		return new Int16cOp(new OpBE<Int16op>(arg.getId(), cast(code)) {
			@Override
			public void prepare() {
			}
			@Override
			protected Int16op write() {
				return getUnderlying().arg(
						part().underlying(),
						underlyingArg(arg));
			}
		});
	}

	@Override
	public final Int32cOp int32arg(final Code code, final Arg<Int32op> arg) {
		return new Int32cOp(new OpBE<Int32op>(arg.getId(), cast(code)) {
			@Override
			public void prepare() {
			}
			@Override
			protected Int32op write() {
				return getUnderlying().arg(
						part().underlying(),
						underlyingArg(arg));
			}
		});
	}

	@Override
	public final Int64cOp int64arg(final Code code, final Arg<Int64op> arg) {
		return new Int64cOp(new OpBE<Int64op>(arg.getId(), cast(code)) {
			@Override
			public void prepare() {
			}
			@Override
			protected Int64op write() {
				return getUnderlying().arg(
						part().underlying(),
						underlyingArg(arg));
			}
		});
	}

	@Override
	public final Fp32cOp fp32arg(final Code code, final Arg<Fp32op> arg) {
		return new Fp32cOp(new OpBE<Fp32op>(arg.getId(), cast(code)) {
			@Override
			public void prepare() {
			}
			@Override
			protected Fp32op write() {
				return getUnderlying().arg(
						part().underlying(),
						underlyingArg(arg));
			}
		});
	}

	@Override
	public final Fp64cOp fp64arg(final Code code, final Arg<Fp64op> arg) {
		return new Fp64cOp(new OpBE<Fp64op>(arg.getId(), cast(code)) {
			@Override
			public void prepare() {
			}
			@Override
			protected Fp64op write() {
				return getUnderlying().arg(
						part().underlying(),
						underlyingArg(arg));
			}
		});
	}

	@Override
	public final BoolCOp boolArg(final Code code, final Arg<BoolOp> arg) {
		return new BoolCOp(new OpBE<BoolOp>(arg.getId(), cast(code)) {
			@Override
			public void prepare() {
			}
			@Override
			protected BoolOp write() {
				return getUnderlying().arg(
						part().underlying(),
						underlyingArg(arg));
			}
		});
	}

	@Override
	public final RelCOp relPtrArg(final Code code, final Arg<RelOp> arg) {
		return new RelCOp(new OpBE<RelOp>(arg.getId(), cast(code)) {
			@Override
			public void prepare() {
			}
			@Override
			protected RelOp write() {
				return getUnderlying().arg(
						part().underlying(),
						underlyingArg(arg));
			}
		});
	}

	@Override
	public final AnyCOp ptrArg(final Code code, final Arg<AnyOp> arg) {
		return new AnyCOp(
				new OpBE<AnyOp>(arg.getId(), cast(code)) {
					@Override
					public void prepare() {
					}
					@Override
					protected AnyOp write() {
						return getUnderlying().arg(
								part().underlying(),
								underlyingArg(arg));
					}
				},
				null);
	}

	@Override
	public final DataCOp dataArg(final Code code, final Arg<DataOp> arg) {
		return new DataCOp(
				new OpBE<DataOp>(arg.getId(), cast(code)) {
					@Override
					public void prepare() {
					}
					@Override
					protected DataOp write() {
						return getUnderlying().arg(
								part().underlying(),
								underlyingArg(arg));
					}
				},
				null);
	}

	@Override
	public <S extends StructOp<S>> S ptrArg(
			final Code code,
			final Arg<S> arg,
			final Type<S> type) {
		return type.op(new CStruct<>(
				new OpBE<S>(arg.getId(), cast(code)) {
					@Override
					public void prepare() {
					}
					@Override
					protected S write() {
						return getUnderlying().arg(
								part().underlying(),
								underlyingArg(arg));
					}
				},
				null,
				type));
	}

	@Override
	public <FF extends Func<FF>> FF funcPtrArg(
			final Code code,
			final Arg<FF> arg,
			final Signature<FF> signature) {
		return signature.op(new CFunc<>(
				new OpBE<FF>(arg.getId(), cast(code)) {
					@Override
					public void prepare() {
					}
					@Override
					protected FF write() {
						return getUnderlying().arg(
								part().underlying(),
								underlyingArg(arg));
					}
				},
				signature));
	}

	@Override
	public void done() {
		if (this.status > 0) {
			// Underlying function already emitted.
			return;
		}
		if (this.underlying != null) {
			// Underlying function already created, but not emitted yet.
			emit();
			return;
		}
		this.status = -1;// Emit underlying function when requested.
		if (code().isExported()) {
			getUnderlying();// Eagerly initialize underlying exported functions.
		}
	}

	@Override
	protected CBlockPart createFirstBlock() {
		return new CFunctionPart<>(this);
	}

	private void emit() {
		this.status = 1;
		prepare();
		reveal();
		getUnderlying().done();
		clear();
	}

	private Function<F> createUnderlying() {

		final Function<F> function = code();
		final FunctionSettings underlyingSettings =
				getBackend().getUnderlyingGenerator()
				.newFunction()
				.set(function);
		final CSignature<F> underlyingSignature =
				getBackend().underlying(function.getSignature());

		return underlyingSettings.create(
				function.getId(),
				underlyingSignature);
	}

	@SuppressWarnings("unchecked")
	private final <O extends Op> Arg<O> underlyingArg(Arg<O> arg) {

		final CSignature<F> underlyingSignature =
				getAllocation().getUnderlyingSignature();
		final Generator underlyingGenerator =
				underlyingSignature.getBackend().getUnderlyingGenerator();
		final Arg<?>[] underlyingArgs =
				underlyingSignature.args(underlyingGenerator);

		return (Arg<O>) underlyingArgs[arg.getIndex()];
	}

}
