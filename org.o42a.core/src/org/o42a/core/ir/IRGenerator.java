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
package org.o42a.core.ir;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.data.GlobalSettings;
import org.o42a.core.ir.object.*;


public class IRGenerator {

	private final Generator generator;

	public IRGenerator(Generator generator) {
		this.generator = generator;

		// Allocated types in the right order.
		// TODO implement forward type allocations.
		ObjectDataType.OBJECT_DATA_TYPE.data(generator);

		ObjectType.OBJECT_TYPE.data(generator);
		DepIR.DEP_IR.data(generator);

		AscendantDescIR.ASCENDANT_DESC_IR.data(generator);
		SampleDescIR.SAMPLE_DESC_IR.data(generator);
		FieldDescIR.FIELD_DESC_IR.data(generator);
		OverriderDescIR.OVERRIDER_DESC_IR.data(generator);
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final String getId() {
		return this.generator.getId();
	}

	public final CodeId id() {
		return getGenerator().id();
	}

	public final CodeId topId() {
		return getGenerator().topId();
	}

	public final CodeId id(String name) {
		return getGenerator().id(name);
	}

	public CodeId rawId(String id) {
		return getGenerator().rawId(id);
	}

	public final GlobalSettings newGlobal() {
		return this.generator.newGlobal();
	}

	public final FunctionSettings newFunction() {
		return this.generator.newFunction();
	}

	public <F extends Func> CodePtr<F> externalFunction(
			String name,
			Signature<F> signature) {
		return this.generator.externalFunction(name, signature);
	}

	@Override
	public String toString() {
		return this.generator.toString();
	}

}
