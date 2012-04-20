/*
    Compiler Core
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
package org.o42a.core.ref;

import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.*;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.impl.RefValueDef;
import org.o42a.core.st.*;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.fn.Cancelable;


public final class RefDefiner extends Definer {

	private ValueAdapter valueAdapter;
	private InlineValue inline;

	RefDefiner(Ref ref, DefinerEnv env) {
		super(ref, env);
	}

	public final Ref getRef() {
		return (Ref) getStatement();
	}

	@Override
	public DefTargets getDefTargets() {
		if (!getRef().isConstant()) {
			return valueDef();
		}
		return valueDef().setConstant();
	}

	public ValueAdapter getValueAdapter() {
		if (this.valueAdapter != null) {
			return this.valueAdapter;
		}

		final ValueStruct<?, ?> expectedStruct = env().getExpectedValueStruct();

		return this.valueAdapter = getRef().valueAdapter(expectedStruct, true);
	}

	public Definitions createDefinitions() {

		final RefValueDef def = new RefValueDef(getRef());

		return def.toDefinitions(env().getExpectedValueStruct());
	}

	@Override
	public DefValue value(Resolver resolver) {
		return getValueAdapter().value(resolver).toDefValue();
	}

	@Override
	public final Instruction toInstruction(Resolver resolver) {
		return null;
	}

	@Override
	public InlineEval inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct,
			Scope origin) {

		final InlineValue inline = getRef().inline(normalizer, origin);

		if (inline == null) {
			return null;
		}

		return new Inline(inline);
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
		this.inline = getRef().inline(normalizer.newNormalizer(), getScope());
		if (this.inline == null) {
			getRef().normalize(normalizer.getAnalyzer());
		}
	}

	@Override
	public final RefEval eval(CodeBuilder builder) {
		return (RefEval) super.eval(builder);
	}

	@Override
	public String toString() {
		return '=' + super.toString();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		getRef().resolve(resolver).resolveValue();
	}

	@Override
	protected Eval createEval(CodeBuilder builder) {
		if (this.inline == null) {
			return new RefEvalImpl(builder, getRef());
		}
		return new InlineRefEvalImpl(builder, getRef(), this.inline);
	}

	private static final class Inline extends InlineEval {

		private final InlineValue value;

		Inline(InlineValue value) {
			super(null);
			this.value = value;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final DefDirs defDirs = dirs.falseWhenUnknown();
			final ValOp value = this.value.writeValue(defDirs.valDirs(), host);

			defDirs.done();
			dirs.returnValue(value);
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return this.value.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class RefEvalImpl extends RefEval {

		RefEvalImpl(CodeBuilder builder, Ref ref) {
			super(builder, ref);
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {

			final CodeDirs condDirs = dirs.falseWhenUnknown();

			getRef().op(host).writeCond(condDirs);
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final DefDirs defDirs = dirs.falseWhenUnknown();
			final ValOp value = getRef().op(host).writeValue(defDirs.valDirs());

			defDirs.done();
			dirs.returnValue(value);
		}

	}

	private static final class InlineRefEvalImpl extends RefEval {

		private final InlineValue inline;

		InlineRefEvalImpl(CodeBuilder builder, Ref ref, InlineValue inline) {
			super(builder, ref);
			this.inline = inline;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {

			final CodeDirs condDirs = dirs.falseWhenUnknown();

			this.inline.writeCond(condDirs, host);
			condDirs.end();
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final DefDirs defDirs = dirs.falseWhenUnknown();
			final ValOp value = this.inline.writeValue(defDirs.valDirs(), host);

			defDirs.done();
			dirs.returnValue(value);
		}

		@Override
		public String toString() {
			if (this.inline == null) {
				return super.toString();
			}
			return this.inline.toString();
		}

	}

}

