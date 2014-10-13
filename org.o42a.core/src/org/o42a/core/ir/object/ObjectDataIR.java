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
import static org.o42a.core.ir.value.Val.FALSE_VAL;
import static org.o42a.core.ir.value.Val.INDEFINITE_VAL;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.value.Val;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueKnowledge;
import org.o42a.util.string.ID;


public final class ObjectDataIR implements Content<ObjectIRData> {

	public static final ID OBJECT_DATA_ID = ID.id("object_data");

	private final ObjectIR objectIR;
	private final ID id;
	private ObjectIRData instance;
	private Val initialValue;

	ObjectDataIR(ObjectIR objectIR) {
		this.objectIR = objectIR;
		this.id = objectIR.getId().setLocal(OBJECT_DATA_ID);
	}

	public final Generator getGenerator() {
		return getObjectIR().getGenerator();
	}

	public final ID getId() {
		return this.id;
	}

	public final ObjectIR getObjectIR() {
		return this.objectIR;
	}

	public final ObjectIRData getInstance() {
		if (this.instance == null) {
			getObjectIR().allocate();
		}
		return this.instance;
	}

	@Override
	public void allocated(ObjectIRData instance) {
		this.instance = instance;
	}

	public Val getInitialValue() {
		if (this.initialValue != null) {
			return this.initialValue;
		}

		final Obj object = getObjectIR().getObject();
		final boolean eager = object.value().getStatefulness().isEager();

		if (!eager && !object.type().getValueType().isStateful()) {
			// Stateless object has no initial value.
			return this.initialValue = INDEFINITE_VAL;
		}

		final ValueKnowledge knowledge =
				object.value().getValue().getKnowledge();

		if (!knowledge.isInitiallyKnown()) {
			return this.initialValue = INDEFINITE_VAL;
		}
		if (knowledge.isFalse()) {
			return FALSE_VAL;
		}

		final Val initialValue =
				getObjectIR().getValueIR().initialValue(this);

		if (initialValue == null) {
			return this.initialValue = INDEFINITE_VAL;
		}

		return this.initialValue = initialValue;
	}

	@Override
	public void fill(ObjectIRData instance) {
		instance.vmtc().setConstant(true).setValue(
				getObjectIR()
				.getVmtIR()
				.terminator()
				.pointer(getGenerator()));

		instance.value().set(getInitialValue());

		instance.desc()
		.setConstant(true)
		.setValue(getObjectIR().getDescIR().ptr());
	}

	public final ObjectDataOp op(CodeBuilder builder, Code code) {
		return getInstance()
				.pointer(getGenerator())
				.op(null, code)
				.op(builder);
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

	ObjectIRData allocateType(SubData<?> data) {
		return data.addNewInstance(OBJECT_DATA_ID, OBJECT_DATA_TYPE);
	}

	ObjectIRData allocateInstance(SubData<?> data) {

		final ObjectIRData result =
				data.addNewInstance(OBJECT_DATA_ID, OBJECT_DATA_TYPE, this);

		getObjectIR().getObjectValueIR().allocate(this);

		return result;
	}

}
