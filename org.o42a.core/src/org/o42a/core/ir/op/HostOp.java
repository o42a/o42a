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
package org.o42a.core.ir.op;

import org.o42a.codegen.Generator;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.object.op.ObjHolder;
import org.o42a.core.member.MemberKey;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.string.ID;


public interface HostOp {

	ID HOST_ID = ID.id("host");

	Generator getGenerator();

	CodeBuilder getBuilder();

	CompilerContext getContext();

	HostValueOp value();

	HostTargetOp target();

	TargetOp field(CodeDirs dirs, MemberKey memberKey);

	ObjectOp materialize(CodeDirs dirs, ObjHolder holder);

	TargetOp dereference(CodeDirs dirs, ObjHolder holder);

}
