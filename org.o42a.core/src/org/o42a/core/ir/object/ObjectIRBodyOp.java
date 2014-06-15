/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectDataIR.OBJECT_DATA_ID;
import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.*;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.Fld;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public final class ObjectIRBodyOp extends StructOp<ObjectIRBodyOp> {

	ObjectIRBodyOp(StructWriter<ObjectIRBodyOp> writer) {
		super(writer);
	}

	@Override
	public final ObjectIRBody getType() {
		return (ObjectIRBody) super.getType();
	}

	public final Obj getSampleDeclaration() {
		return getType().getSampleDeclaration();
	}

	public final StructRecOp<ObjectIRDescOp> declaredIn(Code code) {
		return ptr(null, code, getType().definedIn());
	}

	public final RelRecOp objectData(Code code) {
		return relPtr(null, code, getType().objectData());
	}

	public final Int32recOp flags(Code code) {
		return int32(null, code, getType().flags());
	}

	public final <O extends Fld.Op<O>> O field(
			Code code,
			Fld.Type<O> instance) {
		return struct(null, code, instance);
	}

	public final ObjOp op(
			CodeBuilder builder,
			Obj ascendant,
			ObjectPrecision precision) {
		return op(builder, null, ascendant, precision);
	}

	public final ObjectIRDataOp loadObjectData(Code code) {
		return objectData(code)
				.load(null, code)
				.offset(null, code, this)
				.to(OBJECT_DATA_ID, code, OBJECT_DATA_TYPE);
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

	final ObjOp op(ObjectIR objectIR, ObjectDataOp data, Obj ascendant) {
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
