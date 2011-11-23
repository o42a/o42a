/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.artifact.object;

import static org.o42a.core.def.DefKind.*;
import static org.o42a.core.def.Definitions.emptyDefinitions;
import static org.o42a.core.ref.impl.path.Wrapper.wrapperPrefix;
import static org.o42a.util.use.SimpleUsage.ALL_SIMPLE_USAGES;
import static org.o42a.util.use.SimpleUsage.SIMPLE_USAGE;
import static org.o42a.util.use.SimpleUsage.simpleUsable;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.def.DefKind;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.FullResolution;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.util.use.*;


public final class ObjectValue implements Uses<SimpleUsage> {

	private final Obj object;
	private ValueStruct<?, ?> valueStruct;
	private Value<?> value;
	private Definitions definitions;
	private Definitions explicitDefinitions;

	private final CondPart requirement;
	private final CondPart condition;
	private final ValuePart claim;
	private final ValuePart proposition;

	private Usable<SimpleUsage> uses;
	private Usable<SimpleUsage> rtUses;
	private Usable<SimpleUsage> explicitUses;
	private Usable<SimpleUsage> rtExplicitUses;

	private boolean fullyResolved;

	ObjectValue(Obj object) {
		this.object = object;
		this.requirement = new CondPart(this, REQUIREMENT);
		this.condition = new CondPart(this, CONDITION);
		this.claim = new ValuePart(this, CLAIM);
		this.proposition = new ValuePart(this, PROPOSITION);
	}

	public final Obj getObject() {
		return this.object;
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

	@Override
	public final AllUsages<SimpleUsage> allUsages() {
		return ALL_SIMPLE_USAGES;
	}

	@Override
	public final UseFlag getUseBy(
			UseCaseInfo useCase,
			UseSelector<SimpleUsage> selector) {
		if (this.uses == null) {
			return useCase.toUseCase().unusedFlag();
		}
		return this.uses.getUseBy(useCase, selector);
	}

	@Override
	public final boolean isUsedBy(
			UseCaseInfo useCase,
			UseSelector<SimpleUsage> selector) {
		return getUseBy(useCase, selector).isUsed();
	}

	public final Value<?> getValue() {
		if (this.value == null) {
			this.value = getDefinitions().value(
					getObject().getScope().dummyResolver());
		}
		return this.value;
	}

	public Value<?> value(Resolver resolver) {
		getObject().assertCompatible(resolver.getScope());
		explicitUseBy(resolver);

		final Value<?> result;

		if (resolver.getScope() == getObject().getScope()) {
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
				getOverriddenDefinitions());

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

			return this.explicitDefinitions =
					wrappedDefinitions.prefixWith(wrapperPrefix(
							object.getScope(),
							wrapped.getScope()));
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

		return object.overriddenDefinitions(
				object.getScope(),
				ancestorDefinitions,
				ancestorDefinitions);
	}

	public final CondPart requirement() {
		return this.requirement;
	}

	public final CondPart condition() {
		return this.condition;
	}

	public final ValuePart claim() {
		return this.claim;
	}

	public final ValuePart proposition() {
		return this.proposition;
	}

	public final CondPart condPart(boolean requirement) {
		return requirement ? requirement() : condition();
	}

	public final CondPart condPart(DefKind condKind) {
		assert !condKind.isValue() :
			"Condition definition kind expected: " + condKind;
		return condPart(condKind.isClaim());
	}

	public final ValuePart valuePart(boolean claim) {
		return claim ? claim() : proposition();
	}

	public final ValuePart valuePart(DefKind valueKind) {
		assert valueKind.isValue() :
			"Value definition kind expected: " + valueKind;
		return valuePart(valueKind.isClaim());
	}

	public final ObjectValuePart<?, ?> part(DefKind defKind) {
		switch (defKind) {
		case REQUIREMENT:
			return requirement();
		case CONDITION:
			return condition();
		case CLAIM:
			return claim();
		case PROPOSITION:
			return proposition();
		}

		throw new IllegalArgumentException(
				"Unsupported definition kind: " + defKind);
	}

	public final ObjectValue explicitUseBy(UserInfo user) {
		if (!user.toUser().isDummy()) {
			explicitUses().useBy(user, SIMPLE_USAGE);
		}
		return this;
	}

	public final void wrapBy(ObjectValue wrapValue) {
		for (DefKind defKind : DefKind.values()) {
			part(defKind).wrapBy(wrapValue.part(defKind));
		}
	}

	public final void resolveAll(UserInfo user) {
		if (this.fullyResolved) {
			explicitUseBy(user);
			return;
		}

		final Obj object = getObject();
		final FullResolution fullResolution =
				object.getContext().fullResolution();

		this.fullyResolved = true;
		fullResolution.start();
		try {
			object.resolveAll();
			explicitUseBy(user);
			object.fullyResolveDefinitions();
		} finally {
			fullResolution.end();
		}
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return "ObjectValue[" + this.object + ']';
	}

	final void setValueStruct(ValueStruct<?, ?> valueStruct) {
		if (valueStruct != null) {
			this.valueStruct = valueStruct;
			if (valueStruct.isScoped()) {
				valueStruct.toScoped().assertSameScope(getObject());
			}
		}
	}

	final Usable<SimpleUsage> uses() {
		if (this.uses != null) {
			return this.uses;
		}

		final Obj cloneOf = getObject().getCloneOf();

		if (cloneOf != null) {
			this.uses = cloneOf.value().rtUses();
		} else {
			this.uses = simpleUsable("ValueOf", this.object);
		}
		getObject().content().useBy(this.uses);

		return this.uses;
	}

	final Usable<SimpleUsage> explicitUses() {
		if (this.explicitUses != null) {
			return this.explicitUses;
		}

		final Obj cloneOf = getObject().getCloneOf();

		if (cloneOf != null) {
			this.explicitUses = cloneOf.value().rtExplicitUses();
			rtUses().useBy(this.explicitUses, SIMPLE_USAGE);
		} else {
			this.explicitUses = simpleUsable("ExplicitValueOf", this.object);
			uses().useBy(this.explicitUses, SIMPLE_USAGE);
		}

		return this.explicitUses;
	}

	private final Usable<SimpleUsage> rtUses() {
		if (this.rtUses != null) {
			return this.rtUses;
		}

		final Obj cloneOf = getObject().getCloneOf();

		if (cloneOf != null) {
			return this.rtUses = cloneOf.value().rtUses();
		}

		this.rtUses = simpleUsable("RtValuesOf", this.object);
		uses().useBy(this.rtUses, SIMPLE_USAGE);

		return this.rtUses;
	}

	private final Usable<SimpleUsage> rtExplicitUses() {
		if (this.rtExplicitUses != null) {
			return this.rtExplicitUses;
		}

		final Obj cloneOf = getObject().getCloneOf();

		if (cloneOf != null) {
			return this.rtExplicitUses = cloneOf.value().rtExplicitUses();
		}

		return this.rtExplicitUses =
				simpleUsable("RtExplicitValuesOf", this.object);
	}

	private Definitions getAncestorDefinitions() {

		final Definitions ancestorDefinitions;
		final TypeRef ancestor = getObject().type().getAncestor();

		if (ancestor == null) {
			ancestorDefinitions = null;
		} else {
			ancestorDefinitions =
					ancestor.typeObject(dummyUser()).value()
					.getDefinitions().upgradeScope(getObject().getScope());
		}

		return ancestorDefinitions;
	}

}
