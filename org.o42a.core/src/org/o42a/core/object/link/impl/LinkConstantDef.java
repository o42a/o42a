/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.link.impl;

import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.object.link.KnownLink;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public final class LinkConstantDef extends Def {

	private final Value<KnownLink> value;
	private LinkValueStruct valueStruct;

	public LinkConstantDef(
			Obj source,
			LocationInfo location,
			LinkValueStruct valueStruct,
			KnownLink value) {
		super(
				source,
				location,
				noScopeUpgrade(valueStruct.toScoped().getScope()));
		this.value = valueStruct.compilerValue(value);
	}

	private LinkConstantDef(
			LinkConstantDef prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.value = prototype.value;
	}

	@Override
	public LinkValueStruct getValueStruct() {
		if (this.valueStruct != null) {
			return this.valueStruct;
		}

		final LinkValueStruct valueStruct =
				(LinkValueStruct) this.value.getValueStruct();

		return this.valueStruct =
				valueStruct.prefixWith(getScopeUpgrade().toPrefix());
	}

	@Override
	public boolean unconditional() {
		return true;
	}

	public final KnownLink getLink() {
		return this.value.getCompilerValue();
	}

	@Override
	public DefTarget target() {
		return new DefTarget(
				this.value.getCompilerValue().getTargetRef().getRef());
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
	}

	@Override
	protected boolean hasConstantValue() {
		if (getValueType().isRuntimeConstructed()) {
			return false;
		}
		return getLink().getTargetRef().isStatic();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return this.value;
	}

	@Override
	protected LinkConstantDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new LinkConstantDef(this, upgrade);
	}

	@Override
	protected void resolveTarget(TargetResolver resolver) {
		resolver.resolveTarget(getLink().getTarget());
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		this.value.resolveAll(resolver);
	}

	@Override
	protected InlineValue inline(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return null;
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		if (hasConstantValue()) {
			return this.value.op(dirs.getBuilder(), dirs.code());
		}

		final ObjectOp target =
				getLink()
				.getTargetRef()
				.getRef()
				.op(host)
				.target(dirs.dirs())
				.materialize(dirs.dirs());

		return dirs.value().store(dirs.code(), target.toAny(dirs.code()));
	}

}
