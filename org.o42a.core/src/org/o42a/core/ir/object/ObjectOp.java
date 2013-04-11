/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.FuncPtr;
import org.o42a.codegen.code.op.BoolOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.impl.AnonymousObjOp;
import org.o42a.core.ir.object.op.CastObjectFunc;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.object.state.DepOp;
import org.o42a.core.ir.object.state.KeeperOp;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ir.value.type.ValueOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.object.state.Keeper;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.string.ID;


public abstract class ObjectOp extends IROp implements TargetOp {

	protected static final ID CAST_ID = ID.id("cast");
	protected static final ID TARGET_ID = ID.id("target");
	protected static final ID FIELD_HOST_ID = ID.id("field_host");
	protected static final ID DEP_HOST_ID = ID.id("dep_host");
	protected static final ID KEEPER_HOST_ID = ID.id("keeper_host");

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

	public void fillDeps(CodeDirs dirs, HostOp host, Obj sample) {

		final Analyzer analyzer = getGenerator().getAnalyzer();

		for (Dep dep : sample.deps()) {
			if (dep.exists(analyzer)) {
				fillDep(dirs, host, dep);
			}
		}
	}

	public abstract ObjOp cast(ID id, CodeDirs dirs, Obj ascendant);

	@Override
	public abstract ValueOp value();

	@Override
	public final TargetOp target(CodeDirs dirs) {
		return this;
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

	public final ObjectIRMethodsOp methods(Code code) {
		return body(code).loadMethods(code);
	}

	@Override
	public abstract FldOp field(CodeDirs dirs, MemberKey memberKey);

	public abstract DepOp dep(CodeDirs dirs, Dep dep);

	public abstract KeeperOp keeper(CodeDirs dirs, Keeper keeper);

	@Override
	public final ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {
		return holder.hold(dirs.code(), this);
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs, ObjHolder holder) {

		final TypeParameters<?> typeParameters =
				getWellKnownType().type().getParameters();
		final LinkValueType linkType =
				typeParameters.getValueType().toLinkType();

		assert typeParameters != null :
			"Not a link: " + this;

		final ValDirs valDirs =
				dirs.nested().value("link", linkType, TEMP_VAL_HOLDER);
		final ValOp value = value().writeValue(valDirs);
		final Block code = valDirs.code();

		final DataOp ptr =
				value.value(null, code)
				.toRec(null, code)
				.load(null, code)
				.toData(TARGET_ID, code);
		final ObjectOp result = holder.holdVolatile(
				code,
				anonymousObject(
						getBuilder(),
						ptr,
						linkType.interfaceRef(typeParameters).getType()));

		valDirs.done();

		return result;
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
				castFunc()
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

	private FuncPtr<CastObjectFunc> castFunc() {
		return getGenerator()
				.externalFunction()
				.noSideEffects()
				.link("o42a_obj_cast", CAST_OBJECT);
	}

	private final ObjectIRBodyOp body(Code code) {
		return ptr().toAny(null, code).to(
				null,
				code,
				getWellKnownType().ir(getGenerator()).getBodyType());
	}

	private final void fillDep(CodeDirs dirs, HostOp host, Dep dep) {

		final CodeDirs depDirs = dirs.begin(getId(), "Fill " + dep);

		dep(depDirs, dep).fill(depDirs, host);

		depDirs.done();
	}

}
