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

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.backend.StructWriter;
import org.o42a.codegen.code.op.StructOp;
import org.o42a.codegen.data.Content;
import org.o42a.codegen.data.SubData;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectBodyIR;
import org.o42a.core.member.field.Field;


public abstract class Fld {

	private final Field<?> field;
	private final ObjectBodyIR bodyIR;
	private Type<?> instance;

	Fld(ObjectBodyIR bodyIR, Field<?> field) {
		this.field = field;
		this.bodyIR = bodyIR;
	}

	public final Generator getGenerator() {
		return getBodyIR().getGenerator();
	}

	public Field<?> getField() {
		return this.field;
	}

	public final ObjectBodyIR getBodyIR() {
		return this.bodyIR;
	}

	public CodeId getId() {
		return getField().ir(getGenerator()).getId();
	}

	public abstract FldKind getKind();

	public boolean isOverrider() {

		final Field<?> field = getField();

		if (!field.isOverride()) {
			// new field declaration
			return false;
		}
		if (field.isClone()) {
			// clone does not override anything
			return false;
		}

		return true;
	}

	public Obj getDeclaredIn() {

		final Field<?> original = getField().getOriginal();

		return original.getEnclosingContainer().toObject();
	}

	public Type<?> getInstance() {
		return this.instance;
	}

	public abstract FldOp op(Code code, ObjOp host);

	public void targetAllocated() {
	}

	@Override
	public String toString() {
		return getField().toString();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void allocate(SubData<?> data) {
		this.instance = data.addInstance(
				getGenerator().id("fld").detail(getId().getLocal()),
				(Type) getType(),
				(Content) content());
	}

	protected abstract Type<?> getType();

	protected Content<?> content() {
		return null;
	}

	public static abstract class Op extends StructOp {

		public Op(StructWriter writer) {
			super(writer);
		}

		@Override
		public Type<?> getType() {
			return (Type<?>) super.getType();
		}

	}

	public static abstract class Type<O extends Op>
			extends org.o42a.codegen.data.Type<O> {

	}

}
