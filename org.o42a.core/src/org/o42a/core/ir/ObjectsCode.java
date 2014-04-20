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
package org.o42a.core.ir;

import static org.o42a.core.ir.object.op.CtrOp.ALLOCATABLE_CTR;
import static org.o42a.core.ir.object.op.CtrOp.CTR_ID;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Allocated;
import org.o42a.codegen.code.Block;
import org.o42a.core.ir.object.ObjectIRDataOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.CtrOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.HostOp;
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

		return ancestor.path()
				.target()
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
			HostOp host,
			ObjHolder holder,
			ObjectOp owner,
			ObjectOp ancestor,
			Obj sample) {

		final Block code = dirs.code();

		return newObject(
				dirs,
				host,
				holder,
				owner,
				ancestor == null ? null : ancestor.objectData(code).ptr(),
				sample);
	}

	public final ObjectOp newObject(
			CodeDirs dirs,
			HostOp host,
			ObjHolder holder,
			ObjectOp owner,
			ObjectIRDataOp ancestorData,
			Obj sample) {

		final Block code = dirs.code();
		final Allocated<CtrOp.Op> ctr = code.allocate(CTR_ID, ALLOCATABLE_CTR);
		final ObjectOp newObject = new CtrOp(getBuilder(), ctr).newObject(
				dirs,
				holder,
				owner,
				ancestorData,
				sample.ir(getGenerator()).op(getBuilder(), code));

		if (host != null) {
			newObject.fillDeps(dirs, host, sample);
		}

		return newObject;
	}

	public final ObjectOp objectAncestor(CodeDirs dirs, Obj object) {
		return objectAncestor(dirs, host(), object);
	}

}
