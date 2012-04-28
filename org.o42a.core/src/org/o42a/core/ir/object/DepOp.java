/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.IROp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.local.Dep;
import org.o42a.core.ref.Ref;


public class DepOp extends IROp implements HostOp {

	private final ObjOp host;
	private final DepIR depIR;
	private final DepIR.Op ptr;

	public DepOp(DepIR depIR, ObjOp host, DepIR.Op ptr) {
		super(host.getBuilder());
		this.depIR = depIR;
		this.host = host;
		this.ptr = ptr;
	}

	public final Dep getDep() {
		return depIR().getDep();
	}

	public final ObjOp host() {
		return this.host;
	}

	public final DepIR depIR() {
		return this.depIR;
	}

	@Override
	public final DepIR.Op ptr() {
		return this.ptr;
	}

	@Override
	public final LocalOp toLocal() {
		return null;
	}

	@Override
	public HostOp field(CodeDirs dirs, MemberKey memberKey) {
		return materialize(dirs).field(dirs, memberKey);
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs) {

		final Code code = dirs.code();

		return anonymousObject(
				getBuilder(),
				ptr().object(code).load(null, code),
				getDep().getDepTarget());
	}

	@Override
	public ObjectOp dereference(CodeDirs dirs) {
		return materialize(dirs).dereference(dirs);
	}

	@Override
	public void assign(CodeDirs dirs, HostOp value) {
		throw new UnsupportedOperationException();
	}

	public void fill(CodeBuilder builder, CodeDirs dirs) {

		final DataOp object = object(builder, dirs);
		final Code code = dirs.code();

		ptr().object(code).store(code, object);
		code.dump(getDep() + ": ", this);
	}

	private DataOp object(CodeBuilder builder, CodeDirs dirs) {

		final Code code = dirs.code();

		switch (getDep().getDepKind()) {
		case ENCLOSING_OWNER_DEP:
			return builder.owner().toData(null, code);
		case REF_DEP:

			final Ref depRef = getDep().getDepRef();
			final HostOp refTarget = depRef.op(builder.host()).target(dirs);

			return refTarget.materialize(dirs).toData(null, code);
		}

		throw new IllegalStateException(
				"Dependency of unsupported kind: " + getDep()) ;
	}

	@Override
	public String toString() {
		return "DepOp[" + getDep() + '@' + host() + ']';
	}

}
