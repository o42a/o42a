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
package org.o42a.core.ir.object.type;

import static org.o42a.core.ir.object.type.ValueTypeDescOp.VALUE_TYPE_DESC_TYPE;

import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.data.*;
import org.o42a.codegen.debug.DebugTypeInfo;
import org.o42a.core.ir.object.ObjectIRBody;
import org.o42a.core.ir.op.RelList;
import org.o42a.util.string.ID;


public class ObjectIRDesc extends Type<ObjectIRDescOp> {

	public static final ObjectIRDesc OBJECT_DESC_TYPE = new ObjectIRDesc();

	private StructRec<ValueTypeDescOp> valueType;
	private RelList<FieldDescIR> fields;
	private RelList<ObjectIRBody> ascendants;
	private Int32rec mainBodyLayout;

	private ObjectIRDesc() {
		super(ID.rawId("o42a_obj_desc_t"));
	}

	public final StructRec<ValueTypeDescOp> valueType() {
		return this.valueType;
	}

	public final RelList<FieldDescIR> fields() {
		return this.fields;
	}

	public final RelList<ObjectIRBody> ascendants() {
		return this.ascendants;
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
		this.valueType = data.addPtr("value_type", VALUE_TYPE_DESC_TYPE);
		this.fields = new ObjectIRFields().allocate(data, "fields");
		this.ascendants = new ObjectIRAscendants().allocate(data, "ascendants");
		this.mainBodyLayout = data.addInt32("main_body_layout");
	}

	@Override
	protected DebugTypeInfo createTypeInfo() {
		return externalTypeInfo(0x042a0101);
	}

}
