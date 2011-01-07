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

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.*;
import org.o42a.codegen.code.backend.FuncCaller;
import org.o42a.codegen.code.backend.SignatureWriter;
import org.o42a.codegen.data.GlobalSettings;


public abstract class IRGeneratorBase {

	private static final Object object = new Object();
	private static final Binary binary = new Binary();
	private static final ObjectRef objectRef = new ObjectRef();
	private static final ObjectCond objectCond = new ObjectCond();
	private static final Assigner assigner = new Assigner();

	private final Generator generator;
	private final ValOp.Type valType;
	private final RelList.Type relListType;
	private final ObjectVal objectVal;

	public IRGeneratorBase(Generator generator) {
		this.generator = generator;
		this.valType = generator.addType(new ValOp.Type());
		this.relListType = generator.addType(new RelList.Type());
		this.objectVal = new ObjectVal();
	}

	public final Generator getGenerator() {
		return this.generator;
	}

	public final String getId() {
		return this.generator.getId();
	}

	public final ValOp.Type valType() {
		return this.valType;
	}

	public final RelList.Type relListType() {
		return this.relListType;
	}

	public final Signature<ObjectFunc> objectSignature() {
		return object;
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

	private static final class Object extends Signature<ObjectFunc> {

		Object() {
			super("void", "ObjectF", "any*");
		}

		@Override
		public ObjectFunc op(FuncCaller caller) {
			return new ObjectFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<ObjectFunc> writer) {
			writer.returnVoid();
			writer.addAny();
		}

	}

	private static final class Binary extends Signature<BinaryFunc> {

		Binary() {
			super("any*", "BinaryF", "any*, any*");
		}

		@Override
		public BinaryFunc op(FuncCaller caller) {
			return new BinaryFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<BinaryFunc> writer) {
			writer.returnAny();
			writer.addAny();
			writer.addAny();
		}

	}

	private static final class ObjectRef extends Signature<ObjectRefFunc> {

		ObjectRef() {
			super("any*", "ObjectRefF", "any*");
		}

		@Override
		public ObjectRefFunc op(FuncCaller caller) {
			return new ObjectRefFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<ObjectRefFunc> writer) {
			writer.returnAny();
			writer.addAny();
		}

	}

	private final class ObjectVal extends Signature<ObjectValFunc> {

		ObjectVal() {
			super("void", "ObjectValF", "val*, any*");
		}

		@Override
		public ObjectValFunc op(FuncCaller caller) {
			return new ObjectValFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<ObjectValFunc> writer) {
			writer.returnVoid();
			writer.addPtr(valType());
			writer.addAny();
		}

	}

	private static final class ObjectCond extends Signature<ObjectCondFunc> {

		ObjectCond() {
			super("bool", "ObjectCondF", "any*");
		}

		@Override
		public ObjectCondFunc op(FuncCaller caller) {
			return new ObjectCondFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<ObjectCondFunc> writer) {
			writer.returnBool();
			writer.addAny();
		}

	}

	private static final class Assigner extends Signature<AssignerFunc> {

		Assigner() {
			super("bool", "AssignerF", "any*, any*");
		}

		@Override
		public AssignerFunc op(FuncCaller caller) {
			return new AssignerFunc(caller);
		}

		@Override
		protected void write(SignatureWriter<AssignerFunc> writer) {
			writer.returnBool();
			writer.addAny();
			writer.addAny();
		}

	}

}
