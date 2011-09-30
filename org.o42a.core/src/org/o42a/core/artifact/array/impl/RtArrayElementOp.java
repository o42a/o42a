/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.artifact.array.impl;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.source.CompilerContext;


final class RtArrayElementOp extends RefOp implements HostOp {

	RtArrayElementOp(HostOp host, RtArrayElementConstructor ref) {
		super(host, ref);
	}

	@Override
	public HostOp target(CodeDirs dirs) {
		return this;
	}

	@Override
	public CompilerContext getContext() {
		return getRef().getContext();
	}

	@Override
	public ObjectOp toObject(CodeDirs dirs) {
		return null;
	}

	@Override
	public LocalOp toLocal() {
		return null;
	}

	@Override
	public HostOp field(CodeDirs dirs, MemberKey memberKey) {
		return null;
	}

	@Override
	public ObjectOp materialize(CodeDirs dirs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void assign(CodeDirs dirs, HostOp value) {
		// TODO Auto-generated method stub

	}

}
