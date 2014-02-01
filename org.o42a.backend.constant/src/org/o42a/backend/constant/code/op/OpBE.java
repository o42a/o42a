/*
    Constant Handler Compiler Back-end
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.backend.constant.code.op;

import org.o42a.analysis.use.SimpleUsage;
import org.o42a.analysis.use.User;
import org.o42a.analysis.use.UserInfo;
import org.o42a.backend.constant.code.CCode;
import org.o42a.codegen.code.op.Op;
import org.o42a.util.string.ID;


public abstract class OpBE<U extends Op> extends InstrBE {

	private final ID id;
	private U underlying;
	private COp<U, ?> op;

	public OpBE(ID id, CCode<?> code) {
		super(code);
		assert id != null :
			"Operation identifier not specified";
		this.id = id;
	}

	public final ID getId() {
		return this.id;
	}

	@Override
	public boolean isNoOp() {
		return this.op.getConstant() != null;
	}

	public final U underlying() {
		if (this.underlying != null) {
			return this.underlying;
		}

		part().revealUpTo(this);
		assert this.underlying != null :
			this + " not emitted yet ";

		return this.underlying;
	}

	@Override
	public final User<SimpleUsage> toUser() {
		return this.op.toUser();
	}

	@Override
	public final void useBy(UserInfo user) {
		this.op.useBy(user);
	}

	@Override
	public String toString() {
		if (this.id == null) {
			return super.toString();
		}
		return this.id.toString();
	}

	@Override
	protected final void emit() {
		this.underlying = write();
	}

	protected abstract U write();

	final void init(COp<U, ?> op) {
		this.op = op;
	}

}
