/*
    Compiler Core
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
package org.o42a.core.ir.object.op;

import org.o42a.codegen.code.Block;
import org.o42a.core.ir.object.ObjectOp;


final class VolatileObjHolder extends ObjHolder {

	private final ObjHolder holder;

	VolatileObjHolder(ObjHolder holder) {
		this.holder = holder;
	}

	@Override
	public ObjHolder toVolatile() {
		return this;
	}

	@Override
	public String toString() {
		if (this.holder == null) {
			return super.toString();
		}
		return "Volatile" + this.holder;
	}

	@Override
	protected void setObject(Block code, ObjectOp object) {
		this.holder.set(code, object);
	}

	@Override
	protected void holdObject(Block code, ObjectOp object) {
		this.holder.holdVolatile(code, object);
	}

	@Override
	protected void holdVolatileObject(Block code, ObjectOp object) {
		this.holder.holdVolatile(code, object);
	}

}
