/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import java.util.function.Function;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.field.FldOp;
import org.o42a.core.ir.field.local.LocalIROp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.member.MemberKey;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.string.ID;


public interface HostOp {

	ID HOST_ID = ID.rawId("host");

	default CompilerContext getContext() {
		return getBuilder().getContext();
	}

	default Generator getGenerator() {
		return getBuilder().getGenerator();
	}

	CodeBuilder getBuilder();

	OpPresets getPresets();

	HostOp setPresets(OpPresets presets);

	HostValueOp value();

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

	/**
	 * Creates a target store storing the target as local member.
	 *
	 * @param id target store identifier.
	 * @param getLocal a function obtaining local member by code directions.
	 *
	 * @return target store instance.
	 */
	TargetStoreOp localStore(ID id, Function<CodeDirs, LocalIROp> getLocal);

}
