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
import org.o42a.core.ir.op.*;
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
	public final ObjectOp toObject(CodeDirs dirs) {
		return this;
	}

	@Override
	public final LocalOp toLocal() {
		return null;
	}

	public abstract ObjOp cast(CodeDirs dirs, Obj ascendant);

	public final void writeLogicalValue(CodeDirs dirs) {

		final Code code = dirs.code();
		final ObjectTypeOp objectType = objectType(code);
		final ValOp value = objectType.ptr().data(code).value(code);
		final CondBlk indefinite = value.loadIndefinite(code).branch(
				code,
				"cond_indefinite",
				"cond_definite");
		final CodeBlk definite = indefinite.otherwise();

		definite.dump("Definite value: ", value);
		value.go(definite, dirs);
		definite.go(code.tail());

		writeValue(indefinite, value, null);
		indefinite.dump("Calculated value: ", value);
		value.go(indefinite, dirs);
		indefinite.go(code.tail());
	}

	public final void writeLogicalValue(CodeDirs dirs, ObjectOp body) {

		final Code code = dirs.code();
		final ValOp result = code.allocate(null, VAL_TYPE).storeIndefinite(code);

		writeValue(dirs, result, body);
	}

	public final ValOp writeValue(Code code) {

		final ValOp value = objectType(code).ptr().data(code).value(code);

		writeValue(code, value, null);

		return value;
	}

	public final ValOp writeValue(CodeDirs dirs) {
		return writeValue(dirs, null);
	}

	public final ValOp writeValue(CodeDirs dirs, ValOp result) {

		final Code code = dirs.code();
		final ValOp value = objectType(code).ptr().data(code).value(code);
		final CondBlk indefinite = value.loadIndefinite(code).branch(
				code,
				"val_indefinite",
				"val_definite");
		final CodeBlk definite = indefinite.otherwise();

		definite.dump(this + " value is definite: ", value);
		if (result != null) {
			result.store(definite, value);
		}
		value.go(definite, dirs);
		definite.go(code.tail());

		writeValue(indefinite, value, null);
		indefinite.dump(this + " value calculated: ", value);
		if (result != null) {
			result.store(indefinite, value);
		}
		value.go(indefinite, dirs);
		indefinite.go(code.tail());

		return value;
	}

	public final void writeValue(CodeDirs dirs, ValOp result, ObjectOp body) {
		dirs = dirs.begin(
				"obj_value",
				"Write value of " + body + " by " + this);

		final Code code = dirs.code();

		writeValue(code, result, body);
		code.dump("Value: ", result);
		dirs.end();
		result.go(code, dirs);
	}

	public final void writeRequirement(CodeDirs dirs) {
		writeRequirement(dirs, null);
	}

	public void writeRequirement(CodeDirs dirs, ObjectOp body) {
		if (body != null) {
			dirs = dirs.begin("obj_req", "Requirement of " + body);
		} else {
			dirs = dirs.begin("obj_req", "Requirement");
		}
		objectType(dirs.code()).writeRequirement(dirs, body);
		dirs.end();
	}

	public final void writeClaim(CodeDirs dirs, ValOp result) {
		writeClaim(dirs, result, null);
	}

	public final void writeClaim(CodeDirs dirs, ValOp result, ObjOp body) {
		writeClaim(dirs.code(), result, body);
		result.go(dirs.code(), dirs);
	}

	public final void writeCondition(CodeDirs dirs) {
		writeCondition(dirs, null);
	}

	public void writeCondition(CodeDirs dirs, ObjOp body) {
		if (body != null) {
			dirs = dirs.begin("obj_cond", "Condition of " + body);
		} else {
			dirs = dirs.begin("obj_cond", "Condition");
		}
		objectType(dirs.code()).writeCondition(dirs, body);
		dirs.end();
	}

	public final void writeProposition(Code code, ValOp result) {
		writeProposition(code, result, null);
	}

	public final void writeProposition(CodeDirs dirs, ValOp result) {
		writeProposition(dirs, result, null);
	}

	public final void writeProposition(
			CodeDirs dirs,
			ValOp result,
			ObjOp body) {
		writeProposition(dirs.code(), result, body);
		result.go(dirs.code(), dirs);
	}

	public final ObjectTypeOp objectType(Code code) {
		if (this.objectType != null) {
			return this.objectType;
		}
		return body(code).loadObjectType(code).op(getBuilder(), getPrecision());
	}

	public final BoolOp hasAncestor(Code code) {
		return body(code).ancestorBody(code).load(null, code)
		.toInt32(null, code).ne(null, code, code.int32(0));
	}

	public final ObjectOp ancestor(Code code) {
		return body(code).loadAncestor(getBuilder(), code);
	}

	public final ObjectMethodsIR.Op methods(Code code) {
		return body(code).loadMethods(code);
	}

	@Override
	public abstract FldOp field(CodeDirs dirs, MemberKey memberKey);

	public abstract DepOp dep(CodeDirs dirs, Dep dep);

	@Override
	public final ObjectOp materialize(CodeDirs dirs) {
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

	protected ObjOp dynamicCast(CodeDirs dirs, Obj ascendant) {

		final ObjectIR ascendantIR = ascendant.ir(getGenerator());

		dirs = dirs.begin(
				"cast",
				"Dynamic cast " + this + " to " + ascendantIR.getId());

		final Code code = dirs.code();
		final ObjOp ascendantObj = ascendantIR.op(getBuilder(), code);
		final ObjectTypeOp ascendantType = ascendantObj.objectType(code);

		final DataOp resultPtr =
			castFunc().op(null, code).cast(code, this, ascendantType);
		final CodeBlk castNull = code.addBlock("cast_null");

		resultPtr.isNull(null, code).go(code, castNull.head());

		final ObjOp result = resultPtr.to(null, code, ascendantIR.getBodyType()).op(
				getBuilder(),
				ascendant,
				COMPATIBLE);

		if (castNull.exists()) {
			castNull.debug("Cast failed");
			dirs.goWhenFalse(castNull);
		}

		dirs.end();

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
		return ptr().toAny(null, code).to(
				null,
				code, getWellKnownType().ir(getGenerator()).getBodyType());
	}

}
