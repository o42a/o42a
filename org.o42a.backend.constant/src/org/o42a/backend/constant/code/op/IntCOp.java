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

import org.o42a.backend.constant.code.CCode;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.IntOp;


public abstract class IntCOp<U extends IntOp<U>, T extends Number>
		extends NumCOp<U, T>
		implements IntOp<U> {

	public IntCOp(OpBE<U> backend) {
		super(backend);
	}

	public IntCOp(CodeId id, CCode<?> code, T constant) {
		super(id, code, constant);
	}

	public IntCOp(OpBE<U> backend, T constant) {
		super(backend, constant);
	}

	@Override
	public final U shl(final CodeId id, final Code code, final U numBits) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final IntCOp<U, T> nb = (IntCOp<U, T>) numBits;

		if (isConstant() && nb.isConstant()) {

			final T shl = shl(getConstant(), nb.getConstant().intValue());

			return create(constant(id, ccode, shl), shl);
		}

		return create(
				new OpBE<U>(id, ccode) {
					@Override
					protected U write() {
						return backend().underlying().shl(
								getId(),
								code().getUnderlying(),
								nb.backend().underlying());
					}
				},
				null);
	}

	@Override
	public final U shl(final CodeId id, final Code code, final int numBits) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final T shl = shl(getConstant(), Integer.valueOf(numBits));

			return create(constant(id, ccode, shl), shl);
		}

		return create(
				new OpBE<U>(id, ccode) {
					@Override
					protected U write() {
						return backend().underlying().shl(
								getId(),
								code().getUnderlying(),
								numBits);
					}
				},
				null);
	}

	@Override
	public final U lshr(final CodeId id, final Code code, final U numBits) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final IntCOp<U, T> nb = (IntCOp<U, T>) numBits;

		if (isConstant() && nb.isConstant()) {

			final T lshr = lshr(getConstant(), nb.getConstant().intValue());

			return create(constant(id, ccode, lshr), lshr);
		}

		return create(
				new OpBE<U>(id, ccode) {
					@Override
					protected U write() {
						return backend().underlying().lshr(
								getId(),
								code().getUnderlying(),
								nb.backend().underlying());
					}
				},
				null);
	}

	@Override
	public final U lshr(final CodeId id, final Code code, final int numBits) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final T lshr = lshr(getConstant(), Integer.valueOf(numBits));

			return create(constant(id, ccode, lshr), lshr);
		}

		return create(
				new OpBE<U>(id, ccode) {
					@Override
					protected U write() {
						return backend().underlying().lshr(
								getId(),
								code().getUnderlying(),
								numBits);
					}
				},
				null);
	}

	@Override
	public final U ashr(final CodeId id, final Code code, final U numBits) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final IntCOp<U, T> nb = (IntCOp<U, T>) numBits;

		if (isConstant() && nb.isConstant()) {

			final T ashr = ashr(getConstant(), nb.getConstant().intValue());

			return create(constant(id, ccode, ashr), ashr);
		}

		return create(
				new OpBE<U>(id, ccode) {
					@Override
					protected U write() {
						return backend().underlying().ashr(
								getId(),
								code().getUnderlying(),
								nb.backend().underlying());
					}
				},
				null);
	}

	@Override
	public final U ashr(final CodeId id, final Code code, final int numBits) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final T ashr = ashr(getConstant(), Integer.valueOf(numBits));

			return create(constant(id, ccode, ashr), ashr);
		}

		return create(
				new OpBE<U>(id, ccode) {
					@Override
					protected U write() {
						return backend().underlying().ashr(
								getId(),
								code().getUnderlying(),
								numBits);
					}
				},
				null);
	}

	@Override
	public final U and(final CodeId id, final Code code, final U operand) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final IntCOp<U, T> op = (IntCOp<U, T>) operand;

		if (isConstant() && op.isConstant()) {

			final T and = and(getConstant(), op.getConstant());

			return create(constant(id, ccode, and), and);
		}

		return create(
				new OpBE<U>(id, ccode) {
					@Override
					protected U write() {
						return backend().underlying().and(
								getId(),
								code().getUnderlying(),
								op.backend().underlying());
					}
				},
				null);
	}

	@Override
	public final U or(final CodeId id, final Code code, final U operand) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final IntCOp<U, T> op = (IntCOp<U, T>) operand;

		if (isConstant() && op.isConstant()) {

			final T or = or(getConstant(), op.getConstant());

			return create(constant(id, ccode, or), or);
		}

		return create(
				new OpBE<U>(id, ccode) {
					@Override
					protected U write() {
						return backend().underlying().or(
								getId(),
								code().getUnderlying(),
								op.backend().underlying());
					}
				},
				null);
	}

	@Override
	public final U xor(final CodeId id, final Code code, final U operand) {

		final CCode<?> ccode = cast(code);
		@SuppressWarnings("unchecked")
		final IntCOp<U, T> op = (IntCOp<U, T>) operand;

		if (isConstant() && op.isConstant()) {

			final T xor = xor(getConstant(), op.getConstant());

			return create(constant(id, ccode, xor), xor);
		}

		return create(
				new OpBE<U>(id, ccode) {
					@Override
					protected U write() {
						return backend().underlying().xor(
								getId(),
								code().getUnderlying(),
								op.backend().underlying());
					}
				},
				null);
	}

	@Override
	public final U comp(final CodeId id, final Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final T comp = comp(getConstant());

			return create(constant(id, ccode, comp), comp);
		}

		return create(
				new OpBE<U>(id, ccode) {
					@Override
					protected U write() {
						return backend().underlying().comp(
								getId(),
								code().getUnderlying());
					}
				},
				null);
	}

	@Override
	public final BoolOp lowestBit(final CodeId id, final Code code) {

		final CCode<?> ccode = cast(code);

		if (isConstant()) {

			final boolean lowestBit = (getConstant().intValue() & 1) != 0;

			return new BoolCOp(id, ccode, lowestBit);
		}

		return new BoolCOp(new OpBE<BoolOp>(id, ccode) {
			@Override
			protected BoolOp write() {
				return backend().underlying().lowestBit(
						getId(),
						code().getUnderlying());
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
