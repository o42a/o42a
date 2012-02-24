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
import static org.o42a.core.value.Value.falseValue;

import org.o42a.core.Scope;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.ValueDef;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.object.link.LinkValueType;
import org.o42a.core.object.link.ObjectLink;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


final class LinkCopyValueDef extends ValueDef {

	static Value<?> linkValue(
			Ref ref,
			Resolver resolver,
			LinkValueType toLinkType) {

		final Resolution linkResolution = ref.resolve(resolver);

		if (linkResolution.isError()) {
			return falseValue();
		}

		final Obj linkObject = linkResolution.materialize();
		final Value<?> value =
				linkObject.value().explicitUseBy(resolver).getValue();
		final LinkValueStruct sourceStruct =
				(LinkValueStruct) value.getValueStruct();
		final LinkValueStruct resultStruct =
				sourceStruct.setValueType(toLinkType);

		if (value.getKnowledge().isFalse()) {
			return resultStruct.falseValue();
		}
		if (!value.getKnowledge().isKnownToCompiler()) {
			return resultStruct.runtimeValue();
		}
		if (sourceStruct.getValueType().isVariable()) {
			// Variable can not be copied at compile time.
			return resultStruct.runtimeValue();
		}

		final PrefixPath prefix =
				ref.getPath().toPrefix(resolver.getScope());
		final ObjectLink sourceLink =
				sourceStruct.cast(value).getCompilerValue();
		final TargetRef targetRef =
				sourceLink.getTargetRef().prefixWith(prefix);

		return sourceStruct.compilerValue(
				new LinkCopy(sourceLink, toLinkType, targetRef));
	}

	private final Ref ref;
	private final LinkValueType toLinkType;
	private LinkValueStruct toStruct;

	LinkCopyValueDef(Ref ref, LinkValueType toLinkType) {
		super(sourceOf(ref), ref, ScopeUpgrade.noScopeUpgrade(ref.getScope()));
		this.ref = ref;
		this.toLinkType = toLinkType;
	}

	private LinkCopyValueDef(
			LinkCopyValueDef prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.ref = prototype.ref;
		this.toLinkType = prototype.toLinkType;
	}

	@Override
	public LinkValueStruct getValueStruct() {
		if (this.toStruct != null) {
			return this.toStruct;
		}
		return this.toStruct = fromValueStruct().setValueType(this.toLinkType);
	}

	@Override
	public void normalize(Normalizer normalizer) {
	}

	@Override
	protected boolean hasConstantValue() {
		return false;
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
		return linkValue(this.ref, resolver, this.toLinkType);
	}

	@Override
	protected ValueDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new LinkCopyValueDef(this, upgrade);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.ref.resolve(resolver).resolveValue();
	}

	@Override
	protected InlineValue inlineDef(
			Normalizer normalizer,
			ValueStruct<?, ?> valueStruct) {
		return null;
	}

	@Override
	protected ValOp writeValue(ValDirs dirs, HostOp host) {
		return this.ref.op(host).writeValue(dirs);
	}

	private final LinkValueStruct fromValueStruct() {

		final Scope scope = getScopeUpgrade().rescope(getScope());

		return (LinkValueStruct) this.ref.valueStruct(scope).prefixWith(
				getScopeUpgrade().toPrefix());
	}

	private static final class LinkCopy extends ObjectLink {

		private final LinkValueType toLinkType;
		private final ObjectLink copyOf;

		LinkCopy(
				ObjectLink copyOf,
				LinkValueType toLinkType,
				TargetRef targetRef) {
			super(
					copyOf,
					copyOf.distributeIn(targetRef.getScope().getContainer()),
					targetRef);
			this.toLinkType = toLinkType;
			this.copyOf = copyOf;
		}

		LinkCopy(LinkCopy prototype, TargetRef targetRef) {
			super(prototype, targetRef);
			this.toLinkType = prototype.toLinkType;
			this.copyOf = prototype.copyOf;
		}

		@Override
		public LinkValueType getValueType() {
			return this.toLinkType;
		}

		@Override
		protected TargetRef buildTargetRef() {
			throw new UnsupportedOperationException();
		}

		@Override
		protected ObjectLink findLinkIn(Scope enclosing) {
			return this.copyOf.findIn(enclosing);
		}

		@Override
		protected ObjectLink prefixWith(PrefixPath prefix) {
			return new LinkCopy(
					this,
					getTargetRef().prefixWith(prefix));
		}

	}

}
