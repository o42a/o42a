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
package org.o42a.core.ir.field;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Signature;
import org.o42a.core.ir.op.IRGeneratorBase;


public abstract class FieldIRGenerator extends IRGeneratorBase {

	private final ScopeFld.Type scopeFldType;
	private final ObjFld.Type objFldType;
	private final LinkFld.Type linkFldType;
	private final VarFld.Type varFldType;
	private final Signature<ObjectConstructorFunc> objectConstructorSignature;

	public FieldIRGenerator(Generator generator) {
		super(generator);

		this.objectConstructorSignature =
			new ObjectConstructorFunc.ObjectConstructor();

		this.scopeFldType = generator.addType(new ScopeFld.Type(this));
		this.objFldType = generator.addType(new ObjFld.Type(this));
		this.linkFldType = generator.addType(new LinkFld.Type(this));
		this.varFldType = generator.addType(new VarFld.Type(this));

	}

	public final ScopeFld.Type scopeFldType() {
		return this.scopeFldType;
	}

	public final ObjFld.Type objFldType() {
		return this.objFldType;
	}

	public final LinkFld.Type linkFldType() {
		return this.linkFldType;
	}

	public final VarFld.Type varFldType() {
		return this.varFldType;
	}

	public final Signature<ObjectConstructorFunc> objectConstructorSignature() {
		return this.objectConstructorSignature;
	}

}
