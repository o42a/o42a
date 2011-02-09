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
package org.o42a.core.ir.object;

import static org.o42a.core.ir.object.ObjectOp.anonymousObject;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.codegen.code.op.AnyOp;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.LclOp;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.LocalOp;
import org.o42a.core.ir.object.DepIR.Op;
import org.o42a.core.ir.op.IROp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;


public class DepOp extends IROp implements HostOp {

	private final ObjOp host;
	private final DepIR depIR;

	public DepOp(DepIR depIR, ObjOp host, DepIR.Op ptr) {
		super(host.getBuilder(), ptr);
		this.depIR = depIR;
		this.host = host;
	}

	public final ObjOp host() {
		return this.host;
	}

	public final DepIR depIR() {
		return this.depIR;
	}

	@Override
	public final DepIR.Op ptr() {
		return (Op) super.ptr();
	}

	@Override
	public ObjectOp toObject(Code code, CodePos exit) {

		final Artifact<?> target = depIR().getDep().getTarget();
		final Obj object = target.toObject();

		if (object == null) {
			return null;
		}

		return anonymousObject(
				getBuilder(),
				ptr().object(code).load(code),
				object);
	}

	@Override
	public LocalOp toLocal() {
		return null;
	}

	@Override
	public HostOp field(Code code, CodePos exit, MemberKey memberKey) {
		return toObject(code, exit).field(code, exit, memberKey);
	}

	@Override
	public ObjectOp materialize(Code code, CodePos exit) {

		final Artifact<?> target = depIR().getDep().getTarget();

		return anonymousObject(
				getBuilder(),
				ptr().object(code).load(code),
				target.materialize());
	}

	public void fill(LocalBuilder builder, Code code, CodePos exit) {

		final AnyOp value;
		final Field<?> dependency = depIR().getDep().getDependency();

		if (dependency != null) {

			final LclOp field = builder.host().field(
					code,
					exit,
					dependency.getKey());

			value = field.toAny(code);
		} else {
			value = builder.owner().toAny(code);
		}

		ptr().object(code).store(code, value);
	}

	@Override
	public String toString() {
		return "DepOp[" + this.depIR.getDep() + '@' + host() + ']';
	}

}
