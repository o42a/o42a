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

import org.o42a.backend.constant.code.CCode;
import org.o42a.codegen.code.op.Op;
import org.o42a.util.string.ID;


public final class AliasBE<U extends Op> extends OpBE<U> {

	private final OpBE<U> aliased;

	public AliasBE(ID id, CCode<?> code, OpBE<U> aliased) {
		super(id != null ? id : aliased.getId(), code);
		this.aliased = aliased;
	}

	@Override
	public void prepare() {
		use(this.aliased).prepare();
	}

	@Override
	protected U write() {
		return part().underlying().phi(getId(), this.aliased.underlying());
	}

}
