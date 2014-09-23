/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.ir.op;

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.member.MemberKey;
import org.o42a.util.string.ID;


public interface HostTargetOp {

	TargetOp op(CodeDirs dirs);

	FldOp<?, ?> field(CodeDirs dirs, MemberKey memberKey);

	ObjectOp materialize(CodeDirs dirs, ObjHolder holder);

	ObjectOp dereference(CodeDirs dirs, ObjHolder holder);

	/**
	 * Allocates target store.
	 *
	 * <p>Note that this method is typically called in the allocator code,
	 * when target itself is not constructed yet. So this method should not rely
	 * on target. The {@link TargetStoreOp#storeTarget(CodeDirs)}, on the other
	 * hand, will be called when the target will be available.</p>
	 *
	 * @param id target store identifier.
	 * @param code target store allocation code.
	 *
	 * @return target store instance.
	 */
	TargetStoreOp allocateStore(ID id, Code code);

}
