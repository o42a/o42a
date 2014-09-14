/*
    Constant Handler Compiler Back-end
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import static org.o42a.analysis.use.SimpleUsage.SIMPLE_USAGE;
import static org.o42a.analysis.use.SimpleUsage.simpleUsable;

import org.o42a.analysis.use.*;
import org.o42a.backend.constant.code.CCodePart;
import org.o42a.backend.constant.data.ConstBackend;
import org.o42a.codegen.code.op.Op;
import org.o42a.util.string.ID;


public abstract class COp<U extends Op, T> implements Op, UserInfo {

	private final OpBE<U> backend;
	private final T constant;
	private final Usable<SimpleUsage> allUses;

	public COp(OpBE<U> backend) {
		this(backend, null);
	}

	public COp(OpBE<U> backend, T constant) {
		this.backend = backend;
		this.constant = constant;
		this.allUses = simpleUsable(this);
		this.backend.init(this);
	}

	public final ConstBackend getBackend() {
		return part().code().getBackend();
	}

	public final CCodePart<?> part() {
		return backend().part();
	}

	public final OpBE<U> backend() {
		return this.backend;
	}

	@Override
	public final ID getId() {
		return backend().getId();
	}

	public final boolean isConstant() {
		return getConstant() != null;
	}

	public final T getConstant() {
		return this.constant;
	}

	public final U create(OpBE<U> backend) {
		return create(backend, null);
	}

	public abstract U create(OpBE<U> backend, T constant);

	public final void useBy(UserInfo user) {
		explicitUses().useBy(user, SIMPLE_USAGE);
	}

	@Override
	public final User<SimpleUsage> toUser() {
		return allUses().toUser();
	}

	@Override
	public String toString() {
		if (this.backend == null) {
			return super.toString();
		}
		return this.backend.toString();
	}

	protected final Usable<SimpleUsage> allUses() {
		return this.allUses;
	}

	protected Usable<SimpleUsage> explicitUses() {
		return allUses();
	}

}
