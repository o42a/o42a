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
import static org.o42a.core.ir.op.CastObjectFunc.CAST_OBJECT;
import static org.o42a.core.ir.op.ValOp.VAL_TYPE;

import org.o42a.codegen.code.*;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.op.CastObjectFunc;
import org.o42a.core.ir.op.IROp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.Dep;


public abstract class ObjectOp extends IROp implements HostOp {

	public static ObjectOp anonymousObject(
			CodeBuilder builder,
			DataOp ptr,
			Obj wellKnownType) {
		return new AnonymousObjOp(
				builder,
				ptr,
				wellKnownType);
	}

	private final ObjectPrecision precision;
	private final ObjectTypeOp objectType;

	ObjectOp(CodeBuilder builder, PtrOp ptr, ObjectPrecision precision) {
		super(builder, ptr);
		this.precision = precision;
		this.objectType = null;
	}

	ObjectOp(PtrOp ptr, ObjectTypeOp objectType) {
		super(objectType.getBuilder(), ptr);
		this.objectType = objectType;
		this.precision = objectType.getPrecision();
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

	public final void writeLogicalValue(Code code, CodePos exit) {

		final ObjectTypeOp objectType = objectType(code);
		final ValOp value = objectType.ptr().data(code).value(code);
		final CondBlk indefinite = value.loadIndefinite(code).branch(
				code,
				"cond_indefinite",
				"cond_definite");
		final CodeBlk definite = indefinite.otherwise();

		definite.dump("Definite value: ", value);
		value.loadCondition(definite).go(definite, code.tail(), exit);

		writeValue(indefinite, value, null);
		indefinite.dump("Calculated value: ", value);
		value.loadCondition(indefinite).go(indefinite, code.tail(), exit);
	}

	public final void writeLogicalValue(
			Code code,
			CodePos exit,
			ObjectOp body) {

		final ValOp result = code.allocate(VAL_TYPE).storeUnknown(code);

		writeValue(code, exit, result, body);
	}

	public final ValOp writeValue(Code code) {
		return writeValue(code, (CodePos) null, (ValOp) null);
	}

	public final ValOp writeValue(Code code, CodePos exit) {
		return writeValue(code, exit, null);
	}

	public final ValOp writeValue(Code code, ValOp result) {
		return writeValue(code, null, result);
	}

	public final ValOp writeValue(Code code, CodePos exit, ValOp result) {

		final ValOp value = objectType(code).ptr().data(code).value(code);
		final CondBlk indefinite = value.loadIndefinite(code).branch(
				code,
				"val_indefinite",
				"val_definite");
		final CodeBlk definite = indefinite.otherwise();

		definite.dump(this + " value is definite:\n", value);
		if (result != null) {
			result.store(definite, value);
		}
		checkValue(definite, code.tail(), exit, value);

		writeValue(indefinite, value, null);
		indefinite.dump(this + " value calculated:\n", value);
		if (result != null) {
			result.store(indefinite, value);
		}
		checkValue(indefinite, code.tail(), exit, value);

		return value;
	}

	public final void writeValue(
			Code code,
			CodePos exit,
			ValOp result,
			ObjectOp body) {
		writeValue(code, result, body);
		code.dump("Write value of " + body + " by " + this + ":\n", result);
		if (exit != null) {
			result.loadCondition(code).goUnless(code, exit);
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
		objectType(code).writeRequirement(code, exit, body);
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
			result.loadCondition(code).goUnless(code, exit);
		}
	}

	public final void writeCondition(Code code, CodePos exit) {
		writeCondition(code, exit, null);
	}

	public void writeCondition(Code code, CodePos exit, ObjOp body) {
		if (body != null) {
			code.debug("Condition of " + body.getAscendant() + " by " + this);
		} else {
			code.debug("Condition of " + this);
		}
		objectType(code).writeCondition(code, exit, body);
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
			result.loadCondition(code).goUnless(code, exit);
		}
	}

	public final ObjectTypeOp objectType(Code code) {
		if (this.objectType != null) {
			return this.objectType;
		}
		return body(code).loadObjectType(code).op(getBuilder(), getPrecision());
	}

	public final BoolOp hasAncestor(Code code) {
		return body(code).ancestorBody(code).load(code)
		.toInt32(code).ne(code, code.int32(0));
	}

	public final ObjectOp ancestor(Code code) {
		return body(code).loadAncestor(getBuilder(), code);
	}

	public final ObjectMethodsIR.Op methods(Code code) {
		return body(code).loadMethods(code);
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

		final ObjectIR ascendantIR = ascendant.ir(getGenerator());

		code.begin("Dynamic cast " + this + " to " + ascendantIR.getId());

		final ObjOp ascendantObj = ascendantIR.op(getBuilder(), code);
		final ObjectTypeOp ascendantType = ascendantObj.objectType(code);

		final DataOp resultPtr =
			castFunc().op(code).cast(code, this, ascendantType);
		final ObjOp result = resultPtr.to(code, ascendantIR.getBodyType()).op(
				getBuilder(),
				ascendant,
				COMPATIBLE);

		code.end();

		return result;
	}

	protected void writeValue(Code code, ValOp result, ObjectOp body) {
		if (body != null) {
			code.begin("Value of " + body + " by " + this);
		} else {
			code.begin("Value of " + this);
		}
		objectType(code).writeValue(code, result, body);
		code.end();
	}

	protected void writeClaim(Code code, ValOp result, ObjectOp body) {
		if (body != null) {
			code.begin("Claim of " + body + " by " + this);
		} else {
			code.begin("Claim of " + this);
		}
		objectType(code).writeClaim(code, result, body);
		code.end();
	}

	protected void writeProposition(Code code, ValOp result, ObjectOp body) {
		if (body != null) {
			code.begin("Proposition of " + body + " by " + this);
		} else {
			code.begin("Proposition of " + this);
		}
		objectType(code).writeProposition(code, result, body);
		code.end();
	}

	protected final ObjectTypeOp cachedData() {
		return this.objectType;
	}

	private FuncPtr<CastObjectFunc> castFunc() {
		return getGenerator().externalFunction("o42a_obj_cast", CAST_OBJECT);
	}

	private final ObjectBodyIR.Op body(Code code) {
		return ptr().toAny(code).to(
				code,
				getWellKnownType().ir(getGenerator()).getBodyType());
	}

	private static void checkValue(
			CodeBlk code,
			CodePos ok,
			CodePos exit,
			ValOp value) {
		if (exit != null) {
			value.loadCondition(code).go(code, ok, exit);
		} else {
			code.go(ok);
		}
	}

}
