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

import static org.o42a.core.ir.object.ObjectIRType.OBJECT_TYPE;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.StructRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.object.Obj;
import org.o42a.util.string.ID;


public final class ObjectIRMethods extends Struct<ObjectIRMethodsOp> {

	static final ID METHODS_ID = ID.id().detail("methods");

	private final ObjectIRBody bodyIR;
	private StructRec<ObjectIRTypeOp> objectType;

	ObjectIRMethods(ObjectIRBody bodyIR) {
		super(buildId(bodyIR));
		this.bodyIR = bodyIR;
	}

	public final ObjectIR getObjectIR() {
		return this.bodyIR.getObjectIR();
	}

	public final ObjectIRBody getBodyIR() {
		return this.bodyIR;
	}

	public final StructRec<ObjectIRTypeOp> objectType() {
		return this.objectType;
	}

	@Override
	public final ObjectIRMethodsOp op(StructWriter<ObjectIRMethodsOp> writer) {
		return new ObjectIRMethodsOp(writer);
	}

	@Override
	protected void allocate(SubData<ObjectIRMethodsOp> data) {
		this.objectType = data.addPtr("object_type", OBJECT_TYPE);
	}

	@Override
	protected void fill() {

		final ObjectIR ascendantIR =
				getBodyIR().getAscendant().ir(getGenerator());

		this.objectType.setConstant(true).setValue(
				ascendantIR.getTypeIR().getObjectType()
				.data(getGenerator()).getPointer());
	}

	private static ID buildId(ObjectIRBody bodyIR) {

		final ObjectIR objectIR = bodyIR.getObjectIR();
		final Obj ascendant = bodyIR.getAscendant();
		final ID localId;

		if (bodyIR.isMain()) {
			localId = METHODS_ID;
		} else {
			localId = METHODS_ID.detail(
					ascendant.ir(objectIR.getGenerator()).getId());
		}

		return objectIR.getId().setLocal(localId);
	}

}
