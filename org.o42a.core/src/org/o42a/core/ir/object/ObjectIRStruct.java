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
package org.o42a.core.ir.object;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.Struct;
import org.o42a.codegen.data.SubData;
import org.o42a.util.string.ID;


public final class ObjectIRStruct extends Struct<ObjectIROp> {

	public static final ID OBJECT_ID = ID.id("object");

	private final ObjectIR objectIR;
	private ObjectIRData objectData;

	ObjectIRStruct(ObjectIR objectIR) {
		super(objectIR.getId().detail(OBJECT_ID));
		this.objectIR = objectIR;
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final ObjectIRData objectData() {
		return this.objectData;
	}

	@Override
	public ObjectIROp op(StructWriter<ObjectIROp> writer) {
		return new ObjectIROp(writer);
	}

	@Override
	protected final void allocate(SubData<ObjectIROp> data) {
		this.objectData = getObjectIR().getDataIR().allocate(data);
		allocateBodyIRs(data);
	}

	@Override
	protected void fill() {
	}

	private void allocateBodyIRs(SubData<?> data) {
		for (ObjectIRBody body : getObjectIR().getBodyIRs()) {
			body.allocate(data);
		}
	}

}
