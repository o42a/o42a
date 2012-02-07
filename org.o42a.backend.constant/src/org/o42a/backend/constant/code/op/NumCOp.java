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
package org.o42a.backend.constant.code.op;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CBlock;
import org.o42a.backend.constant.code.CCode;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;


public abstract class NumCOp<U extends NumOp<U>, T extends Number>
		extends AbstractCOp<U, T>
		implements NumOp<U> {

	public NumCOp(OpBE<U> backend) {
		super(backend);
	}

	public NumCOp(CodeId id, CCode<?> code, T constant) {
		super(new NumConstBE<U, T>(id, code, constant), constant);
		((NumConstBE<?, ?>) backend()).init(this);
	}

	public NumCOp(OpBE<U> backend, T constant) {
		super(backend, constant);
	}

	@Override
	public final U neg(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final T neg = neg(getConstant());

			return create(constant(id, ccode, neg), neg);
		}

		return create(new OpBE<U>(id, ccode) {
			@Override
			protected U write() {
				return backend().underlying().neg(
						getId(),
						code().getUnderlying());
			}
		});
	}

	@Override
	public final U add(CodeId id, Code code, U summand) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> s = (NumCOp<U, T>) summand;

		if (isConstant() && s.isConstant()) {

			final T sum = add(getConstant(), s.getConstant());

			return create(constant(id, ccode, sum), sum);
		}

		return create(new OpBE<U>(id, ccode) {
			@Override
			protected U write() {
				return backend().underlying().add(
						getId(),
						code().getUnderlying(),
						s.backend().underlying());
			}
		});
	}

	@Override
	public final U sub(CodeId id, Code code, U subtrahend) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> s = (NumCOp<U, T>) subtrahend;

		if (isConstant() && s.isConstant()) {

			final T diff = sub(getConstant(), s.getConstant());

			return create(constant(id, ccode, diff), diff);
		}

		return create(new OpBE<U>(id, ccode) {
			@Override
			protected U write() {
				return backend().underlying().sub(
						getId(),
						code().getUnderlying(),
						s.backend().underlying());
			}
		});
	}

	@Override
	public final U mul(CodeId id, Code code, U multiplier) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> m = (NumCOp<U, T>) multiplier;

		if (isConstant() && m.isConstant()) {

			final T mul = mul(getConstant(), m.getConstant());

			return create(constant(id, ccode, mul), mul);
		}

		return create(new OpBE<U>(id, ccode) {
			@Override
			protected U write() {
				return backend().underlying().mul(
						getId(),
						code().getUnderlying(),
						m.backend().underlying());
			}
		});
	}

	@Override
	public final U div(CodeId id, Code code, U divisor) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> d = (NumCOp<U, T>) divisor;

		if (isConstant() && d.isConstant()) {

			final T div = div(getConstant(), d.getConstant());

			return create(constant(id, ccode, div), div);
		}

		return create(new OpBE<U>(id, ccode) {
			@Override
			protected U write() {
				return backend().underlying().div(
						getId(),
						code().getUnderlying(),
						d.backend().underlying());
			}
		});
	}

	@Override
	public final U rem(CodeId id, Code code, U divisor) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> d = (NumCOp<U, T>) divisor;

		if (isConstant() && d.isConstant()) {

			final T rem = rem(getConstant(), d.getConstant());

			return create(constant(id, ccode, rem), rem);
		}

		return create(new OpBE<U>(id, ccode) {
			@Override
			protected U write() {
				return backend().underlying().rem(
						getId(),
						code().getUnderlying(),
						d.backend().underlying());
			}
		});
	}

	@Override
	public final BoolOp eq(CodeId id, Code code, U other) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> o = (NumCOp<U, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean eq = getConstant().equals(o.getConstant());

			return new BoolCOp(id, ccode, eq);
		}

		return new BoolCOp(new OpBE<BoolOp>(id, ccode) {
			@Override
			protected BoolOp write() {
				return backend().underlying().eq(
						getId(),
						code().getUnderlying(),
						o.backend().underlying());
			}
		});
	}

	@Override
	public final BoolOp ne(CodeId id, Code code, U other) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> o = (NumCOp<U, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean ne = !getConstant().equals(o.getConstant());

			return new BoolCOp(id, ccode, ne);
		}

		return new BoolCOp(new OpBE<BoolOp>(id, ccode) {
			@Override
			protected BoolOp write() {
				return backend().underlying().ne(
						getId(),
						code().getUnderlying(),
						o.backend().underlying());
			}
		});
	}

	@Override
	public final BoolOp gt(CodeId id, Code code, U other) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> o = (NumCOp<U, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean gt = cmp(getConstant(), o.getConstant()) > 0;

			return new BoolCOp(id, ccode, gt);
		}

		return new BoolCOp(new OpBE<BoolOp>(id, ccode) {
			@Override
			protected BoolOp write() {
				return backend().underlying().gt(
						getId(),
						code().getUnderlying(),
						o.backend().underlying());
			}
		});
	}

	@Override
	public BoolOp ge(CodeId id, Code code, U other) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> o = (NumCOp<U, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean ge = cmp(getConstant(), o.getConstant()) >= 0;

			return new BoolCOp(id, ccode, ge);
		}

		return new BoolCOp(new OpBE<BoolOp>(id, ccode) {
			@Override
			protected BoolOp write() {
				return backend().underlying().ge(
						getId(),
						code().getUnderlying(),
						o.backend().underlying());
			}
		});
	}

	@Override
	public BoolOp lt(CodeId id, Code code, U other) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> o = (NumCOp<U, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean lt = cmp(getConstant(), o.getConstant()) < 0;

			return new BoolCOp(id, ccode, lt);
		}

		return new BoolCOp(new OpBE<BoolOp>(id, ccode) {
			@Override
			protected BoolOp write() {
				return backend().underlying().lt(
						getId(),
						code().getUnderlying(),
						o.backend().underlying());
			}
		});
	}

	@Override
	public BoolOp le(CodeId id, Code code, U other) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> o = (NumCOp<U, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean le = cmp(getConstant(), o.getConstant()) <= 0;

			return new BoolCOp(id, ccode, le);
		}

		return new BoolCOp(new OpBE<BoolOp>(id, ccode) {
			@Override
			protected BoolOp write() {
				return backend().underlying().le(
						getId(),
						code().getUnderlying(),
						o.backend().underlying());
			}
		});
	}

	@Override
	public final Int8op toInt8(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {
			return new Int8cOp(id, ccode, getConstant().byteValue());
		}

		return new Int8cOp(new OpBE<Int8op>(id, ccode) {
			@Override
			protected Int8op write() {
				return backend().underlying().toInt8(
						getId(),
						code().getUnderlying());
			}
		});
	}

	@Override
	public final Int16op toInt16(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {
			return new Int16cOp(id, ccode, getConstant().shortValue());
		}

		return new Int16cOp(new OpBE<Int16op>(id, ccode) {
			@Override
			protected Int16op write() {
				return backend().underlying().toInt16(
						getId(),
						code().getUnderlying());
			}
		});
	}

	@Override
	public final Int32op toInt32(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {
			return new Int32cOp(id, ccode, getConstant().intValue());
		}

		return new Int32cOp(new OpBE<Int32op>(id, ccode) {
			@Override
			protected Int32op write() {
				return backend().underlying().toInt32(
						getId(),
						code().getUnderlying());
			}
		});
	}

	@Override
	public final Int64op toInt64(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {
			return new Int64cOp(id, ccode, getConstant().longValue());
		}

		return new Int64cOp(new OpBE<Int64op>(id, ccode) {
			@Override
			protected Int64op write() {
				return backend().underlying().toInt64(
						getId(),
						code().getUnderlying());
			}
		});
	}

	@Override
	public final Fp32op toFp32(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {
			return new Fp32cOp(id, ccode, getConstant().floatValue());
		}

		return new Fp32cOp(new OpBE<Fp32op>(id, ccode) {
			@Override
			protected Fp32op write() {
				return backend().underlying().toFp32(
						getId(),
						code().getUnderlying());
			}
		});
	}

	@Override
	public final Fp64op toFp64(CodeId id, Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {
			return new Fp64cOp(id, ccode, getConstant().doubleValue());
		}

		return new Fp64cOp(new OpBE<Fp64op>(id, ccode) {
			@Override
			protected Fp64op write() {
				return backend().underlying().toFp64(
						getId(),
						code().getUnderlying());
			}
		});
	}

	@Override
	public final void returnValue(Block code) {

		final CBlock<?> ccode = cast(code);

		ccode.beforeReturn();
		new TermBE(ccode) {
			@Override
			public void reveal() {
				backend().underlying().returnValue(code().getUnderlying());
			}
		};
	}

	protected abstract U underlyingConstant(CCode<?> code, T constant);

	protected abstract T neg(T value);

	protected abstract T add(T value1, T value2);

	protected abstract T sub(T value1, T value2);

	protected abstract T mul(T value1, T value2);

	protected abstract T div(T value1, T value2);

	protected abstract T rem(T value1, T value2);

	protected abstract int cmp(T value1, T value2);

	protected OpBE<U> constant(CodeId id, CCode<?> code, T constant) {
		return new NumConstBE<U, T>(id, code, constant, this);
	}

	private static final class NumConstBE<
			U extends NumOp<U>,
			T extends Number>
					extends ConstBE<U, T> {

		private NumCOp<U, T> op;

		NumConstBE(CodeId id, CCode<?> code, T constant) {
			super(id, code, constant);
		}

		NumConstBE(CodeId id, CCode<?> code, T constant, NumCOp<U, T> op) {
			super(id, code, constant);
			this.op = op;
		}

		@Override
		protected U write() {
			return this.op.underlyingConstant(code(), this.constant);
		}

		@SuppressWarnings("unchecked")
		final void init(NumCOp<?, ?> op) {
			this.op = (NumCOp<U, T>) op;
		}

	}

}
