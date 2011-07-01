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
import static org.o42a.core.ir.value.ValStoreMode.ASSIGNMENT_VAL_STORE;
import static org.o42a.core.ir.value.ValStoreMode.INITIAL_VAL_STORE;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CondCode;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.PtrOp;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.Dep;
import org.o42a.core.value.ValueType;


public abstract class ObjectOp extends IROp implements HostOp, ObjValOp {

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

	ObjectOp(CodeBuilder builder, PtrOp<?> ptr, ObjectPrecision precision) {
		super(builder, ptr);
		this.precision = precision;
		this.objectType = null;
	}

	ObjectOp(PtrOp<?> ptr, ObjectTypeOp objectType) {
		super(objectType.getBuilder(), ptr);
		this.objectType = objectType;
		this.precision = objectType.getPrecision();
	}

	public final ValueType<?> getValueType() {
		return getWellKnownType().getValueType();
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

	public abstract ObjOp cast(CodeId id, CodeDirs dirs, Obj ascendant);

	@Override
	public final void writeLogicalValue(CodeDirs dirs) {

		final ValDirs valDirs = dirs.value(getValueType());

		writeValue(valDirs);
		valDirs.done();
	}

	public final void writeLogicalValue(CodeDirs dirs, ObjectOp body) {
		assert body == null || body.getValueType().assertIs(getValueType());

		final ValDirs valDirs = dirs.value(getValueType());

		writeValue(valDirs, body);
		valDirs.done();
	}

	@Override
	public final ValOp writeValue(ValDirs dirs) {
		assert dirs.getValueType().assertIs(getValueType());

		final Code code = dirs.code();
		final ValOp value = objectType(code).ptr().data(code).value(code).op(
				getBuilder(),
				getValueType());
		final CondCode indefinite = value.loadIndefinite(null, code).branch(
				code,
				"val_indefinite",
				"val_definite");
		final Code definite = indefinite.otherwise();

		definite.dump(this + " value is definite: ", value.ptr());
		value.go(definite, dirs);
		definite.go(code.tail());

		evaluateAndStoreValue(indefinite, value, dirs);

		indefinite.dump(this + " value calculated: ", value.ptr());
		indefinite.go(code.tail());

		return value;
	}

	public ValOp writeValue(ValDirs dirs, ObjectOp body) {

		final ValDirs dubDirs = dirs.begin(
				"Value of "
				+ (body != null ? body + " by " + this : toString()));
		final ValOp result =
				objectType(dubDirs.code()).writeValue(dubDirs, body);

		dubDirs.done();

		return result;
	}

	public final void writeRequirement(CodeDirs dirs) {
		writeRequirement(dirs, null);
	}

	public void writeRequirement(CodeDirs dirs, ObjectOp body) {

		final CodeDirs subDirs;

		if (body != null) {
			subDirs = dirs.begin("obj_req", "Requirement of " + body);
		} else {
			subDirs = dirs.begin("obj_req", "Requirement");
		}

		objectType(subDirs.code()).writeRequirement(subDirs, body);

		subDirs.end();
	}

	public final ValOp writeClaim(ValDirs dirs) {
		return writeClaim(dirs, null);
	}

	public final void writeCondition(CodeDirs dirs) {
		writeCondition(dirs, null);
	}

	public void writeCondition(CodeDirs dirs, ObjOp body) {

		final CodeDirs subDirs;

		if (body != null) {
			subDirs = dirs.begin("obj_cond", "Condition of " + body);
		} else {
			subDirs = dirs.begin("obj_cond", "Condition");
		}

		objectType(subDirs.code()).writeCondition(subDirs, body);

		subDirs.end();
	}

	public final ValOp writeProposition(ValDirs dirs) {
		return writeProposition(dirs, null);
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

	protected ObjOp dynamicCast(
			CodeId id,
			CodeDirs dirs,
			Obj ascendant) {

		final ObjectIR ascendantIR = ascendant.ir(getGenerator());
		final CodeDirs subDirs = dirs.begin(
				id != null ? id.getId() : "cast",
				"Dynamic cast " + this + " to " + ascendantIR.getId());

		final Code code = subDirs.code();
		final ObjOp ascendantObj = ascendantIR.op(getBuilder(), code);
		final ObjectTypeOp ascendantType = ascendantObj.objectType(code);

		final DataOp resultPtr =
			castFunc()
			.op(null, code)
			.cast(
					id != null ? id.detail("ptr") : null,
					code,
					this,
					ascendantType);
		final Code castNull = code.addBlock("cast_null");

		resultPtr.isNull(null, code).go(code, castNull.head());

		final ObjOp result =
			resultPtr.to(id, code, ascendantIR.getBodyType()).op(
							getBuilder(),
							ascendant,
							COMPATIBLE);

		if (castNull.exists()) {
			castNull.debug("Cast failed");
			castNull.go(subDirs.falseDir());
		}

		subDirs.end();

		return result;
	}

	protected ValOp writeClaim(ValDirs dirs, ObjectOp body) {

		final ValDirs subDirs = dirs.begin(
				"Claim of "
				+ (body != null ? body + " by " + this : toString()));
		final ValOp result =
				objectType(subDirs.code()).writeClaim(subDirs, body);

		subDirs.done();

		return result;
	}

	protected ValOp writeProposition(ValDirs dirs, ObjectOp body) {

		final ValDirs subDirs = dirs.begin(
				"Proposition of "
				+ (body != null ? body + " by " + this : toString()));
		final ValOp result =
			objectType(subDirs.code()).writeProposition(subDirs, body);

		subDirs.done();

		return result;
	}

	protected final ObjectTypeOp cachedData() {
		return this.objectType;
	}

	private void evaluateAndStoreValue(
			Code code,
			ValOp value,
			ValDirs resultDirs) {

		final Code falseCode = code.addBlock("eval_false");
		final Code unknownCode = code.addBlock("eval_unknown");
		final ValDirs valDirs =
			getBuilder().splitWhenUnknown(
					code,
					falseCode.head(),
					unknownCode.head())
			.value(code.id("obj_val"), value);

		value.setStoreMode(INITIAL_VAL_STORE);
		writeValue(valDirs, null);

		valDirs.done();
		value.setStoreMode(ASSIGNMENT_VAL_STORE);

		if (falseCode.exists()) {
			value.storeFalse(falseCode);
			falseCode.go(resultDirs.falseDir());
		}
		if (unknownCode.exists()) {
			value.storeUnknown(unknownCode);
			unknownCode.go(resultDirs.unknownDir());
		}
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
