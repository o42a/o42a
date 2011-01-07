/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.op.IROp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.Dep;


public abstract class ObjectOp extends IROp implements HostOp {

	public static ObjectOp anonymousObject(
			CodeBuilder builder,
			AnyOp ptr,
			Obj wellKnownType) {
		return new AnonymousObjOp(
				builder,
				ptr,
				wellKnownType);
	}

	private final ObjectPrecision precision;
	private final ObjectDataOp data;

	ObjectOp(CodeBuilder builder, PtrOp ptr, ObjectPrecision precision) {
		super(builder, ptr);
		this.precision = precision;
		this.data = null;
	}

	ObjectOp(PtrOp ptr, ObjectDataOp data) {
		super(data.getBuilder(), ptr);
		this.data = data;
		this.precision = data.getPrecision();
	}

	public abstract Obj getWellKnownType();

	public final ObjectPrecision getPrecision() {
		return this.precision;
	}

	@Override
	public final ObjectOp toObject(Code code, CodePos exit) {
		return this;
	}

	@Override
	public final LocalOp toLocal() {
		return null;
	}

	public abstract ObjOp cast(Code code, CodePos exit, Obj ascendant);

	public final void writeCondition(Code code, CodePos exit) {

		final ObjectDataOp data = data(code);
		final ValOp value = data.ptr().value(code);
		final CondBlk indefinite = value.indefinite(code).branch(
				code,
				"cond_indefinite",
				"cond_definite");
		final CodeBlk definite = indefinite.otherwise();

		definite.dumpValue("Definite value", value);
		value.condition(definite).go(definite, code.tail(), exit);

		writeValue(indefinite, value, null);
		indefinite.dumpValue("Calculated value", value);
		value.condition(indefinite).go(indefinite, code.tail(), exit);
	}

	public final void writeCondition(Code code, CodePos exit, ObjectOp body) {

		final ValOp result =
			code.allocate(getGenerator().valType()).storeUnknown(code);

		writeValue(code, exit, result, body);
	}

	public final void writeValue(Code code, ValOp result) {
		writeValue(code, null, result);
	}

	public final void writeValue(Code code, CodePos exit, ValOp result) {

		final ObjectDataOp data = data(code);
		final ValOp value = data.ptr().value(code);
		final CondBlk indefinite = value.indefinite(code).branch(
				code,
				"val_indefinite",
				"val_definite");
		final CodeBlk definite = indefinite.otherwise();

		definite.dumpValue(this + " value is definite", value);
		result.store(definite, value);
		checkValue(definite, code.tail(), exit, value);

		writeValue(indefinite, value, null);
		indefinite.dumpValue(this + " value calculated", value);
		result.store(indefinite, value);
		checkValue(indefinite, code.tail(), exit, value);
	}

	public final void writeValue(
			Code code,
			CodePos exit,
			ValOp result,
			ObjectOp body) {
		writeValue(code, result, body);
		code.dumpValue("Write value of " + body + " by " + this, result);
		if (exit != null) {
			result.condition(code).goUnless(code, exit);
		}
	}

	public final void writeRequirement(Code code, CodePos exit) {
		writeRequirement(code, exit, null);
	}

	public void writeRequirement(Code code, CodePos exit, ObjectOp body) {
		if (body != null) {
			code.debug("Requirement of " + body);
		} else {
			code.debug("Requirement");
		}
		data(code).writeRequirement(code, exit, body);
	}

	public final void writeClaim(Code code, ValOp result) {
		writeClaim(code, null, result, null);
	}

	public final void writeClaim(Code code, CodePos exit, ValOp result) {
		writeClaim(code, exit, result, null);
	}

	public final void writeClaim(
			Code code,
			CodePos exit,
			ValOp result,
			ObjOp body) {
		writeClaim(code, result, body);
		if (exit != null) {
			result.condition(code).goUnless(code, exit);
		}
	}

