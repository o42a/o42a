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
package org.o42a.core.def;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


final class CondValueDef extends ValueDef {

	private final CondDef def;

	CondValueDef(CondDef def) {
		super(def.getSource(), def.getLocation(), def.getScopeUpgrade());
		this.def = def;
		update(
				def.isRequirement() ? DefKind.CLAIM : DefKind.PROPOSITION,
				def.hasPrerequisite());
	}

	private CondValueDef(CondValueDef prototype, ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.def = prototype.def;
	}

	@Override
	public final ValueStruct<?, ?> getValueStruct() {
		return ValueStruct.VOID;
	}

	@Override
	protected boolean hasConstantValue() {
		return this.def.isConstant();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.def.getLogical().logicalValue(resolver).toValue(
				getValueStruct());
	}

	@Override
	protected ValueDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new CondValueDef(this, upgrade);
	}

	@Override
	protected Logical buildPrerequisite() {
		return this.def.getPrerequisite();
	}

	@Override
	protected Logical buildPrecondition() {
		return this.def.getPrecondition();
	}

	@Override
	protected Logical buildLogical() {
		return this.def.buildLogical();
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.def.fullyResolve(resolver);
	}

	@Override
	protected InlineValue inlineDef(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {

		final InlineCond inline = this.def.inline(normalizer);

		if (inline == null) {
			return null;
		}

		return new Inline(valueStruct, inline);
	}

	@Override
	protected void normalizeDef(Normalizer normalizer) {
		this.def.normalize(normalizer);
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		this.def.getLogical().write(dirs.dirs(), host);
		return dirs.value().storeVoid(dirs.code());
	}

	private static final class Inline extends InlineValue {

		private final InlineCond inline;

		public Inline(ValueStruct<?, ?> valueStruct, InlineCond inline) {
			super(valueStruct);
			this.inline = inline;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			this.inline.writeCond(dirs, host);
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			writeCond(dirs.dirs(), host);
			return dirs.value().storeVoid(dirs.code());
		}

		@Override
		public void cancel() {
			this.inline.cancel();
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
