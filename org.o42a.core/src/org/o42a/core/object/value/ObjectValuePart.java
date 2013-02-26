/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
import static org.o42a.core.ref.RefUser.rtRefUser;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.*;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.object.def.Defs;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.RefUser;


public final class ObjectValuePart implements UserInfo {

	private final ObjectValue objectValue;
	private final boolean claim;
	private Usable<ValuePartUsage> uses;
	private Usable<SimpleUsage> ancestorDefsUpdates;
	private RefUser refUser;

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
			ancestorDefsUpdateUses().useBy(user, SIMPLE_USAGE);
		}
	}

	public final UserInfo ancestorDefsUpdatesUser() {
		return ancestorDefsUpdateUses();
	}

	public final FullResolver fullResolver() {
		return getObject()
				.getScope()
				.resolver()
				.fullResolver(refUser(), VALUE_REF_USAGE);
	}

	public final void wrapBy(ObjectValuePart wrapPart) {
		uses().useBy(
				wrapPart.uses().usageUser(VALUE_PART_USAGE),
				VALUE_PART_USAGE);
		uses().useBy(
				wrapPart.uses().usageUser(VALUE_PART_ACCESS),
				VALUE_PART_ACCESS);
		ancestorDefsUpdateUses().useBy(
				wrapPart.ancestorDefsUpdateUses(),
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
		final Usable<ValueUsage> valueUses = objectValue.uses();
		final boolean runtimeConstructed =
				getObjectValue().isRuntimeConstructed();

		valueUses.useBy(
				this.uses,
				runtimeConstructed
				? RUNTIME_VALUE_USAGE : STATIC_VALUE_USAGE);
		this.uses.useBy(
				valueUses.selectiveUser(
						runtimeConstructed
						? ANY_RUNTIME_VALUE_USAGE
						: ANY_STATIC_VALUE_USAGE),
				VALUE_PART_USAGE);

		return this.uses;
	}

	private RefUser refUser() {
		if (this.refUser != null) {
			return this.refUser;
		}
		return this.refUser = rtRefUser(uses(), getObjectValue().rtUses());
	}

	private final Usable<SimpleUsage> ancestorDefsUpdateUses() {
		if (this.ancestorDefsUpdates != null) {
			return this.ancestorDefsUpdates;
		}

		final String name = isClaim() ? "Claim" : "Proposition";

		this.ancestorDefsUpdates = simpleUsable(
				name + "AncestorDefsUpdates",
				getObject());
		this.ancestorDefsUpdates.useBy(getObjectValue().rtUses(), SIMPLE_USAGE);

		return this.ancestorDefsUpdates;
	}

}
