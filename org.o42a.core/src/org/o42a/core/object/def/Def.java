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
package org.o42a.core.object.def;

import static org.o42a.core.object.def.Definitions.NO_CLAIMS;
import static org.o42a.core.object.def.Definitions.NO_PROPOSITIONS;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;

import org.o42a.core.*;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.TargetResolver;
import org.o42a.core.ref.*;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.util.log.Loggable;


public abstract class Def implements SourceInfo {

	public static final Obj sourceOf(ScopeInfo scope) {
		return sourceOf(scope.getScope().getContainer());
	}

	public static Obj sourceOf(Container container) {

		final Obj object = container.toObject();

		if (object != null) {
			return object;
		}

		final LocalScope local = container.toLocal();

		assert local != null :
			"Definition can be created only inside object or local scope";

		return local.getOwner();
	}

	private final ScopeUpgrade scopeUpgrade;
	private final Obj source;
	private final LocationInfo location;
	private Value<?> constantValue;
	private boolean claim;
	private boolean allResolved;

	public Def(
			Obj source,
			LocationInfo location,
			ScopeUpgrade scopeUpgrade) {
		this(source, location, scopeUpgrade, false);
	}

	public Def(
			Obj source,
			LocationInfo location,
			ScopeUpgrade scopeUpgrade,
			boolean claim) {
		this.scopeUpgrade = scopeUpgrade;
		this.location = location;
		this.source = source;
		this.claim = claim;
	}

	protected Def(Def prototype, ScopeUpgrade scopeUpgrade) {
		this.scopeUpgrade = scopeUpgrade;
		this.source = prototype.source;
		this.location = prototype.location;
		this.claim = prototype.claim;
	}

	public final boolean isClaim() {
		return this.claim;
	}

	@Override
	public final Scope getScope() {
		return this.scopeUpgrade.getFinalScope();
	}

	public final ScopeUpgrade getScopeUpgrade() {
		return this.scopeUpgrade;
	}

	@Override
	public final Loggable getLoggable() {
		return this.location.getLoggable();
	}

	@Override
	public final CompilerContext getContext() {
		return this.location.getContext();
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	@Override
	public final Obj getSource() {
		return this.source;
	}

	public final ValueType<?> getValueType() {
		return getValueStruct().getValueType();
	}

	public abstract ValueStruct<?, ?> getValueStruct();

	public abstract boolean unconditional();

	public final boolean isExplicit() {
		return getSource() == sourceOf(getScope().toObject());
	}

	public final Def upgradeScope(Scope toScope) {
		if (toScope == getScope()) {
			return this;
		}
		return upgradeScope(ScopeUpgrade.upgradeScope(this, toScope));
	}

	public final Def claim() {
		if (isClaim()) {
			return this;
		}

		final Def copy = copy();

		copy.claim = true;

		return copy;
	}

	public final Def unclaim() {
		if (!isClaim()) {
			return this;
		}

		final Def copy = copy();

		copy.claim = false;

		return copy;
	}

	public final Value<?> getConstantValue() {
		if (this.constantValue != null) {
			return this.constantValue;
		}
		if (!hasConstantValue()) {
			return this.constantValue = getValueStruct().runtimeValue();
		}
		return this.constantValue = value(getScope().dummyResolver());
	}

	public Value<?> value(Resolver resolver) {
		assertCompatible(resolver.getScope());

		final Resolver rescoped = getScopeUpgrade().rescope(resolver);
		final Value<?> value = calculateValue(rescoped);

		if (value == null) {
			return getValueStruct().unknownValue();
		}

		return value.prefixWith(getScopeUpgrade().toPrefix());
	}

	public DefTarget target() {
		return DefTarget.NO_DEF_TARGET;
	}

	public final Def toVoid() {
		if (getValueType().isVoid()) {
			return this;
		}
		return new VoidDef(this);
	}

	public final Definitions toDefinitions(ValueStruct<?, ?> valueStruct) {
		assert valueStruct != null :
			"Value structure expected";

		if (isClaim()) {
			return new Definitions(
					this,
					getScope(),
					valueStruct,
					new Defs(true, this),
					NO_PROPOSITIONS);
		}

		return new Definitions(
				this,
				getScope(),
				valueStruct,
				NO_CLAIMS,
				new Defs(false, this));
	}

	public final void resolveAll(Resolver resolver) {
		this.allResolved = true;
		getContext().fullResolution().start();
		try {
			fullyResolve(getScopeUpgrade().rescope(resolver));
		} finally {
			getContext().fullResolution().end();
		}
	}

	public abstract InlineEval inline(Normalizer normalizer);

	public abstract void normalize(RootNormalizer normalizer);

	public abstract Eval eval();

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	public final boolean assertFullyResolved() {
		assert this.allResolved :
			this + " is not fully resolved";
		return true;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(getClass().getSimpleName()).append('[');
		out.append(getLocation());
		if (isClaim()) {
			out.append("!]");
		} else {
			out.append(".]");
		}

		return out.toString();
	}

	protected abstract Def create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade);

	protected abstract boolean hasConstantValue();

	protected abstract Value<?> calculateValue(Resolver resolver);

	protected void resolveTarget(TargetResolver resolver) {
	}

	protected abstract void fullyResolve(Resolver resolver);

	protected final LocationInfo getLocation() {
		return this.location;
	}

	final Def upgradeScope(ScopeUpgrade upgrade) {

		final ScopeUpgrade oldUpgrade = getScopeUpgrade();
		final ScopeUpgrade newUpgrade = oldUpgrade.and(upgrade);

		if (newUpgrade == oldUpgrade) {
			return this;
		}

		return create(newUpgrade, upgrade);
	}

	private final Def copy() {
		return create(getScopeUpgrade(), noScopeUpgrade(getScope()));
	}

}
