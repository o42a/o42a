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

import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.codegen.code.Block;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.def.ValueDef;
import org.o42a.core.object.link.*;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public class LinkByValueDef extends ValueDef {

	static Value<?> linkByValue(Ref ref, LinkValueStruct linkStruct) {
		return linkStruct.compilerValue(new TargetLink(
				ref.toTargetRef(linkStruct.getTypeRef()),
				ref.distribute(),
				linkStruct.getValueType()));
	}

	private final Ref ref;
	private final LinkValueStruct linkStruct;

	public LinkByValueDef(Ref ref, LinkValueStruct linkStruct) {
		super(sourceOf(ref), ref, ScopeUpgrade.noScopeUpgrade(ref.getScope()));
		this.ref = ref;
		this.linkStruct = linkStruct;
	}

	public LinkByValueDef(
			LinkByValueDef prototype,
			ScopeUpgrade scopeUpgrade,
			ScopeUpgrade additionalUpgrade) {
		super(prototype, scopeUpgrade);
		this.ref = prototype.ref;
		this.linkStruct = prototype.linkStruct;
	}

	@Override
	public final LinkValueStruct getValueStruct() {
		return this.linkStruct.prefixWith(getScopeUpgrade().toPrefix());
	}

	@Override
	public Ref target() {
		return this.ref;
	}

	@Override
	public void normalize(Normalizer normalizer) {
	}

	@Override
	protected boolean hasConstantValue() {
		return this.ref.isStatic();
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.ref.getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.ref.getScope());
	}

	@Override
	protected Logical buildLogical() {
		return this.ref.getLogical();
	}

	@Override
	protected Value<?> calculateValue(Resolver resolver) {
		return linkByValue(this.ref, this.linkStruct);
	}

	@Override
	protected ValueDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new LinkByValueDef(this, upgrade, additionalUpgrade);
	}

	@Override
	protected void resolveTarget(TargetResolver resolver) {
		resolver.resolveTarget(this.ref.getResolution().toObject());
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.ref.resolve(resolver).resolveTarget();
	}

	@Override
	protected InlineValue inlineDef(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return null;
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {

		final Block code = dirs.code();
		final ObjectOp target =
				this.ref.op(host).target(dirs.dirs()).materialize(dirs.dirs());

		return dirs.value().store(code, target.toAny(code));
	}

	private static final class TargetLink extends KnownLink {

		private final LinkValueType linkType;

		TargetLink(
				TargetRef targetRef,
				Distributor distributor,
				LinkValueType linkType) {
			super(targetRef, distributor, targetRef);
			this.linkType = linkType;
		}

		TargetLink(TargetLink prototype, TargetRef targetRef) {
			super(prototype, targetRef);
			this.linkType = prototype.linkType;
		}

		@Override
		public LinkValueType getValueType() {
			return this.linkType;
		}

		@Override
		protected TargetRef buildTargetRef() {
			throw new UnsupportedOperationException();
		}

		@Override
		protected ObjectLink findLinkIn(Scope enclosing) {

			final TargetRef targetRef =
					getTargetRef().upgradeScope(enclosing);

			return new TargetLink(this, targetRef);
		}

		@Override
		protected KnownLink prefixWith(PrefixPath prefix) {
			return new TargetLink(
					this,
					getTargetRef().prefixWith(prefix));
		}

	}

}
