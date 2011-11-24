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

import static org.o42a.core.artifact.object.ValueUsage.*;
import static org.o42a.util.use.SimpleUsage.SIMPLE_USAGE;
import static org.o42a.util.use.SimpleUsage.simpleUsable;

import org.o42a.core.def.Def;
import org.o42a.core.def.DefKind;
import org.o42a.core.def.Defs;
import org.o42a.core.ref.Resolver;
import org.o42a.util.use.*;


public abstract class ObjectValuePart<D extends Def<D>, S extends Defs<D, S>>
		implements UserInfo {

	private final ObjectValue objectValue;
	private final DefKind defKind;
	private Usable<SimpleUsage> usedBy;
	private Usable<SimpleUsage> accessedBy;
	private Usable<SimpleUsage> ancestorDefsUpdatedBy;

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

	public final Uses<SimpleUsage> accessed() {
		if (this.accessedBy == null) {
			return SimpleUsage.neverUsed();
		}
		return this.accessedBy;
	}

	public final Uses<SimpleUsage> ancestorDefsUpdates() {
		if (getObject().getConstructionMode().isRuntime()) {
			return SimpleUsage.alwaysUsed();
		}
		if (this.ancestorDefsUpdatedBy == null) {
			return SimpleUsage.neverUsed();
		}
		return this.ancestorDefsUpdatedBy;
	}

	@Override
	public final User<?> toUser() {
		return uses().toUser();
	}

	public final void accessBy(UserInfo user) {
		if (!user.toUser().isDummy()) {
			accessedBy().useBy(user, SIMPLE_USAGE);
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

	final Usable<SimpleUsage> uses() {
		if (this.usedBy != null) {
			return this.usedBy;
		}

		this.usedBy = simpleUsable(this);

		final ObjectValue objectValue = getObjectValue();
		final Obj object = objectValue.getObject();
		final Usable<ValueUsage> valueUses = objectValue.uses();

		valueUses.useBy(
				this.usedBy,
				object.isClone() ? RUNTIME_VALUE_USAGE : STATIC_VALUE_USAGE);
		this.usedBy.useBy(
				valueUses.usageUser(
						object.isClone()
						? EXPLICIT_RUNTINE_VALUE_USAGE
						: EXPLICIT_STATIC_VALUE_USAGE),
				SIMPLE_USAGE);

		return this.usedBy;
	}

	final Usable<SimpleUsage> accessedBy() {
		if (this.accessedBy != null) {
			return this.accessedBy;
		}

		this.accessedBy = simpleUsable(
				getDefKind().displayName() + "AccessOf",
				getObject());
		uses().useBy(this.accessedBy, SIMPLE_USAGE);

		return this.accessedBy;
	}

	final Usable<SimpleUsage> ancestorDefsUpdatedBy() {
		if (this.ancestorDefsUpdatedBy != null) {
			return this.ancestorDefsUpdatedBy;
		}
		return this.ancestorDefsUpdatedBy = simpleUsable(
				"Ancestor " + getDefKind().displayName() + "sUpdatesOf",
				getObject());
	}

	final void wrapBy(ObjectValuePart<?, ?> wrapPart) {
		uses().useBy(wrapPart.uses(), SIMPLE_USAGE);
		accessedBy().useBy(wrapPart.accessedBy(), SIMPLE_USAGE);
		ancestorDefsUpdatedBy().useBy(
				wrapPart.ancestorDefsUpdatedBy(),
				SIMPLE_USAGE);
	}

}
