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
package org.o42a.core.ir.object.state;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;
import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.DataOp;
import org.o42a.codegen.code.op.DataRecOp;
import org.o42a.core.ir.object.ObjOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.state.Dep;
import org.o42a.util.string.ID;


public class DepOp extends IROp implements TargetOp, HostValueOp {

	public static final ID DEP_ID = ID.id("dep");

	private final ObjOp host;
	private final DepIR depIR;
	private final DepIR.Op ptr;

	DepOp(ObjOp host, DepIR depIR, DepIR.Op ptr) {
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
	public final HostValueOp value() {
		return this;
	}

	@Override
	public TargetOp target(CodeDirs dirs) {
		return this;
	}

	@Override
	public void writeCond(CodeDirs dirs) {
		loadDep(dirs).value().writeCond(dirs);
	}

	@Override
	public ValOp writeValue(ValDirs dirs) {
		return loadDep(dirs.dirs()).value().writeValue(dirs);
	}

	@Override
	public TargetOp field(CodeDirs dirs, MemberKey memberKey) {
		return loadDep(dirs).field(dirs, memberKey);
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs, ObjHolder holder) {

		final Block code = dirs.code();

		return holder.hold(code, object(code));
	}

	@Override
	public TargetOp dereference(CodeDirs dirs, ObjHolder holder) {
		return loadDep(dirs).dereference(dirs, holder);
	}

	@Override
	public void assign(CodeDirs dirs, HostOp value) {
		throw new UnsupportedOperationException();
	}

	public void fill(CodeDirs dirs, HostOp host) {
		dirs.code().debug("Depends on " + getDep().ref());

		final DataRecOp objectRec = ptr().object(dirs.code());
		final Block noDep = dirs.addBlock("no_dep");
		final CodeDirs depDirs =
				dirs.getBuilder().dirs(dirs.code(), noDep.head());

		final DataOp object = createObject(depDirs, host);
		final Block code = depDirs.code();

		objectRec.store(code, object);
		code.dump(getDep() + " := ", this);

		if (noDep.exists()) {

			final ObjectIR noneIR =
					getContext().getNone().ir(getGenerator());

			objectRec.store(
					noDep,
					noneIR.op(getBuilder(), noDep).toData(null, noDep));
			noDep.go(code.tail());
		}

		depDirs.done();
	}

	@Override
	public String toString() {
		return "DepOp[" + getDep() + '@' + host() + ']';
	}

	private TargetOp loadDep(CodeDirs dirs) {
		return materialize(dirs, tempObjHolder(dirs.getAllocator()));
	}

	private DataOp createObject(CodeDirs dirs, HostOp owner) {

		final HostOp target = getDep().ref().op(owner).target(dirs);

		return target.materialize(dirs, tempObjHolder(dirs.getAllocator()))
				.toData(null, dirs.code());
	}

	private ObjectOp object(final Block code) {
		return anonymousObject(
				getBuilder(),
				ptr().object(code).load(null, code),
				getDep().getDepTarget());
	}

}