	public final void writePostCondition(Code code, CodePos exit) {
		writePostCondition(code, exit, null);
	}

	public void writePostCondition(Code code, CodePos exit, ObjOp body) {
		if (body != null) {
			code.debug(
					"Post-condition of " + body.getAscendant() + " by " + this);
		} else {
			code.debug("Post-condition of " + this);
		}
		data(code).writePostCondition(code, exit, body);
	}

	public final void writeProposition(Code code, ValOp result) {
		writeProposition(code, null, result, null);
	}

	public final void writeProposition(Code code, CodePos exit, ValOp result) {
		writeProposition(code, exit, result, null);
	}

	public final void writeProposition(
			Code code,
			CodePos exit,
			ValOp result,
			ObjOp body) {
		writeProposition(code, result, body);
		if (exit != null) {
			result.condition(code).goUnless(code, exit);
		}
	}

	public final ObjectDataOp data(Code code) {
		if (this.data != null) {
			return this.data;
		}
		return body(code).data(code).op(getBuilder(), getPrecision());
	}

	public final BoolOp hasAncestor(Code code) {
		return body(code).ancestorBody(code).load(code)
		.toInt32(code).ne(code, code.int32(0));
	}

	public final ObjectOp ancestor(Code code) {
		return body(code).ancestor(getBuilder(), code);
	}

	public final ObjectMethodsIR.Op methods(Code code) {
		return body(code).methods(code);
	}

	@Override
	public abstract FldOp field(Code code, CodePos exit, MemberKey memberKey);

	public abstract DepOp dep(Code code, CodePos exit, Dep dep);

	@Override
	public final ObjectOp materialize(Code code, CodePos exit) {
		return this;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append('(');

		switch (getPrecision()) {
		case EXACT:
			break;
		case COMPATIBLE:
			out.append("(*) ");
			break;
		case DERIVED:
			out.append("(?) ");
			break;
		}

		out.append(getWellKnownType());
		out.append(") ");
		out.append(ptr());

		return out.toString();
	}

	protected ObjOp dynamicCast(Code code, Obj ascendant) {
		code.debug("Dynamic cast " + this + " to " + ascendant);

		final ObjectIR ascendantIR = ascendant.ir(getGenerator());
		final ObjOp ascendantObj = ascendantIR.op(getBuilder(), code);
		final ObjectDataType.Op ascendantType = ascendantObj.data(code).ptr();

		final AnyOp result = getGenerator().castFunc().op(code).call(
				code,
				ptr().toAny(code),
				ascendantType.toAny(code));

		return result.to(code, ascendantIR.getBodyType()).op(
				getBuilder(),
				ascendant,
				COMPATIBLE);
	}

	protected void writeValue(Code code, ValOp result, ObjectOp body) {
		if (body != null) {
			code.debug("Value of " + body + " by " + this);
		} else {
			code.debug("Value of " + this);
		}
		data(code).writeValue(code, result, body);
	}

	protected void writeClaim(Code code, ValOp result, ObjectOp body) {
		if (body != null) {
			code.debug("Claim of " + body + " by " + this);
		} else {
			code.debug("Claim of " + this);
		}
		data(code).writeClaim(code, result, body);
	}

	protected void writeProposition(Code code, ValOp result, ObjectOp body) {
		if (body != null) {
			code.debug("Proposition of " + body + " by " + this);
		} else {
			code.debug("Proposition of " + this);
		}
		data(code).writeProposition(code, result, body);
	}

	protected final ObjectDataOp cachedData() {
		return this.data;
	}

	private final ObjectBodyIR.Op body(Code code) {
		return ptr().to(
				code,
				getWellKnownType().ir(getGenerator()).getBodyType());
	}

	private static void checkValue(
			CodeBlk code,
			CodePos ok,
			CodePos exit,
			ValOp value) {
		if (exit != null) {
			value.condition(code).go(code, ok, exit);
		} else {
			code.go(ok);
		}
	}

}
