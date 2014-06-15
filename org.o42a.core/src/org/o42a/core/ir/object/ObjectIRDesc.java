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

import static org.o42a.core.ir.object.ObjectIRData.OBJECT_DATA_TYPE;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.impl.ObjectIRFields;
import org.o42a.core.ir.object.impl.ObjectIROverriders;
import org.o42a.core.ir.object.type.FieldDescIR;
import org.o42a.core.ir.object.type.OverriderDescIR;
import org.o42a.core.ir.op.RelList;
import org.o42a.util.string.ID;


public class ObjectIRDesc extends Type<ObjectIRDescOp> {

	public static final ObjectIRDesc OBJECT_DESC_TYPE = new ObjectIRDesc();

	private StructRec<ObjectIRDescOp> declaration;
	private StructRec<ObjectIRDataOp> data;
	private RelList<FieldDescIR> fields;
	private RelList<OverriderDescIR> overriders;
	private Int32rec mainBodyLayout;

	private ObjectIRDesc() {
		super(ID.rawId("o42a_obj_desc_t"));
	}

	public final StructRec<ObjectIRDescOp> declaration() {
		return this.declaration;
	}

	public final StructRec<ObjectIRDataOp> data() {
		return this.data;
	}

	public final RelList<FieldDescIR> fields() {
		return this.fields;
	}

	public final RelList<OverriderDescIR> overriders() {
		return this.overriders;
	}

	public final Int32rec mainBodyLayout() {
		return this.mainBodyLayout;
	}

	@Override
	public ObjectIRDescOp op(StructWriter<ObjectIRDescOp> writer) {
		return new ObjectIRDescOp(writer);
	}

	@Override
	protected void allocate(SubData<ObjectIRDescOp> data) {
		this.declaration = data.addPtr("declaration", OBJECT_DESC_TYPE);
		this.data = data.addPtr("data", OBJECT_DATA_TYPE);
		this.fields = new ObjectIRFields().allocate(data, "fields");
		this.overriders = new ObjectIROverriders().allocate(data, "overriders");
		this.mainBodyLayout = data.addInt32("main_body_layout");
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0101);
	}

}
