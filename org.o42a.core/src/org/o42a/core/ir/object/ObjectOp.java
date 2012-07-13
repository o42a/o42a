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

import static org.o42a.core.ir.object.ObjectPrecision.COMPATIBLE;
import static org.o42a.core.ir.object.op.CastObjectFunc.CAST_OBJECT;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.impl.AnonymousObjOp;
import org.o42a.core.ir.object.op.CastObjectFunc;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.IROp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.struct.ValueOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.Dep;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.util.string.ID;


public abstract class ObjectOp extends IROp implements HostOp {

	protected static final ID CAST_ID = ID.id("cast");
	protected static final ID TARGET_ID = ID.id("target");
	protected static final ID FIELD_HOST_ID = ID.id("field_host");
	protected static final ID DEP_HOST_ID = ID.id("dep_host");

	public static ObjectOp anonymousObject(
			CodeBuilder builder,
			DataOp ptr,
			Obj wellKnownType) {
		return new AnonymousObjOp(builder, ptr, wellKnownType);
	}

	private final ObjectPrecision precision;
	private final ObjectTypeOp objectType;

	protected ObjectOp(CodeBuilder builder, ObjectPrecision precision) {
		super(builder);
		this.precision = precision;
		this.objectType = null;
	}

	protected ObjectOp(ObjectTypeOp objectType) {
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
		for (Dep dep : sample.deps()) {
			if (dep.isDisabled()) {
				continue;
			}
			dep(dirs, dep).fill(
					getBuilder().host().toLocal().getBuilder(),
					dirs);
		}
	}

	public abstract ObjOp cast(ID id, CodeDirs dirs, Obj ascendant);

	public ObjectOp dynamicCast(
			ID id,
			CodeDirs dirs,
			ObjectTypeOp type,
			Obj wellKnownType,
			boolean reportError) {

		final CodeDirs subDirs = dirs.begin(
				id != null ? id : CAST_ID,
				"Dynamic cast " + this + " to " + wellKnownType);

		final Block code = subDirs.code();

		code.dumpName("To", type);

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

			final DataOp nonePtr =
					getContext()
					.getNone()
					.ir(getGenerator())
					.op(getBuilder(), code)
					.toData(null, code);

			resultPtr =
					castResult.isNull(null, code)
					.select(null, code, nonePtr, castResult);
		}

		final ObjectOp result =
				anonymousObject(getBuilder(), resultPtr, wellKnownType);

		subDirs.done();

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

	public final ObjectIRMethodsOp methods(Code code) {
		return body(code).loadMethods(code);
	}

	@Override
	public abstract FldOp field(CodeDirs dirs, MemberKey memberKey);

	public abstract DepOp dep(CodeDirs dirs, Dep dep);

	@Override
	public final ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return holder.hold(dirs.code(), this);
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {

		final LinkValueStruct linkStruct =
				getWellKnownType().value().getValueStruct().toLinkStruct();

		assert linkStruct != null :
			"Not a link: " + this;

		final ValDirs valDirs =
				dirs.nested().value("link", linkStruct, TEMP_VAL_HOLDER);
		final ValOp value = value().writeValue(valDirs);
		final Block code = valDirs.code();

		final DataOp ptr =
				value.value(null, code)
				.toPtr(null, code)
				.load(null, code)
				.toData(TARGET_ID, code);

		final Block resultCode = valDirs.done().code();

		return holder.hold(
				resultCode,
				anonymousObject(
						getBuilder(),
						ptr,
						linkStruct.getTypeRef().getType()));
	}

	@Override
	public void assign(CodeDirs dirs, HostOp value) {
		value().assign(
				dirs,
				value.materialize(dirs, tempObjHolder(dirs.getAllocator())));
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

	protected ObjOp dynamicCast(ID id, CodeDirs dirs, Obj ascendant) {

		final ObjectIR ascendantIR = ascendant.ir(getGenerator());
		final CodeDirs subDirs = dirs.begin(
				id != null ? id : CAST_ID,
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

		subDirs.done();

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

	private final ObjectIRBodyOp body(Code code) {
		return ptr().toAny(null, code).to(
				null,
				code,
				getWellKnownType().ir(getGenerator()).getBodyType());
	}

}
