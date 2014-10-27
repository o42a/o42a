/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.ir.field.object;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.util.fn.Init.init;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.OpMeans;
import org.o42a.codegen.data.Ptr;
import org.o42a.codegen.data.StructRec;
import org.o42a.core.ir.field.*;
import org.o42a.core.ir.field.RefFld.StatefulOp;
import org.o42a.core.ir.field.RefFld.StatefulType;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.util.fn.Init;


public class ObjFld extends RefFld<
		StatefulOp,
		StatefulType,
		Ptr<ObjFldConf.Op>,
		StructRec<ObjFldConf.Op>> {

	private final Init<ObjFldConf> conf = init(this::findConf);

	public ObjFld(
			ObjectIRBody bodyIR,
			Field field,
			boolean dummy) {
		super(bodyIR, field, dummy, field.toObject(), field.toObject());
	}

	@Override
	public final FldKind getKind() {
		return FldKind.OBJ;
	}

	public final ObjFldConf conf() {
		return this.conf.get();
	}

	@Override
	protected StatefulType getType() {
		return STATEFUL_FLD;
	}

	@Override
	protected Obj targetType(Obj bodyType) {
		return bodyType.member(getField().getKey())
				.toField()
				.object(dummyUser());
	}

	@Override
	protected ObjVmtRecord createVmtRecord() {
		return new ObjVmtRecord(this);
	}

	@Override
	protected ObjFldOp op(
			Code code,
			ObjOp host,
			OpMeans<RefFld.StatefulOp> ptr) {
		return new ObjFldOp(host, this, ptr);
	}

	private ObjFldConf findConf() {
		assert !isDummy() :
			"Can not create config for dummy field " + this;
		// TODO Reuse the object field config when possible.
		return new ObjFldConf(this);
	}

}
