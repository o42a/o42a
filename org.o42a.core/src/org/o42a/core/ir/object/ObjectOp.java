/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;
import static org.o42a.core.ir.op.CastObjectFunc.CAST_OBJECT;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.struct.ValueOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.Dep;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.LinkValueStruct;


public abstract class ObjectOp extends IROp implements HostOp {

	public static ObjectOp anonymousObject(
			CodeBuilder builder,
			DataOp ptr,
			Obj wellKnownType) {
		return new AnonymousObjOp(builder, ptr, wellKnownType);
	}

	private final ObjectPrecision precision;
	private final ObjectTypeOp objectType;

	ObjectOp(CodeBuilder builder, ObjectPrecision precision) {
		super(builder);
		this.precision = precision;
		this.objectType = null;
	}

	ObjectOp(ObjectTypeOp objectType) {
		super(objectType.getBuilder());
		this.objectType = objectType;
		this.precision = objectType.getPrecision();
	}

	public abstract Obj getWellKnownType();

	public final ObjectPrecision getPrecision() {
		return this.precision;
	}

	@Override
	public final LocalOp toLocal() {
		return null;
	}

	public void fillDeps(CodeDirs dirs, Obj sample) {
		for (Dep dep : sample.getDeps()) {
			if (dep.isDisabled()) {
				continue;
			}
			dep(dirs, dep).fill(
					getBuilder().host().toLocal().getBuilder(),
					dirs);
		}
	}

	public abstract ObjOp cast(CodeId id, CodeDirs dirs, Obj ascendant);

	public ObjectOp dynamicCast(
			CodeId id,
			CodeDirs dirs,
			ObjectTypeOp type,
			Obj wellKnownType,
			boolean reportError) {

		final CodeDirs subDirs = dirs.begin(
				id != null ? id.getId() : "cast",
				"Dynamic cast " + this + " to " + wellKnownType);

		final Block code = subDirs.code();

		code.dumpName("To", type.ptr());

		final DataOp castResult =
				castFunc(reportError)
				.op(null, code)
				.cast(
						id != null ? id.detail("ptr") : null,
						code,
						this,
						type);

		final DataOp resultPtr;

		if (!reportError) {
			resultPtr = castResult;

			final Block castNull = code.addBlock("cast_null");

			castResult.isNull(null, code).go(code, castNull.head());

			if (castNull.exists()) {
				castNull.debug("Cast failed");
				castNull.go(subDirs.falseDir());
			}
		} else {

			final DataOp falsePtr = getContext()
					.getFalse()
					.ir(getGenerator())
					.op(getBuilder(), code)
					.toData(code);

			resultPtr =
					castResult.isNull(null, code)
					.select(null, code, falsePtr, castResult);
		}

		final ObjectOp result =
				anonymousObject(getBuilder(), resultPtr, wellKnownType);

		subDirs.end();

		return result;
	}

	public abstract ValueOp value();

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
	public ObjectOp dereference(CodeDirs dirs) {

		final LinkValueStruct linkStruct =
				getWellKnownType().value().getValueStruct().toLinkStruct();

		assert linkStruct != null :
			"Not a link: " + this;

		final ValDirs valDirs = dirs.value(linkStruct, "link");
		final ValOp value = value().writeValue(valDirs);
		final Block code = valDirs.code();

		final DataOp ptr =
				value.value(null, code)
				.toPtr(null, code)
				.load(null, code)
				.toData(code.id("target"), code);

		valDirs.done();

		return anonymousObject(
				getBuilder(),
				ptr,
				linkStruct.getTypeRef().typeObject(dummyUser()));
	}

	@Override
	public void assign(CodeDirs dirs, HostOp value) {
		value().assign(dirs, value.materialize(dirs));
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

	protected ObjOp dynamicCast(CodeId id, CodeDirs dirs, Obj ascendant) {

		final ObjectIR ascendantIR = ascendant.ir(getGenerator());
		final CodeDirs subDirs = dirs.begin(
				id != null ? id.getId() : "cast",
				"Dynamic cast " + this + " to " + ascendantIR.getId());

		final Block code = subDirs.code();
		final ObjOp ascendantObj = ascendantIR.op(getBuilder(), code);
		final ObjectTypeOp ascendantType = ascendantObj.objectType(code);

		final DataOp resultPtr =
				castFunc(false)
				.op(null, code)
				.cast(
						id != null ? id.detail("ptr") : null,
						code,
						this,
						ascendantType);

		resultPtr.isNull(null, code).go(code, subDirs.falseDir());

		final ObjOp result = resultPtr.to(
				id,
				code,
				ascendantIR.getBodyType()).op(
						getBuilder(),
						ascendant,
						COMPATIBLE);

		subDirs.end();

		return result;
	}

	protected final ObjectTypeOp cachedData() {
		return this.objectType;
	}

	private FuncPtr<CastObjectFunc> castFunc(boolean reportError) {
		return getGenerator()
				.externalFunction()
				.sideEffects(false)
				.link(
						reportError
						? "o42a_obj_cast_or_error" : "o42a_obj_cast",
						CAST_OBJECT);
	}

	private final ObjectBodyIR.Op body(Code code) {
		return ptr().toAny(null, code).to(
				null,
				code,
				getWellKnownType().ir(getGenerator()).getBodyType());
	}

}
