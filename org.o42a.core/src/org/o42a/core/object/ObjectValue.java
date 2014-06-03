/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.object;

import static org.o42a.core.object.def.Definitions.emptyDefinitions;
import static org.o42a.core.object.value.ValueUsage.EXPLICIT_VALUE_USAGE;
import static org.o42a.core.object.value.ValueUsage.VALUE_USAGE;
import static org.o42a.core.ref.RefUsage.TYPE_PARAMETER_REF_USAGE;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.*;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Sample;
import org.o42a.core.object.value.ObjectValueBase;
import org.o42a.core.object.value.Statefulness;
import org.o42a.core.object.value.ValueUsage;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.FullResolution;
import org.o42a.core.value.Value;


public final class ObjectValue extends ObjectValueBase {

	private static final byte FULLY_RESOLVED = 1;
	private static final byte NORMALIZED = 2;

	private Statefulness statefulness;
	private Value<?> value;
	private Definitions definitions;
	private Definitions explicitDefinitions;

	private Usable<ValueUsage> uses;

	private byte fullResolution;

	ObjectValue(Obj object) {
		super(object);
	}

	public final Statefulness getStatefulness() {
		if (this.statefulness != null) {
			return this.statefulness;
		}
		return this.statefulness = getObject().determineStatefulness();
	}

	public final UseFlag selectUse(
			Analyzer analyzer,
			UseSelector<ValueUsage> selector) {
		if (this.uses == null) {
			return analyzer.toUseCase().unusedFlag();
		}
		return this.uses.selectUse(analyzer, selector);
	}

	public final boolean isRuntimeConstructed() {
		return getObject().value().getStatefulness().isStateless()
				|| getObject().type().isRuntimeConstructed();
	}

	public final boolean isUsed(
			Analyzer analyzer,
			UseSelector<ValueUsage> selector) {
		return selectUse(analyzer, selector).isUsed();
	}

	public final Value<?> getValue() {
		if (this.value == null) {
			this.value =
					getDefinitions().value(getObject().getScope().resolver());
		}
		return this.value;
	}

	public Value<?> value(Resolver resolver) {
		getObject().assertCompatible(resolver.getScope());

		final Value<?> result;

		if (resolver.getScope().is(getObject().getScope())) {
			result = getValue();
		} else {
			result = getDefinitions().value(resolver);
		}

		return result;
	}

	public final Definitions getDefinitions() {
		if (this.definitions != null) {
			return this.definitions;
		}

		final Obj object = getObject();

		object.resolve();

		final Definitions overriddenDefinitions = getOverriddenDefinitions();
		final Definitions definitions =
				object.overrideDefinitions(overriddenDefinitions)
				.upgradeTypeParameters(getObject().type().getParameters());

		if (getObject().type().getValueType().isVoid()) {
			return this.definitions = definitions.addVoid();
		}

		return this.definitions = definitions;
	}

	public final Definitions getExplicitDefinitions() {
		if (this.explicitDefinitions != null) {
			return this.explicitDefinitions;
		}

		final Obj object = getObject();
		final Obj wrapped = object.getWrapped();

		if (wrapped != object) {

			final Definitions wrappedDefinitions =
					wrapped.value().getDefinitions();
			final Definitions explicitDefinitions;

			if (getObject().type().getValueType().isVoid()) {
				explicitDefinitions = wrappedDefinitions.toVoid();
			} else {
				explicitDefinitions = wrappedDefinitions;
			}

			return this.explicitDefinitions =
					explicitDefinitions.wrapBy(object.getScope());
		}

		this.explicitDefinitions = object.explicitDefinitions();

		if (this.explicitDefinitions != null) {
			return this.explicitDefinitions;
		}

		return this.explicitDefinitions =
				emptyDefinitions(object, object.getScope());
	}

