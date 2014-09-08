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

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.CtrOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.util.string.ID;


public final class ObjectsCode {

	public static final ID ANCESTOR_ID = ID.rawId("ancestor");
	public static final ID NEW_OBJECT_ID = ID.rawId("new_object");

	public static ObjectOp objectAncestor(
			CodeDirs dirs,
			HostOp host,
			Obj object,
			ObjHolder holder) {

		final CodeDirs subDirs = dirs.begin(ANCESTOR_ID, "Ancestor");

		final TypeRef ancestorType = object.type().getAncestor();
		final RefOp ancestor = ancestorType.op(host);

		final ObjectOp result = ancestor.path()
				.target()
				.materialize(subDirs, holder);

		subDirs.done();

		dirs.code().dumpName("Ancestor: ", result);

		return result;
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

	public final CtrOp allocateCtr(Code code) {
		return new CtrOp(
				getBuilder(),
				code.allocate(CTR_ID, ALLOCATABLE_CTR)::get);
	}

	public final ObjectOp objectAncestor(
			CodeDirs dirs,
			Obj object,
			ObjHolder holder) {
		return objectAncestor(dirs, host(), object, holder);
	}

}
