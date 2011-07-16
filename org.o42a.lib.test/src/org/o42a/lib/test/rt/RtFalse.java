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
import org.o42a.common.object.CompiledBuiltin;
import org.o42a.common.source.EmptyURLSource;
import org.o42a.common.source.URLSourceTree;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.lib.test.TestModule;


public class RtFalse extends CompiledBuiltin {

	public static final URLSourceTree RT_FALSE =
			new EmptyURLSource(TestModule.TEST, "rt-false");

	public RtFalse(TestModule owner) {
		super(compileField(owner, RT_FALSE));
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return value().getValueType().runtimeValue();
	}

	@Override
	public void resolveBuiltin(Obj object) {
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final Code code = dirs.code();

		code.debug("Run-time false");
		code.go(dirs.falseDir());

		return falseValue().op(dirs.getBuilder(), code);
	}

}
