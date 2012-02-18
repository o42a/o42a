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

import static org.o42a.analysis.use.SimpleUsage.SIMPLE_USAGE;
import static org.o42a.analysis.use.SimpleUsage.simpleUsable;
import static org.o42a.backend.constant.data.ConstBackend.cast;

import org.o42a.analysis.use.*;
import org.o42a.backend.constant.code.CCode;
import org.o42a.backend.constant.code.CCodePart;
import org.o42a.backend.constant.code.ReturnBE;
import org.o42a.backend.constant.data.struct.CStruct;
import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.Op;
import org.o42a.codegen.code.op.StructOp;


public final class BoolCOp extends BoolOp implements COp<BoolOp, Boolean> {

	private final OpBE<BoolOp> backend;
	private final Boolean constant;
	private final Usable<SimpleUsage> allUses;

	public BoolCOp(OpBE<BoolOp> backend) {
		this(backend, null);
	}

	public BoolCOp(OpBE<BoolOp> backend, Boolean constant) {
		this.backend = backend;
		this.constant = constant;
		this.allUses = simpleUsable(this);
		this.backend.init(this);
	}

	public BoolCOp(CodeId id, CCode<?> code, boolean constant) {
		this(new BoolConstBE(id, code, constant), constant);
	}

	@Override
	public final CCodePart<?> part() {
		return backend().part();
	}

	@Override
	public final OpBE<BoolOp> backend() {
		return this.backend;
	}

	@Override
	public CodeId getId() {
		return backend().getId();
	}

	@Override
	public final boolean isConstant() {
		return getConstant() != null;
	}

	@Override
	public final Boolean getConstant() {
		return this.constant;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends Op> O select(
			final CodeId id,
			final Code code,
			final O trueValue,
			final O falseValue) {

		final CCode<?> ccode = cast(code);
		final CodeId selectId = id != null ? id : getId().sub("select");

		if (isConstant()) {
			if (getConstant()) {
				return create(selectId, ccode, trueValue);
			}
			return create(selectId, ccode, falseValue);
		}

		if (trueValue instanceof StructOp) {

			@SuppressWarnings("rawtypes")
			final CStruct trueStruct = cast((StructOp) trueValue);
			@SuppressWarnings("rawtypes")
			final CStruct falseStruct = cast((StructOp) falseValue);

			return selectStruct(selectId, ccode, trueStruct, falseStruct);
		}

		final COp<O, ?> trueVal = cast(trueValue);

		return trueVal.create(new OpBE<O>(selectId, ccode) {
			@Override
			public void prepare() {

			}
			@Override
			protected O write() {
				return backend().underlying().select(
						getId(),
						part().underlying(),
						trueVal.backend().underlying(),
						cast(falseValue).backend().underlying());
			}
		});
	}

	@Override
	public void returnValue(Block code) {
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

	@Override
	public final BoolOp create(OpBE<BoolOp> backend) {
		return create(backend, null);
	}

	@Override
	public BoolOp create(OpBE<BoolOp> backend, Boolean constant) {
		return new BoolCOp(backend, constant);
	}

	@Override
	public final void useBy(UserInfo user) {
		this.allUses.useBy(user, SIMPLE_USAGE);
	}

	@Override
	public User<SimpleUsage> toUser() {
		return this.allUses.toUser();
	}

	@Override
	public String toString() {
		if (this.backend == null) {
			return super.toString();
		}
		return this.backend.toString();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <O extends Op> O create(CodeId id, CCode<?> code, O value) {

		final COp val = cast(value);

		if (val.part() == code.nextPart()) {
			return value;
		}

		return (O) val.create(
				new AliasBE(id, code, val.backend()),
				val.getConstant());
	}

	private <S extends StructOp<S>> S selectStruct(
			final CodeId id,
			final CCode<?> code,
			final CStruct<S> trueValue,
			final CStruct<S> falseValue) {
		return trueValue.create(new OpBE<S>(id, code) {
			@Override
			public void prepare() {
				use(backend());
				use(trueValue);
				use(falseValue);
			}
			@Override
			protected S write() {
				return backend().underlying().select(
						getId(),
						part().underlying(),
						trueValue.backend().underlying(),
						falseValue.backend().underlying());
			}
		});
	}

	private static final class BoolConstBE extends ConstBE<BoolOp, Boolean> {

		BoolConstBE(CodeId id, CCode<?> code, boolean constant) {
			super(id, code, constant);
		}

		@Override
		protected BoolOp write() {
			return part().underlying().bool(constant());
		}

	}

}