	public final Definitions getOverriddenDefinitions() {

		final Obj object = getObject();
		final Definitions ancestorDefinitions = getAncestorDefinitions();
		final Definitions overriddenDefinitions;

		if (ancestorDefinitions != null) {
			ancestorDefinitions.assertScopeIs(object.getScope());
			overriddenDefinitions = ancestorDefinitions;
		} else {
			overriddenDefinitions = emptyDefinitions(object, object.getScope());
		}

		return overriddenDefinitions(overriddenDefinitions);
	}

	public final Definitions overriddenDefinitions(
			Definitions overriddenDefinitions) {

		Definitions definitions = overriddenDefinitions;
		final Obj object = getObject();
		final Sample sample = object.type().getSample();

		if (sample != null) {
			definitions = sample.overrideDefinitions(definitions);
		}

		return definitions;
	}

	public final ObjectValue explicitUseBy(UserInfo user) {
		if (!user.isDummyUser()) {
			uses().useBy(user, EXPLICIT_VALUE_USAGE);
		}
		return this;
	}

	public final void wrapBy(ObjectValue wrapValue) {
		valueDefs().wrapBy(wrapValue.valueDefs());
	}

	public final void resolveAll(UserInfo user) {
		if (this.fullResolution != 0) {
			explicitUseBy(user);
			return;
		}

		final Obj object = getObject();
		final FullResolution fullResolution =
				object.getContext().fullResolution();

		this.fullResolution = FULLY_RESOLVED;
		fullResolution.start();
		try {
			object.resolveAll();
			explicitUseBy(user);
			object.fullyResolveDefinitions();
			object.type().getParameters().resolveAll(
					object.getScope()
					.resolver()
					.fullResolver(uses(), TYPE_PARAMETER_REF_USAGE));
			// Use an ancestor value, as it is involved
			// into this object's value evaluation.
			useAncestorValue();
		} finally {
			fullResolution.end();
		}
	}

	public final void normalize(Analyzer analyzer) {
		if (this.fullResolution >= NORMALIZED) {
			return;
		}
		this.fullResolution = NORMALIZED;

		final RootNormalizer normalizer =
				new RootNormalizer(analyzer, getObject().getScope());

		getDefinitions().normalize(normalizer);
	}

	public final User<ValueUsage> toUser() {
		return uses().toUser();
	}

	@Override
	protected final Usable<ValueUsage> uses() {
		if (this.uses != null) {
			return this.uses;
		}

		final Obj cloneOf = getObject().getCloneOf();

		if (cloneOf != null) {
			this.uses = cloneOf.value().uses();
		} else {
			this.uses = ValueUsage.usable(this);
			// Always construct the value functions
			// of run-time constructed objects.
			this.uses.useBy(
					getObject().type().derivation(),
					VALUE_USAGE);
			this.uses.useBy(
					this.uses.usageUser(EXPLICIT_VALUE_USAGE),
					EXPLICIT_VALUE_USAGE);
			this.uses.useBy(
					this.uses.usageUser(VALUE_USAGE),
					VALUE_USAGE);
		}
		getObject().content().useBy(this.uses);

		return this.uses;
	}

	private void useAncestorValue() {

		final ObjectType objectType = getObject().type();
		final TypeRef ancestorRef = objectType.getAncestor();

		if (ancestorRef == null) {
			return;
		}

		final Obj ancestor = ancestorRef.getType();

		if (ancestor == null) {
			return;
		}
		if (!getDefinitions().hasInherited()) {
			return;
		}

		ancestor.value().uses().useBy(uses(), VALUE_USAGE);
	}

	private Definitions getAncestorDefinitions() {

		final Definitions ancestorDefinitions;
		final TypeRef ancestor = getObject().type().getAncestor();

		if (ancestor == null) {
			ancestorDefinitions = null;
		} else {
			ancestorDefinitions =
					ancestor.getType().value()
					.getDefinitions().upgradeScope(getObject().getScope());
		}

		return ancestorDefinitions;
	}

}
