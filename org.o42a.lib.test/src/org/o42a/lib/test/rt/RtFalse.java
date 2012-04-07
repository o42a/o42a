/*
    Test Framework
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.codegen.code.Block;
import org.o42a.common.object.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.InlineValue;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.lib.test.TestModule;
import org.o42a.util.fn.Cancelable;


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
	public InlineValue inlineBuiltin(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {
		return new Inline(valueStruct);
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {

		final Block code = dirs.code();

		code.debug("Run-time false");
		code.go(dirs.falseDir());

		return falseValue().op(dirs.getBuilder(), code);
	}

	private static final class Inline extends InlineValue {

		Inline(ValueStruct<?, ?> valueStruct) {
			super(null, valueStruct);
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {

			final Block code = dirs.code();

			code.debug("Run-time false");
			code.go(dirs.falseDir());
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {

			final Block code = dirs.code();

			code.debug("Run-time false");
			code.go(dirs.falseDir());

			return falseValue().op(dirs.getBuilder(), code);
		}

		@Override
		public String toString() {
			return "RT-FALSE";
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}
