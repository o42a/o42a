/*
    Test Framework
    Copyright (C) 2011-2013 Ruslan Lopatin

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

import org.o42a.codegen.code.Code;
import org.o42a.common.builtin.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.Value;
import org.o42a.lib.test.TestModule;
import org.o42a.util.fn.Cancelable;


@SourcePath(relativeTo = TestModule.class, value = "rt-void.o42a")
public class RtVoid extends AnnotatedBuiltin {

	private static final RtVoidEval RT_VOID_EVAL = new RtVoidEval();

	public RtVoid(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return type().getParameters().runtimeValue();
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {
		return RT_VOID_EVAL;
	}

	@Override
	public Eval evalBuiltin() {
		return RT_VOID_EVAL;
	}

	private static final class RtVoidEval extends InlineEval {

		RtVoidEval() {
			super(null);
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final Code code = dirs.code();

			code.debug("Run-time void");

			dirs.returnValue(dirs.getBuilder().voidVal(dirs.code()));
		}

		@Override
		public String toString() {
			return "RT-VOID";
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}
