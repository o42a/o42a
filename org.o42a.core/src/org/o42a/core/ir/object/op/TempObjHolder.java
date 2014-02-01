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

import org.o42a.codegen.code.Allocator;
import org.o42a.codegen.code.Block;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.util.string.ID;


final class TempObjHolder extends ObjHolder {

	private static final ID USE_ID = ID.id("use");

	private final Allocator allocator;
	private ObjectUseOp use;

	TempObjHolder(Allocator allocator) {
		this.allocator = allocator;
	}

	@Override
	public String toString() {
		if (this.allocator == null) {
			return super.toString();
		}
		return "TempObjHolder[" + this.allocator + ']';
	}

	@Override
	protected void setObject(Block code, ObjectOp object) {
		use(object).setUsed(code, object);
	}

	@Override
	protected void holdObject(Block code, ObjectOp object) {
	}

	@Override
	protected void holdVolatileObject(Block code, ObjectOp object) {
		use(object).startUse(code, object);
	}

	private ObjectUseOp use(ObjectOp object) {
		if (this.use != null) {
			return this.use;
		}
		return this.use = new ObjectUseOp(
				USE_ID.detail(object.getId()),
				object.getBuilder(),
				this.allocator);
	}

}
