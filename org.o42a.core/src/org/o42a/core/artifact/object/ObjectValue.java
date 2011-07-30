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

import org.o42a.core.def.Definitions;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.FullResolution;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.use.*;


public final class ObjectValue implements UserInfo {

	private final Obj object;
	private Usable usable;
	private ValueType<?> valueType;
	private Value<?> value;
	private Definitions definitions;
	private Definitions explicitDefinitions;
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
	public final User toUser() {
		return usable().toUser();
	}

	@Override
	public final boolean isUsedBy(UseCase useCase) {
		return getUseBy(useCase).isUsed();
	}

	@Override
	public final UseFlag getUseBy(UseCase useCase) {
		if (this.usable == null) {
			return useCase.unusedFlag();
		}
		return this.usable.getUseBy(useCase);
	}

	public final Value<?> getValue() {
		if (this.value == null) {
			this.value = getDefinitions().value(
					getObject().getScope().dummyResolver()).getValue();
		}
		return this.value;
	}

	public Value<?> value(Resolver resolver) {
		getObject().assertCompatible(resolver.getScope());

		final Value<?> result;

		if (resolver == getObject().getScope()) {
			result = getValue();
		} else {
			result = getDefinitions().value(resolver).getValue();
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

	public final ObjectValue useBy(UserInfo user) {
		if (!user.toUser().isDummy()) {
			usable().useBy(user);
		}
		return this;
	}

	public final Resolver valueResolver() {
		return getObject().getScope().newResolver(usable());
	}

	public final void resolveAll(UserInfo user) {
		if (this.fullyResolved) {
			useBy(user);
			return;
		}

		final Obj object = getObject();
		final FullResolution fullResolution =
				object.getContext().fullResolution();

		this.fullyResolved = true;
		fullResolution.start();
		try {
			object.resolveAll();
			useBy(user);
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

	private Definitions getAncestorDefinitions() {

		final Definitions ancestorDefinitions;
		final TypeRef ancestor = getObject().type().getAncestor();

		if (ancestor == null) {
			ancestorDefinitions = null;
		} else {
			ancestorDefinitions =
				ancestor.typeObject(this).value()
				.getDefinitions().upgradeScope(getObject().getScope());
		}

		return ancestorDefinitions;
	}

	private final Usable usable() {
		if (this.usable != null) {
			return this.usable;
		}

		this.usable = simpleUsable(this);
		getObject().content().useBy(this.usable);

		return this.usable;
	}

}
