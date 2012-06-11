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
package org.o42a.core.object.value;

import static org.o42a.analysis.use.SimpleUsage.SIMPLE_USAGE;
import static org.o42a.analysis.use.SimpleUsage.simpleUsable;
import static org.o42a.core.object.value.ValuePartUsage.VALUE_PART_ACCESS;
import static org.o42a.core.object.value.ValuePartUsage.VALUE_PART_USAGE;
import static org.o42a.core.object.value.ValueUsage.*;
import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.*;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.object.def.Defs;
import org.o42a.core.ref.FullResolver;


public final class ObjectValuePart implements UserInfo {

	private final ObjectValue objectValue;
	private final boolean claim;
	private Usable<ValuePartUsage> uses;
	private Usable<SimpleUsage> ancestorDefsUpdates;

	ObjectValuePart(ObjectValue objectValue, boolean claim) {
		this.objectValue = objectValue;
		this.claim = claim;
	}

	public final Obj getObject() {
		return getObjectValue().getObject();
	}

	public final ObjectValue getObjectValue() {
		return this.objectValue;
	}

	public final boolean isClaim() {
		return this.claim;
	}

	public final Defs getDefs() {
		return getObjectValue().getDefinitions().defs(isClaim());
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

	public final UseFlag selectUse(
			Analyzer analyzer,
			UseSelector<ValuePartUsage> selector) {
		return uses().selectUse(analyzer, selector);
	}

	public final boolean isUsed(
			Analyzer analyzer,
			UseSelector<ValuePartUsage> selector) {
		return selectUse(analyzer, selector).isUsed();
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

	public final UserInfo ancestorDefsUpdatedByUser() {
		return ancestorDefsUpdatedBy();
	}

	public final FullResolver fullResolver() {
		return getObject()
				.getScope()
				.resolver()
				.fullResolver(uses(), VALUE_REF_USAGE);
	}

	public final void wrapBy(ObjectValuePart wrapPart) {
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

	@Override
	public String toString() {
		if (this.objectValue == null) {
			return super.toString();
		}
		return (isClaim() ? "ClaimOf[" : "PropositionOf[") + getObject() + ']';
	}

	final Usable<ValuePartUsage> uses() {
		if (this.uses != null) {
			return this.uses;
		}

		this.uses = ValuePartUsage.usable(this);

		final ObjectValueParts objectValue = getObjectValue();
		final Obj object = objectValue.getObject();
		final Usable<ValueUsage> valueUses = objectValue.uses();

		valueUses.useBy(
				this.uses,
				object.isClone() ? RUNTIME_VALUE_USAGE : STATIC_VALUE_USAGE);
		this.uses.useBy(
				valueUses.usageUser(
						object.isClone()
						? EXPLICIT_RUNTIME_VALUE_USAGE
						: EXPLICIT_STATIC_VALUE_USAGE),
				VALUE_PART_USAGE);

		return this.uses;
	}

	final Usable<SimpleUsage> ancestorDefsUpdatedBy() {
		if (this.ancestorDefsUpdates != null) {
			return this.ancestorDefsUpdates;
		}

		final String name = isClaim() ? "Claim" : "Proposition";

		return this.ancestorDefsUpdates = simpleUsable(
				"Ancestor " + name + "UpdatesOf",
				getObject());
	}

}
