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
package org.o42a.backend.constant.code.op;

import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.CCodePart;
import org.o42a.backend.constant.code.ReturnBE;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.*;
import org.o42a.util.string.ID;


public abstract class NumCOp<U extends NumOp<U>, T extends Number>
		extends COp<U, T>
		implements NumOp<U> {

	public NumCOp(OpBE<U> backend) {
		super(backend);
	}

	public NumCOp(ID id, CCode<?> code, T constant) {
		super(new NumConstBE<U, T>(id, code, constant), constant);
		((NumConstBE<?, ?>) backend()).init(this);
	}

	public NumCOp(OpBE<U> backend, T constant) {
		super(backend, constant);
	}

	@Override
	public final U neg(ID id, Code code) {

		final CCode<?> ccode = cast(code);
		final ID resultId = code.getOpNames().unaryId(id, NEG_ID, this);

		if (isConstant()) {

			final T neg = neg(getConstant());

			return create(constant(resultId, ccode, neg), neg);
		}

		return create(new OpBE<U>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
			}
			@Override
			protected U write() {
				return backend().underlying().neg(
						getId(),
						part().underlying());
			}
		});
	}

	@Override
	public final U add(ID id, Code code, U summand) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, ADD_ID, this, summand);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> s = (NumCOp<U, T>) summand;

		if (isConstant() && s.isConstant()) {

			final T sum = add(getConstant(), s.getConstant());

			return create(constant(resultId, ccode, sum), sum);
		}

		return create(new OpBE<U>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
				use(s);
			}
			@Override
			protected U write() {
				return backend().underlying().add(
						getId(),
						part().underlying(),
						s.backend().underlying());
			}
		});
	}

	@Override
	public final U sub(ID id, Code code, U subtrahend) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, SUB_ID, this, subtrahend);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> s = (NumCOp<U, T>) subtrahend;

		if (isConstant() && s.isConstant()) {

			final T diff = sub(getConstant(), s.getConstant());

			return create(constant(resultId, ccode, diff), diff);
		}

		return create(new OpBE<U>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
				use(s);
			}
			@Override
			protected U write() {
				return backend().underlying().sub(
						getId(),
						part().underlying(),
						s.backend().underlying());
			}
		});
	}

	@Override
	public final U mul(ID id, Code code, U multiplier) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, MUL_ID, this, multiplier);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> m = (NumCOp<U, T>) multiplier;

		if (isConstant() && m.isConstant()) {

			final T mul = mul(getConstant(), m.getConstant());

			return create(constant(resultId, ccode, mul), mul);
		}

		return create(new OpBE<U>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
				use(m);
			}
			@Override
			protected U write() {
				return backend().underlying().mul(
						getId(),
						part().underlying(),
						m.backend().underlying());
			}
		});
	}

	@Override
	public final U div(ID id, Code code, U divisor) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, DIV_ID, this, divisor);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> d = (NumCOp<U, T>) divisor;

		if (isConstant() && d.isConstant()) {

			final T div = div(getConstant(), d.getConstant());

			return create(constant(resultId, ccode, div), div);
		}

		return create(new OpBE<U>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
				use(d);
			}
			@Override
			protected U write() {
				return backend().underlying().div(
						getId(),
						part().underlying(),
						d.backend().underlying());
			}
		});
	}

	@Override
	public final U rem(ID id, Code code, U divisor) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, REM_ID, this, divisor);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> d = (NumCOp<U, T>) divisor;

		if (isConstant() && d.isConstant()) {

			final T rem = rem(getConstant(), d.getConstant());

			return create(constant(resultId, ccode, rem), rem);
		}

		return create(new OpBE<U>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
				use(d);
			}
			@Override
			protected U write() {
				return backend().underlying().rem(
						getId(),
						part().underlying(),
						d.backend().underlying());
			}
		});
	}

	@Override
	public final BoolOp eq(ID id, Code code, U other) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, EQ_ID, this, other);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> o = (NumCOp<U, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean eq = getConstant().equals(o.getConstant());

			return new BoolCOp(resultId, ccode, eq);
		}

		return new BoolCOp(new OpBE<BoolOp>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
				use(o);
			}
			@Override
			protected BoolOp write() {
				return backend().underlying().eq(
						getId(),
						part().underlying(),
						o.backend().underlying());
			}
		});
	}

	@Override
	public final BoolOp ne(ID id, Code code, U other) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, NE_ID, this, other);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> o = (NumCOp<U, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean ne = !getConstant().equals(o.getConstant());

			return new BoolCOp(resultId, ccode, ne);
		}

		return new BoolCOp(new OpBE<BoolOp>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
				use(o);
			}
			@Override
			protected BoolOp write() {
				return backend().underlying().ne(
						getId(),
						part().underlying(),
						o.backend().underlying());
			}
		});
	}

	@Override
	public final BoolOp gt(ID id, Code code, U other) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, GT_ID, this, other);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> o = (NumCOp<U, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean gt = cmp(getConstant(), o.getConstant()) > 0;

			return new BoolCOp(resultId, ccode, gt);
		}

		return new BoolCOp(new OpBE<BoolOp>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
				use(o);
			}
			@Override
			protected BoolOp write() {
				return backend().underlying().gt(
						getId(),
						part().underlying(),
						o.backend().underlying());
			}
		});
	}

	@Override
	public BoolOp ge(ID id, Code code, U other) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, GE_ID, this, other);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> o = (NumCOp<U, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean ge = cmp(getConstant(), o.getConstant()) >= 0;

			return new BoolCOp(resultId, ccode, ge);
		}

		return new BoolCOp(new OpBE<BoolOp>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
				use(o);
			}
			@Override
			protected BoolOp write() {
				return backend().underlying().ge(
						getId(),
						part().underlying(),
						o.backend().underlying());
			}
		});
	}

	@Override
	public BoolOp lt(ID id, Code code, U other) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, LT_ID, this, other);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> o = (NumCOp<U, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean lt = cmp(getConstant(), o.getConstant()) < 0;

			return new BoolCOp(resultId, ccode, lt);
		}

		return new BoolCOp(new OpBE<BoolOp>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
				use(o);
			}
			@Override
			protected BoolOp write() {
				return backend().underlying().lt(
						getId(),
						part().underlying(),
						o.backend().underlying());
			}
		});
	}

	@Override
	public BoolOp le(ID id, Code code, U other) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, LE_ID, this, other);
		@SuppressWarnings("unchecked")
		final NumCOp<U, T> o = (NumCOp<U, T>) other;

		if (isConstant() && o.isConstant()) {

			final boolean le = cmp(getConstant(), o.getConstant()) <= 0;

			return new BoolCOp(resultId, ccode, le);
		}

		return new BoolCOp(new OpBE<BoolOp>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
				use(o);
			}
			@Override
			protected BoolOp write() {
				return backend().underlying().le(
						getId(),
						part().underlying(),
						o.backend().underlying());
			}
		});
	}

	@Override
	public final Int8op toInt8(ID id, Code code) {

		final CCode<?> ccode = cast(code);
		final ID resultId = code.getOpNames().castId(id, INT8_ID, this);

		if (isConstant()) {
			return new Int8cOp(resultId, ccode, getConstant().byteValue());
		}

		return new Int8cOp(new OpBE<Int8op>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
			}
			@Override
			protected Int8op write() {
				return backend().underlying().toInt8(
						getId(),
						part().underlying());
			}
		});
	}

	@Override
	public final Int16op toInt16(ID id, Code code) {

		final CCode<?> ccode = cast(code);
		final ID resultId = code.getOpNames().castId(id, INT16_ID, this);

		if (isConstant()) {
			return new Int16cOp(resultId, ccode, getConstant().shortValue());
		}

		return new Int16cOp(new OpBE<Int16op>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
			}
			@Override
			protected Int16op write() {
				return backend().underlying().toInt16(
						getId(),
						part().underlying());
			}
		});
	}

	@Override
	public final Int32op toInt32(ID id, Code code) {

		final CCode<?> ccode = cast(code);
		final ID resultId = code.getOpNames().castId(id, INT32_ID, this);

		if (isConstant()) {
			return new Int32cOp(resultId, ccode, getConstant().intValue());
		}

		return new Int32cOp(new OpBE<Int32op>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
			}
			@Override
			protected Int32op write() {
				return backend().underlying().toInt32(
						getId(),
						part().underlying());
			}
		});
	}

	@Override
	public final Int64op toInt64(ID id, Code code) {

		final CCode<?> ccode = cast(code);
		final ID resultId = code.getOpNames().castId(id, INT64_ID, this);

		if (isConstant()) {
			return new Int64cOp(resultId, ccode, getConstant().longValue());
		}

		return new Int64cOp(new OpBE<Int64op>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
			}
			@Override
			protected Int64op write() {
				return backend().underlying().toInt64(
						getId(),
						part().underlying());
			}
		});
	}

	@Override
	public final Fp32op toFp32(ID id, Code code) {

		final CCode<?> ccode = cast(code);
		final ID resultId = code.getOpNames().castId(id, FP32_ID, this);

		if (isConstant()) {
			return new Fp32cOp(resultId, ccode, getConstant().floatValue());
		}

		return new Fp32cOp(new OpBE<Fp32op>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
			}
			@Override
			protected Fp32op write() {
				return backend().underlying().toFp32(
						getId(),
						part().underlying());
			}
		});
	}

	@Override
	public final Fp64op toFp64(ID id, Code code) {

		final CCode<?> ccode = cast(code);
		final ID resultId = code.getOpNames().castId(id, FP64_ID, this);

		if (isConstant()) {
			return new Fp64cOp(resultId, ccode, getConstant().doubleValue());
		}

		return new Fp64cOp(new OpBE<Fp64op>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
			}
			@Override
			protected Fp64op write() {
				return backend().underlying().toFp64(
						getId(),
						part().underlying());
			}
		});
	}

	@Override
	public final void returnValue(Block code, boolean dispose) {
		new ReturnBE(cast(code).nextPart(), dispose) {
			@Override
			public void prepare() {
				use(backend());
			}
			@Override
			protected void emit() {
				backend().underlying().returnValue(part().underlying());
			}
		};
	}

	protected abstract U underlyingConstant(CCodePart<?> part, T constant);

	protected abstract T neg(T value);

	protected abstract T add(T value1, T value2);

	protected abstract T sub(T value1, T value2);

	protected abstract T mul(T value1, T value2);

	protected abstract T div(T value1, T value2);

	protected abstract T rem(T value1, T value2);

	protected abstract int cmp(T value1, T value2);

	protected final OpBE<U> constant(ID id, CCode<?> code, T constant) {
		return new NumConstBE<>(id, code, constant, this);
	}

	private static final class NumConstBE<
			U extends NumOp<U>,
			T extends Number>
					extends ConstBE<U, T> {

		private NumCOp<U, T> op;

		NumConstBE(ID id, CCode<?> code, T constant) {
			super(id, code, constant);
		}

		NumConstBE(ID id, CCode<?> code, T constant, NumCOp<U, T> op) {
			super(id, code, constant);
			this.op = op;
		}

		@Override
		protected U write() {
			return this.op.underlyingConstant(part(), constant());
		}

		@SuppressWarnings("unchecked")
		final void init(NumCOp<?, ?> op) {
			this.op = (NumCOp<U, T>) op;
		}

	}

}
