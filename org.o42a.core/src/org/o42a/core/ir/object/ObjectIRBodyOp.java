/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;
import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.ObjectTypeIR.OBJECT_TYPE_ID;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.state.DepIR;
import org.o42a.core.ir.object.state.KeeperIROp;
import org.o42a.core.ir.object.state.KeeperIRType;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.string.ID;


public final class ObjectIRBodyOp extends StructOp<ObjectIRBodyOp> {

	ObjectIRBodyOp(StructWriter<ObjectIRBodyOp> writer) {
		super(writer);
	}

	@Override
	public final ObjectIRBody getType() {
		return (ObjectIRBody) super.getType();
	}

	public final Obj getAscendant() {
		return getType().getAscendant();
	}

	public final RelRecOp objectType(Code code) {
		return relPtr(null, code, getType().objectType());
	}

	public final RelRecOp ancestorBody(Code code) {
		return relPtr(null, code, getType().ancestorBody());
	}

	public final DataRecOp methods(Code code) {
		return ptr(null, code, getType().methods());
	}

	public final Int32recOp flags(Code code) {
		return int32(null, code, getType().flags());
	}

	public final <O extends Fld.Op<O>> O field(
			Code code,
			Fld.Type<O> instance) {
		return struct(null, code, instance);
	}

	public final DepIR.Op dep(Code code, DepIR.Type instance) {
		return struct(null, code, instance);
	}

	public final KeeperIROp<?> keeper(Code code, KeeperIRType<?> instance) {
		return struct(null, code, instance);
	}

	public final ObjOp op(
			CodeBuilder builder,
			Obj ascendant,
			ObjectPrecision precision) {
		return op(builder, null, ascendant, precision);
	}

	public final ObjectIRTypeOp loadObjectType(Code code) {
		return objectType(code)
				.load(null, code)
				.offset(null, code, this)
				.to(OBJECT_TYPE_ID, code, OBJECT_TYPE);
	}

	public final ObjectOp loadAncestor(CodeBuilder builder, Code code) {

		final TypeRef ancestorRef = getAscendant().type().getAncestor();
		final Obj ancestor;

		if (ancestorRef == null) {
			ancestor = builder.getContext().getVoid();
		} else {
			ancestor = ancestorRef.getType();
		}

		final AnyOp ancestorBodyPtr =
				ancestorBody(code)
				.load(null, code)
				.offset(null, code, this);

		return anonymousObject(
				builder,
				ancestorBodyPtr.toData(null, code),
				ancestor);
	}

	public final ObjectIRMethodsOp loadMethods(Code code) {

		final DataOp methodsPtr = methods(code).load(null, code);

		return methodsPtr.to(null, code, getType().getMethodsIR());
	}

	@Override
	public String toString() {
		return "*" + getType().getId();
	}

	@Override
	protected ID fieldId(Code code, ID local) {
		return ObjectIRBody.BODY_ID.setLocal(local);
	}

	final ObjOp op(
			CodeBuilder builder,
			ObjectIR objectIR,
			Obj ascendant,
			ObjectPrecision precision) {
		return new ObjOp(
				builder,
				objectIR != null ? objectIR : getType().getObjectIR(),
				this,
				ascendant,
				precision);
	}

	final ObjOp op(ObjectIR objectIR, ObjectTypeOp data, Obj ascendant) {
		return new ObjOp(
				objectIR != null ? objectIR : getType().getObjectIR(),
				this,
				ascendant,
				data);
	}

	final ObjOp op(CodeBuilder builder, ObjectIR objectIR) {
		return new ObjOp(builder, objectIR, this);
	}

	FldOp<?> declaredField(Code code, ObjOp host, MemberKey memberKey) {

		final Fld<?> declared = getType().fld(memberKey);

		assert declared != null :
			memberKey + " is not declared in " + this;

		return declared.op(code, host);
	}

}
