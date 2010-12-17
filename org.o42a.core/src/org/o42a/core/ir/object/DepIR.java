/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.AnyPtrRec;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.Dep;


public class DepIR {

	private final IRGenerator generator;
	private final Dep dep;
	private Type instance;

	public DepIR(IRGenerator generator, Dep dep) {
		this.generator = generator;
		this.dep = dep;
	}

	public final IRGenerator getGenerator() {
		return this.generator;
	}

	public final Dep getDep() {
		return this.dep;
	}

	public Type getInstance() {
		return this.instance;
	}

	public final DepOp op(Code code, ObjOp host) {
		assert getInstance() != null :
			this + " is not allocated yet";
		return new DepOp(
				this,
				host,
				host.ptr().writer().struct(code, getInstance()));
	}

	void allocate(SubData<?> data) {

		final String localName;

		if (getDep().dependencyOnEnclosingOwner()) {
			localName = "_owner";
		} else {

			final Field<?> dependency = this.dep.getDependency();

			localName = "_dep_" + dependency.ir(getGenerator()).getLocalName();
		}

		this.instance = data.addInstance(localName, getGenerator().depType());
		this.instance.getObject().setNull();
	}

	@Override
	public String toString() {
		return this.dep + " IR";
	}

	public static final class Type extends org.o42a.codegen.data.Type<Op> {

		private AnyPtrRec object;

		Type() {
			super("Dep");
		}

		public final AnyPtrRec getObject() {
			return this.object;
		}

		@Override
		public Op op(StructWriter writer) {
			return new Op(writer);
		}

		@Override
		protected void allocate(SubData<Op> data) {
			this.object = data.addPtr("object");
		}

	}

	public static final class Op extends StructOp {

		private Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public Type getType() {
			return (Type) super.getType();
		}

		public final DataOp<AnyOp> object(Code code) {
			return writer().ptr(code, getType().getObject());
		}

		@Override
		public Op create(StructWriter writer) {
			return new Op(writer);
		}

	}

}
