/*
    Modules Commons
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
package org.o42a.common.def;

import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.Logical.runtimeLogical;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public class BuiltinValueDef extends ValueDef {

	private final Common common;

	public BuiltinValueDef(Builtin builtin) {
		super(
				builtin.toObject(),
				builtin,
				noScopeUpgrade(builtin.toObject().getScope()));
		this.common = new Common(builtin);
	}

	private BuiltinValueDef(
			BuiltinValueDef prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.common = prototype.common;
	}

	@Override
	public ValueStruct<?, ?> getValueStruct() {
		return this.common.builtin.toObject().value().getValueStruct();
	}

	@Override
	protected String name() {
		return "BuiltinValueDef";
	}

	@Override
	protected boolean hasConstantValue() {
		return this.common.builtin.isConstantBuiltin();
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.common.builtin.toObject().getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.common.builtin.toObject().getScope());
	}

	@Override
	protected Logical buildLogical() {
		return runtimeLogical(this, this.common.builtin.toObject().getScope());
	}

	@Override
	protected BuiltinValueDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new BuiltinValueDef(this, upgrade);
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.common.builtin.calculateBuiltin(resolver);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {

		final Obj object = resolver.getContainer().toObject();
		final Obj builtin = this.common.builtin.toObject();

		if (builtin != object) {
			builtin.value().resolveAll(resolver);
		}
		object.resolveAll();
		this.common.builtin.resolveBuiltin(
				object.value().valuePart(isClaim()).resolver());
	}

	@Override
	protected InlineValue inlineDef(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return this.common.builtin.inlineBuiltin(
				normalizer,
				valueStruct,
				getScope());
	}

	@Override
	protected void normalizeDef(Normalizer normalizer) {
		this.common.inline = inline(normalizer, getValueStruct());
	}

	@Override
	protected ValOp writeDef(ValDirs dirs, HostOp host) {

		final InlineValue inline = this.common.inline;

		if (inline != null) {
			return inline.writeValue(dirs, host);
		}

		return super.writeDef(dirs, host);
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		return this.common.builtin.writeBuiltin(dirs, host);
	}

	private static final class Common {

		private final Builtin builtin;
		private InlineValue inline;

		Common(Builtin builtin) {
			this.builtin = builtin;
		}

		@Override
		public String toString() {
			if (this.builtin == null) {
				return "null";
			}
			return this.builtin.toString();
		}

	}

}
