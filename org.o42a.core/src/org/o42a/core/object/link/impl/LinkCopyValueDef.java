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
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.ValueDef;
import org.o42a.core.object.link.*;
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

		final Obj linkObject = linkResolution.toObject();
		final Value<?> value =
				linkObject.value().explicitUseBy(resolver).getValue();
		final LinkValueStruct sourceStruct =
				value.getValueStruct().toLinkStruct();
		final LinkValueStruct resultStruct =
				sourceStruct.setValueType(toLinkType);

		if (value.getKnowledge().isFalse()) {
			return resultStruct.falseValue();
		}
		if (!value.getKnowledge().isKnownToCompiler()) {
			return resultStruct.runtimeValue();
		}
		if (sourceStruct.getValueType().isRuntimeConstructed()) {
			// Run time constructed link can not be copied at compile time.
			return resultStruct.runtimeValue();
		}

		final PrefixPath prefix =
				ref.getPath().toPrefix(resolver.getScope());
		final KnownLink sourceLink =
				sourceStruct.cast(value).getCompilerValue();
		final TargetRef targetRef =
				sourceLink.getTargetRef().prefixWith(prefix);

		return resultStruct.compilerValue(
				new LinkCopy(sourceLink, toLinkType, targetRef));
	}

	private final Ref ref;
	private final LinkValueType toLinkType;
	private LinkValueStruct toStruct;
	private LinkValueStruct fromStruct;

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
	public Ref target() {
		if (hasPrerequisite() && !getPrerequisite().isTrue()) {
			return null;
		}
		if (!getPrecondition().isTrue()) {
			return null;
		}
		return this.ref.getPath().dereference().target(this.ref.distribute());
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
	protected void resolveTarget(TargetResolver resolver) {

		final Obj object = this.ref.getResolution().toObject();

		object.value().getDefinitions().resolveTargets(resolver);
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
		if (getValueStruct().assignableFrom(fromValueStruct())) {
			return this.ref.op(host).writeValue(dirs);
		}

		final ValDirs fromDirs = dirs.dirs().value(fromValueStruct());
		final ValOp from = this.ref.op(host).writeValue(fromDirs);
		final ValOp result = dirs.value().store(dirs.code(), from);

		fromDirs.done();

		return result;
	}

	private final LinkValueStruct fromValueStruct() {
		if (this.fromStruct != null) {
			return this.fromStruct;
		}

		final Scope scope = getScopeUpgrade().rescope(getScope());

		return this.fromStruct = this.ref.valueStruct(scope).prefixWith(
				getScopeUpgrade().toPrefix()).toLinkStruct();
	}

	private static final class LinkCopy extends KnownLink {

		private final LinkValueType toLinkType;
		private final Link copyOf;

		LinkCopy(
				Link copyOf,
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
		protected Link findLinkIn(Scope enclosing) {
			return this.copyOf.findIn(enclosing);
		}

		@Override
		protected KnownLink prefixWith(PrefixPath prefix) {
			return new LinkCopy(
					this,
					getTargetRef().prefixWith(prefix));
		}

	}

}
