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
package org.o42a.lib.test.rt.parser;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.lib.test.TestModule;
import org.o42a.root.adapter.ByString;
import org.o42a.util.fn.Cancelable;


@SourcePath(relativeTo = TestModule.class, value = "parser/string.o42a")
public final class ParseString extends ByString<String> {

	public ParseString(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> value = super.calculateBuiltin(resolver);

		if (!value.getKnowledge().isKnown()) {
			return value;
		}

		return type().getParameters().runtimeValue();
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue inlineInput = input().inline(normalizer, origin);

		if (inlineInput == null) {
			return null;
		}

		return new InlineCopyString(this, inlineInput);
	}

	@Override
	public Eval evalBuiltin() {
		return new EvalCopyString(this);
	}

	@Override
	protected String byString(
			LocationInfo location,
			Resolver resolver,
			String input) {
		return input;
	}

	@Override
	protected ValOp parse(ValDirs dirs, ValOp value) {
		dirs.code().debug("Run-time string");
		return value;
	}

	private static final class InlineCopyString extends InlineEval {

		private final ParseString parseString;
		private final InlineValue inputValue;

		InlineCopyString(ParseString parseString, InlineValue inputValue) {
			super(null);
			this.parseString = parseString;
			this.inputValue = inputValue;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			dirs.code().debug("Run-time string");
			dirs.returnValue(this.inputValue.writeValue(dirs.valDirs(), host));
		}

		@Override
		public String toString() {
			if (this.parseString == null) {
				return super.toString();
			}
			return this.parseString.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class EvalCopyString implements Eval {

		private final ParseString byString;

		EvalCopyString(ParseString byString) {
			this.byString = byString;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			dirs.code().debug("Run-time string");
			dirs.returnValue(
					this.byString.input().op(host).writeValue(dirs.valDirs()));
		}

		@Override
		public String toString() {
			if (this.byString == null) {
				return super.toString();
			}
			return this.byString.toString();
		}

	}

}
