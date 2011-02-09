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
package org.o42a.core.ir.op;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.data.GlobalSettings;


public abstract class IRGeneratorBase {

	private static final Signature<BinaryFunc> binary =
		new BinaryFunc.Binary();
	private static final Signature<ObjectRefFunc> objectRef =
		new ObjectRefFunc.ObjectRef();
	private static final Signature<ObjectCondFunc> objectCond =
		new ObjectCondFunc.ObjectCond();
	private static final Signature<AssignerFunc> assigner =
		new AssignerFunc.Assigner();

	private final Generator generator;
	private final ValOp.Type valType;
	private final RelList.Type relListType;
	private final Signature<ObjectValFunc> objectVal;

	public IRGeneratorBase(Generator generator) {
		this.generator = generator;
		this.valType = generator.addType(new ValOp.Type(this));
		this.relListType = generator.addType(new RelList.Type(this));
		this.objectVal = new ObjectValFunc.ObjectVal(this);
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

	public final ValOp.Type valType() {
		return this.valType;
	}

	public final RelList.Type relListType() {
		return this.relListType;
	}

	public final Signature<BinaryFunc> binarySignature() {
		return binary;
	}

	public final Signature<ObjectRefFunc> objectRefSignature() {
		return objectRef;
	}

	public final Signature<ObjectCondFunc> objectCondSignature() {
		return objectCond;
	}

	public final Signature<ObjectValFunc> objectValSignature() {
		return this.objectVal;
	}

	public final Signature<AssignerFunc> assignerSignature() {
		return assigner;
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
