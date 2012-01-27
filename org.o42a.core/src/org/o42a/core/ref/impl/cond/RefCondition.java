/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.ref.impl.cond;

import static org.o42a.core.value.Value.voidValue;

import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.ref.InlineValue;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.*;
import org.o42a.core.value.ValueStruct;


public final class RefCondition extends Statement {

	private final Ref ref;
	private StatementEnv conditionalEnv;

	public RefCondition(Ref ref) {
		super(ref, ref.distribute());
		this.ref = ref;
	}

	public final Ref getRef() {
		return this.ref;
	}

	@Override
	public Definer define(StatementEnv env) {
		return new RefConditionDefiner(this, env);
	}

	@Override
	public Statement reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref ref = this.ref.reproduce(reproducer);

		if (ref == null) {
			return null;
		}

		return new RefCondition(ref);
	}

	@Override
	public InlineValue inlineImperative(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {

		final InlineValue value = this.ref.inline(normalizer, getScope());

		if (value == null) {
			return null;
		}

		return new Inline(valueStruct, value);
	}

	@Override
	public void normalizeImperative(Normalizer normalizer) {
		this.ref.normalizeImperative(normalizer);
	}

	@Override
	public String toString() {
		return this.ref.toString();
	}

	@Override
	protected void fullyResolveImperative(LocalResolver resolver) {
		this.ref.resolve(resolver).resolveLogical();
	}

	@Override
	protected StOp createOp(CodeBuilder builder) {
		return new Op(builder, this.ref);
	}

	final StatementEnv getConditionalEnv() {
		return this.conditionalEnv;
	}

	private static final class Inline extends InlineValue {

		private final InlineValue value;

		Inline(ValueStruct<?, ?> valueStruct, InlineValue value) {
			super(valueStruct);
			this.value = value;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			this.value.writeCond(dirs, host);
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			writeCond(dirs.dirs(), host);
			return voidValue().op(dirs.getBuilder(), dirs.code());
		}

		@Override
		public void cancel() {
			this.value.cancel();
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return "(++" + this.value + ")";
		}

	}

	private static final class Op extends StOp {

		Op(CodeBuilder builder, Statement statement) {
			super(builder, statement);
		}

		@Override
		public void writeValue(Control control, ValOp result) {
			writeLogicalValue(control);
		}

		@Override
		public void writeLogicalValue(Control control) {
			getStatement().op(getBuilder()).writeLogicalValue(control);
		}

	}

}
