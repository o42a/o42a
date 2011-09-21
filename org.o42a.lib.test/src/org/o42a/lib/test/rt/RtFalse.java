/*
    Test Framework
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
package org.o42a.lib.test.rt;

import static org.o42a.core.value.Value.falseValue;

import org.o42a.codegen.code.Code;
import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.lib.test.TestModule;


@SourcePath(relativeTo = TestModule.class, value = "rt-false.o42a")
public class RtFalse extends AnnotatedBuiltin {

	public RtFalse(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return value().getValueStruct().runtimeValue();
	}

	@Override
	public void resolveBuiltin(Resolver resolver) {
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final Code code = dirs.code();

		code.debug("Run-time false");
		code.go(dirs.falseDir());

		return falseValue().op(dirs.getBuilder(), code);
	}

}
