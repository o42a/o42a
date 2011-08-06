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

import static org.o42a.core.def.Definitions.emptyDefinitions;
import static org.o42a.util.use.Usable.simpleUsable;
import static org.o42a.util.use.User.dummyUser;

import java.util.EnumMap;

import org.o42a.core.def.DefKind;
import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.FullResolution;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.use.*;


public final class ObjectValue implements UseInfo {

	private final Obj object;
	private ValueType<?> valueType;
	private Value<?> value;
	private Definitions definitions;
	private Definitions explicitDefinitions;

	private Usable usable;
	private Usable explicitUsable;
	private EnumMap<DefKind, ValuePart> parts;

	private boolean fullyResolved;

	ObjectValue(Obj object) {
		this.object = object;
	}

	public final Obj getObject() {
		return this.object;
	}

	public final ValueType<?> getValueType() {
		if (this.valueType != null) {
			return this.valueType;
		}

		final Obj object = getObject();
		final TypeRef ancestor = object.type().getAncestor();

		if (ancestor == null) {
			return ValueType.VOID;
		}

		setValueType(ancestor.typeObject(
				object.getScope().dummyResolver()).value().getValueType());

		return this.valueType;
	}

	@Override
	public final boolean isUsedBy(UseCaseInfo useCase) {
		return getUseBy(useCase).isUsed();
	}

	@Override
	public final UseFlag getUseBy(UseCaseInfo useCase) {
		if (this.usable == null) {
			return useCase.toUseCase().unusedFlag();
		}
		return this.usable.getUseBy(useCase);
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

		final Value<?> result;

		if (resolver == getObject().getScope()) {
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

	public final ValuePart requirement() {
		return part(DefKind.REQUIREMENT);
	}

	public final ValuePart condition() {
		return part(DefKind.CONDITION);
	}

	public final ValuePart claim() {
		return part(DefKind.CLAIM);
	}

	public final ValuePart proposition() {
		return part(DefKind.PROPOSITION);
	}

	public final ValuePart part(DefKind defKind) {
		assert defKind != null :
			"Definition kind not specified";
		if (this.parts == null) {
			this.parts = new EnumMap<DefKind, ValuePart>(DefKind.class);
		} else {

			final ValuePart part = this.parts.get(defKind);

			if (part != null) {
				return part;
			}
		}

		final ValuePart part = new ValuePart(this, defKind);

		this.parts.put(defKind, part);

		return part;
	}

	public final ObjectValue explicitUseBy(UserInfo user) {
		if (!user.toUser().isDummy()) {
			explicitUsable().useBy(user);
		}
		return this;
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

	final void setValueType(ValueType<?> valueType) {
		if (valueType != null) {
			this.valueType = valueType;
		}
	}

	final Usable usable() {
		if (this.usable != null) {
			return this.usable;
		}

		this.usable = simpleUsable(this);
		getObject().content().useBy(this.usable);

		return this.usable;
	}

	final Usable explicitUsable() {
		if (this.explicitUsable != null) {
			return this.explicitUsable;
		}

		this.explicitUsable = simpleUsable("ValueOf", this.object);

		return this.explicitUsable;
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
