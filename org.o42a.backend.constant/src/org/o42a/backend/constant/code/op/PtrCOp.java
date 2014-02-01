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
import org.o42a.backend.constant.code.ReturnBE;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.codegen.data.AbstractPtr;
import org.o42a.util.string.ID;


public abstract class PtrCOp<P extends PtrOp<P>, PT extends AbstractPtr>
		extends AbstractCOp<P, PT>
		implements PtrOp<P> {

	public PtrCOp(OpBE<P> backend) {
		super(backend);
	}

	public PtrCOp(OpBE<P> backend, PT constant) {
		super(backend, constant);
	}

	@Override
	public final BoolCOp isNull(ID id, Code code) {

		final CCode<?> ccode = cast(code);
		final ID resultId = code.getOpNames().unaryId(id, IS_NULL_ID, this);

		if (isConstant()) {
			return new BoolCOp(resultId, ccode, getConstant().isNull());
		}

		return new BoolCOp(new OpBE<BoolOp>(resultId, ccode) {
			@Override
			public void prepare() {
				use(backend());
			}
			@Override
			protected BoolOp write() {
				return backend().underlying().isNull(
						getId(),
						part().underlying());
			}
		});
	}

	@Override
	public final BoolCOp eq(ID id, Code code, P other) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, EQ_ID, this, other);
		final COp<P, ?> o = cast(other);

		if (isConstant() && o.isConstant()) {
			return new BoolCOp(
					resultId,
					ccode,
					getConstant().equals(o.getConstant()));
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
	public BoolOp ne(ID id, Code code, P other) {

		final CCode<?> ccode = cast(code);
		final ID resultId =
				code.getOpNames().binaryId(id, NE_ID, this, other);
		final COp<P, ?> o = cast(other);

		if (isConstant() && o.isConstant()) {
			return new BoolCOp(
					resultId,
					ccode,
					!getConstant().equals(o.getConstant()));
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
	public final void returnValue(Block code) {
		new ReturnBE(cast(code).nextPart()) {
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

}
