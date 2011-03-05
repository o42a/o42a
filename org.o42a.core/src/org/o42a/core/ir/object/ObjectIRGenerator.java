/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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

import org.o42a.codegen.Generator;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.ir.local.LocalIRGenerator;


public abstract class ObjectIRGenerator extends LocalIRGenerator {

	private final ObjectDataType objectDataType;
	private final ObjectType objectType;
	private final DepIR.Type depType;
	private final AscendantDescIR.Type ascendantDescType;
	private final SampleDescIR.Type sampleDescType;
	private final FieldDescIR.Type fieldDescType;
	private final OverriderDescIR.Type overriderDescType;
	private final CtrOp.Type ctr;

	public ObjectIRGenerator(Generator generator) {
		super(generator);
		this.objectDataType =
			generator.addType(new ObjectDataType((IRGenerator) this));

		// object type refers to descriptor type instances,
		// but not to their structures
		this.ascendantDescType = new AscendantDescIR.Type(this);
		this.sampleDescType = new SampleDescIR.Type();
		this.fieldDescType = new FieldDescIR.Type(this);
		this.overriderDescType = new OverriderDescIR.Type(this);

		this.objectType = generator.addType(new ObjectType(this));
		this.depType = generator.addType(new DepIR.Type());

		// deferred descriptor types allocation
		generator.addType(this.ascendantDescType);
		generator.addType(this.sampleDescType);
		generator.addType(this.fieldDescType);
		generator.addType(this.overriderDescType);

		this.ctr = generator.addType(new CtrOp.Type(this));
	}

	public final ObjectDataType objectDataType() {
		return this.objectDataType;
	}

	public final ObjectType objectType() {
		return this.objectType;
	}

	public final DepIR.Type depType() {
		return this.depType;
	}

	public final AscendantDescIR.Type ascendantDescType() {
		return this.ascendantDescType;
	}

	public final SampleDescIR.Type sampleDescType() {
		return this.sampleDescType;
	}

	public final FieldDescIR.Type fieldDescType() {
		return this.fieldDescType;
	}

	public final OverriderDescIR.Type overriderDescType() {
		return this.overriderDescType;
	}

	public final CtrOp.Type ctr() {
		return this.ctr;
	}

}
