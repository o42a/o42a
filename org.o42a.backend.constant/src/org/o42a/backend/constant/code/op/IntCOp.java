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
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.IntOp;
import org.o42a.util.string.ID;


public abstract class IntCOp<U extends IntOp<U>, T extends Number>
		extends NumCOp<U, T>
		implements IntOp<U> {

	public IntCOp(OpBE<U> backend) {
		super(backend);
	}

	public IntCOp(ID id, CCode<?> code, T constant) {
		super(id, code, constant);
	}

	public IntCOp(OpBE<U> backend, T constant) {
		super(backend, constant);
	}

	@Override
	public final U shl(final ID id, final Code code, final U numBits) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, SHL_ID, this, numBits);
		@SuppressWarnings("unchecked")
		final IntCOp<U, T> nb = (IntCOp<U, T>) numBits;

		if (isConstant() && nb.isConstant()) {

			final T shl = shl(getConstant(), nb.getConstant().intValue());

			return create(constant(resultId, ccode, shl), shl);
		}

		return create(
				new OpBE<U>(resultId, ccode) {
					@Override
					public void prepare() {
						use(backend());
						use(nb);
					}
					@Override
					protected U write() {
						return backend().underlying().shl(
								getId(),
								part().underlying(),
								nb.backend().underlying());
					}
				},
				null);
	}

	@Override
	public final U shl(final ID id, final Code code, final int numBits) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, SHL_ID, this, numBits);

		if (isConstant()) {

			final T shl = shl(getConstant(), Integer.valueOf(numBits));

			return create(constant(resultId, ccode, shl), shl);
		}

		return create(
				new OpBE<U>(resultId, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected U write() {
						return backend().underlying().shl(
								getId(),
								part().underlying(),
								numBits);
					}
				},
				null);
	}

	@Override
	public final U lshr(final ID id, final Code code, final U numBits) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, LSHR_ID, this, numBits);
		@SuppressWarnings("unchecked")
		final IntCOp<U, T> nb = (IntCOp<U, T>) numBits;

		if (isConstant() && nb.isConstant()) {

			final T lshr = lshr(getConstant(), nb.getConstant().intValue());

			return create(constant(resultId, ccode, lshr), lshr);
		}

		return create(
				new OpBE<U>(resultId, ccode) {
					@Override
					public void prepare() {
						use(backend());
						use(nb);
					}
					@Override
					protected U write() {
						return backend().underlying().lshr(
								getId(),
								part().underlying(),
								nb.backend().underlying());
					}
				},
				null);
	}

	@Override
	public final U lshr(final ID id, final Code code, final int numBits) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, LSHR_ID, this, numBits);

		if (isConstant()) {

			final T lshr = lshr(getConstant(), Integer.valueOf(numBits));

			return create(constant(resultId, ccode, lshr), lshr);
		}

		return create(
				new OpBE<U>(resultId, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected U write() {
						return backend().underlying().lshr(
								getId(),
								part().underlying(),
								numBits);
					}
				},
				null);
	}

	@Override
	public final U ashr(final ID id, final Code code, final U numBits) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, ASHR_ID, this, numBits);
		@SuppressWarnings("unchecked")
		final IntCOp<U, T> nb = (IntCOp<U, T>) numBits;

		if (isConstant() && nb.isConstant()) {

			final T ashr = ashr(getConstant(), nb.getConstant().intValue());

			return create(constant(resultId, ccode, ashr), ashr);
		}

		return create(
				new OpBE<U>(resultId, ccode) {
					@Override
					public void prepare() {
						use(backend());
						use(nb);
					}
					@Override
					protected U write() {
						return backend().underlying().ashr(
								getId(),
								part().underlying(),
								nb.backend().underlying());
					}
				},
				null);
	}

	@Override
	public final U ashr(final ID id, final Code code, final int numBits) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, ASHR_ID, this, numBits);

		if (isConstant()) {

			final T ashr = ashr(getConstant(), Integer.valueOf(numBits));

			return create(constant(resultId, ccode, ashr), ashr);
		}

		return create(
				new OpBE<U>(resultId, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected U write() {
						return backend().underlying().ashr(
								getId(),
								part().underlying(),
								numBits);
					}
				},
				null);
	}

	@Override
	public final U and(final ID id, final Code code, final U operand) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, AND_ID, this, operand);
		@SuppressWarnings("unchecked")
		final IntCOp<U, T> op = (IntCOp<U, T>) operand;

		if (isConstant() && op.isConstant()) {

			final T and = and(getConstant(), op.getConstant());

			return create(constant(resultId, ccode, and), and);
		}

		return create(
				new OpBE<U>(resultId, ccode) {
					@Override
					public void prepare() {
						use(backend());
						use(op);
					}
					@Override
					protected U write() {
						return backend().underlying().and(
								getId(),
								part().underlying(),
								op.backend().underlying());
					}
				},
				null);
	}

	@Override
	public final U or(final ID id, final Code code, final U operand) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, OR_ID, this, operand);
		@SuppressWarnings("unchecked")
		final IntCOp<U, T> op = (IntCOp<U, T>) operand;

		if (isConstant() && op.isConstant()) {

			final T or = or(getConstant(), op.getConstant());

			return create(constant(resultId, ccode, or), or);
		}

		return create(
				new OpBE<U>(resultId, ccode) {
					@Override
					public void prepare() {
						use(backend());
						use(op);
					}
					@Override
					protected U write() {
						return backend().underlying().or(
								getId(),
								part().underlying(),
								op.backend().underlying());
					}
				},
				null);
	}

	@Override
	public final U xor(final ID id, final Code code, final U operand) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, XOR_ID, this, operand);
		@SuppressWarnings("unchecked")
		final IntCOp<U, T> op = (IntCOp<U, T>) operand;

		if (isConstant() && op.isConstant()) {

			final T xor = xor(getConstant(), op.getConstant());

			return create(constant(resultId, ccode, xor), xor);
		}

		return create(
				new OpBE<U>(resultId, ccode) {
					@Override
					public void prepare() {
						use(backend());
						use(op);
					}
					@Override
					protected U write() {
						return backend().underlying().xor(
								getId(),
								part().underlying(),
								op.backend().underlying());
					}
				},
				null);
	}

	@Override
	public final U comp(final ID id, final Code code) {

		final CCode<?> ccode = cast(code);
		final ID resultId = code.getOpNames().unaryId(id, COMP_ID, this);

		if (isConstant()) {

			final T comp = comp(getConstant());

			return create(constant(resultId, ccode, comp), comp);
		}

		return create(
				new OpBE<U>(resultId, ccode) {
					@Override
					public void prepare() {
						use(backend());
					}
					@Override
					protected U write() {
						return backend().underlying().comp(
								getId(),
								part().underlying());
					}
				},
				null);
	}

	@Override
	public final BoolOp lowestBit(final ID id, final Code code) {

		final CCode<?> ccode = cast(code);
		final ID resultId = code.getOpNames().castId(id, BOOL_ID, this);

		if (isConstant()) {

			final boolean lowestBit = (getConstant().intValue() & 1) != 0;

			return new BoolCOp(resultId, ccode, lowestBit);
		}

		return new BoolCOp(new OpBE<BoolOp>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
			}
			@Override
			protected BoolOp write() {
				return backend().underlying().lowestBit(
						getId(),
						part().underlying());
			}
		});
	}

	protected abstract T shl(T value, int numBits);

	protected abstract T lshr(T value, int numBits);

	protected abstract T ashr(T value, int numBits);

	protected abstract T and(T value1, T value2);

	protected abstract T or(T value1, T value2);

	protected abstract T xor(T value1, T value2);

	protected abstract T comp(T value);

}
