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
package org.o42a.core.artifact.object;

import static org.o42a.core.artifact.object.ValuePartUsage.ALL_VALUE_PART_USAGES;
import static org.o42a.core.artifact.object.ValuePartUsage.VALUE_PART_ACCESS;
import static org.o42a.core.artifact.object.ValuePartUsage.VALUE_PART_USAGE;
import static org.o42a.core.artifact.object.ValueUsage.*;
import static org.o42a.util.use.SimpleUsage.SIMPLE_USAGE;
import static org.o42a.util.use.SimpleUsage.simpleUsable;

import org.o42a.core.def.Def;
import org.o42a.core.def.DefKind;
import org.o42a.core.def.Defs;
import org.o42a.core.ref.Resolver;
import org.o42a.util.use.*;


public abstract class ObjectValuePart<D extends Def<D>, S extends Defs<D, S>>
		implements UserInfo, Uses<ValuePartUsage> {

	private final ObjectValue objectValue;
	private final DefKind defKind;
	private Usable<ValuePartUsage> uses;
	private Usable<SimpleUsage> ancestorDefsUpdates;

	ObjectValuePart(ObjectValue objectValue, DefKind defKind) {
		this.objectValue = objectValue;
		this.defKind = defKind;
	}

	public final Obj getObject() {
		return getObjectValue().getObject();
	}

	public final ObjectValue getObjectValue() {
		return this.objectValue;
	}

	public final DefKind getDefKind() {
		return this.defKind;
	}

	@SuppressWarnings("unchecked")
	public final S getDefs() {
		return (S) getObjectValue().getDefinitions().defs(getDefKind());
	}

	public final Uses<SimpleUsage> ancestorDefsUpdates() {
		if (getObject().getConstructionMode().isRuntime()) {
			return SimpleUsage.alwaysUsed();
		}
		if (this.ancestorDefsUpdates == null) {
			return SimpleUsage.neverUsed();
		}
		return this.ancestorDefsUpdates;
	}

	@Override
	public final User<ValuePartUsage> toUser() {
		return uses().toUser();
	}

	@Override
	public final AllUsages<ValuePartUsage> allUsages() {
		return ALL_VALUE_PART_USAGES;
	}

	@Override
	public final UseFlag selectUse(
			UseCaseInfo useCase,
			UseSelector<ValuePartUsage> selector) {
		return uses().selectUse(useCase, selector);
	}

	@Override
	public final boolean isUsed(
			UseCaseInfo useCase,
			UseSelector<ValuePartUsage> selector) {
		return selectUse(useCase, selector).isUsed();
	}

	public final void accessBy(UserInfo user) {
		if (!user.toUser().isDummy()) {
			uses().useBy(user, VALUE_PART_ACCESS);
		}
	}

	public final void updateAncestorDefsBy(UserInfo user) {
		if (!user.toUser().isDummy()) {
			ancestorDefsUpdatedBy().useBy(user, SIMPLE_USAGE);
		}
	}

	public final Resolver resolver() {
		return getObject().getScope().newResolver(uses());
	}

	@Override
	public String toString() {
		if (this.defKind == null) {
			return super.toString();
		}
		return this.defKind.displayName() + "Of[" + getObject() + ']';
	}

	final Usable<ValuePartUsage> uses() {
		if (this.uses != null) {
			return this.uses;
		}

		this.uses = ValuePartUsage.usable(this);

		final ObjectValue objectValue = getObjectValue();
		final Obj object = objectValue.getObject();
		final Usable<ValueUsage> valueUses = objectValue.uses();

		valueUses.useBy(
				this.uses,
				object.isClone() ? RUNTIME_VALUE_USAGE : STATIC_VALUE_USAGE);
		this.uses.useBy(
				valueUses.usageUser(
						object.isClone()
						? EXPLICIT_RUNTINE_VALUE_USAGE
						: EXPLICIT_STATIC_VALUE_USAGE),
				VALUE_PART_USAGE);

		return this.uses;
	}

	final Usable<SimpleUsage> ancestorDefsUpdatedBy() {
		if (this.ancestorDefsUpdates != null) {
			return this.ancestorDefsUpdates;
		}
		return this.ancestorDefsUpdates = simpleUsable(
				"Ancestor " + getDefKind().displayName() + "sUpdatesOf",
				getObject());
	}

	final void wrapBy(ObjectValuePart<?, ?> wrapPart) {
		uses().useBy(
				wrapPart.uses().usageUser(VALUE_PART_USAGE),
				VALUE_PART_USAGE);
		uses().useBy(
				wrapPart.uses().usageUser(VALUE_PART_ACCESS),
				VALUE_PART_ACCESS);
		ancestorDefsUpdatedBy().useBy(
				wrapPart.ancestorDefsUpdatedBy(),
				SIMPLE_USAGE);
	}

}
