/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import static org.o42a.core.ir.object.op.CtrOp.CTR_ID;
import static org.o42a.core.ir.object.op.CtrOp.CTR_TYPE;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.ir.object.ObjectIRTypeOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.CtrOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;


public final class ObjectsCode {

	public static ObjectOp objectAncestor(
			CodeDirs dirs,
			HostOp host,
			Obj object) {

		final TypeRef ancestorType = object.type().getAncestor();

		if (ancestorType == null) {
			return null;
		}

		final RefOp ancestor = ancestorType.op(host);

		return ancestor.target(dirs)
				.materialize(dirs, tempObjHolder(dirs.getAllocator()));
	}

	private final CodeBuilder builder;

	ObjectsCode(CodeBuilder builder) {
		this.builder = builder;
	}

	public final Generator getGenerator() {
		return getBuilder().getGenerator();
	}

	public final CodeBuilder getBuilder() {
		return this.builder;
	}

	public final HostOp host() {
		return getBuilder().host();
	}

	public final ObjectOp newObject(
			CodeDirs dirs,
			ObjHolder holder,
			ObjectOp owner,
			ObjectOp ancestor,
			Obj sample) {
		return newObject(
				dirs,
				holder,
				owner,
				ancestor == null
				? null : ancestor.objectType(dirs.code()).ptr(),
				sample);
	}

	public final ObjectOp newObject(
			CodeDirs dirs,
			ObjHolder holder,
			ObjectOp owner,
			ObjectIRTypeOp ancestor,
			Obj sample) {

		final Code alloc = dirs.code().getAllocator().allocation();
		final CtrOp.Op ctr = alloc.allocate(CTR_ID, CTR_TYPE);
		final ObjectOp newObject = ctr.op(this).newObject(
				dirs,
				holder,
				owner,
				ancestor,
				sample.ir(getGenerator()).op(getBuilder(), dirs.code()));

		newObject.fillDeps(dirs, sample);

		return newObject;
	}

	public final ObjectOp objectAncestor(CodeDirs dirs, Obj object) {
		return objectAncestor(dirs, host(), object);
	}

}