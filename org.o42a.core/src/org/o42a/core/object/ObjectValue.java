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
package org.o42a.core.object;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.object.def.Definitions.emptyDefinitions;
import static org.o42a.core.object.value.ValueUsage.*;
import static org.o42a.core.ref.RefUsage.TYPE_REF_USAGE;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.*;
import org.o42a.core.Scope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Sample;
import org.o42a.core.object.value.ObjectValueParts;
import org.o42a.core.object.value.ValueUsage;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.RootNormalizer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.FullResolution;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public final class ObjectValue extends ObjectValueParts {

	private static final byte FULLY_RESOLVED = 1;
	private static final byte NORMALIZED = 2;

	private ValueStruct<?, ?> valueStruct;
	private Value<?> value;
	private Definitions definitions;
	private Definitions explicitDefinitions;

	private Usable<ValueUsage> uses;

	private byte fullResolution;

	ObjectValue(Obj object) {
		super(object);
	}

	public final ValueType<?> getValueType() {
		return getValueStruct().getValueType();
	}

	public final ValueStruct<?, ?> getValueStruct() {
		if (this.valueStruct != null) {
			return this.valueStruct;
		}

		setValueStruct(getObject().determineValueStruct());

		return this.valueStruct;
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
		return !getValueType().isStateful()
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

		final Definitions definitions = object.overrideDefinitions(
				object.getScope(),
				getOverriddenDefinitions())
				.upgradeValueStruct(getValueStruct());

		if (!object.getConstructionMode().isRuntime()) {
			return this.definitions = definitions;
		}

		return this.definitions = definitions.runtime();
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

			if (getValueType().isVoid()) {
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

		return overriddenDefinitions(
				object.getScope(),
				ancestorDefinitions,
				ancestorDefinitions);
	}

	public final Definitions overriddenDefinitions(
			Scope scope,
			Definitions overriddenDefinitions,
			Definitions ancestorDefinitions) {
		if (ancestorDefinitions != null) {
			ancestorDefinitions.assertScopeIs(scope);
		}

		final Obj object = getObject();
		Definitions definitions = overriddenDefinitions;

		if (overriddenDefinitions != null) {
			overriddenDefinitions.assertScopeIs(scope);
			definitions = overriddenDefinitions;
		} else {
			definitions = emptyDefinitions(object, object.getScope());
		}

		final ObjectType type;

		if (scope.is(object.getScope())) {
			type = object.type().useBy(dummyUser());
		} else {
			type = object.type().useBy(scope.toObject().value().uses());
		}

		boolean hasExplicitAncestor =
				type.getAscendants().getExplicitAncestor() != null;
		final Sample[] samples = object.type().getSamples();

		for (int i = samples.length - 1; i >= 0; --i) {

			final Sample sample = samples[i];

			if (sample.isExplicit()) {
				if (hasExplicitAncestor) {
					definitions = definitions.override(ancestorDefinitions);
					hasExplicitAncestor = false;
				}
			}

			definitions = sample.overrideDefinitions(
					scope,
					definitions,
					ancestorDefinitions);
		}

		if (hasExplicitAncestor) {
			definitions = definitions.override(ancestorDefinitions);
		}

		return definitions;
	}

	public final ObjectValue explicitUseBy(UserInfo user) {
		if (!user.toUser().isDummy()) {
			uses().useBy(
					user,
					isRuntimeConstructed()
					? EXPLICIT_RUNTIME_VALUE_USAGE
					: EXPLICIT_STATIC_VALUE_USAGE);
		}
		return this;
	}

	public final void wrapBy(ObjectValue wrapValue) {
		claim().wrapBy(wrapValue.claim());
		proposition().wrapBy(wrapValue.proposition());
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
			getValueStruct().resolveAll(
					object.getScope()
					.resolver()
					.fullResolver(uses(), TYPE_REF_USAGE));
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

	public final User<?> rtUses() {
		return uses().selectiveUser(ANY_RUNTIME_VALUE_USAGE);
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
					getObject().type().rtDerivation(),
					RUNTIME_VALUE_USAGE);
		}
		getObject().content().useBy(this.uses);

		return this.uses;
	}

	final void setValueStruct(ValueStruct<?, ?> valueStruct) {
		if (valueStruct != null) {
			this.valueStruct = valueStruct;
			if (valueStruct.isScoped()) {
				valueStruct.toScoped().assertSameScope(getObject());
			}
		}
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
