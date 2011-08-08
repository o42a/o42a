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

import static org.o42a.util.use.Usable.simpleUsable;

import org.o42a.core.def.Def;
import org.o42a.core.def.DefKind;
import org.o42a.core.def.Defs;
import org.o42a.core.ref.Resolver;
import org.o42a.util.use.*;


public abstract class ObjectValuePart<D extends Def<D>, S extends Defs<D, S>>
		implements UserInfo {

	private final ObjectValue objectValue;
	private final DefKind defKind;
	private Usable usedBy;
	private Usable inheriedBy;
	private Usable ancestorDefsUpdatedBy;

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

	@Override
	public final UseFlag getUseBy(UseCaseInfo useCase) {
		if (this.usedBy == null) {
			return useCase.toUseCase().unusedFlag();
		}
		return this.usedBy.getUseBy(useCase);
	}

	@Override
	public final boolean isUsedBy(UseCaseInfo useCase) {
		return getUseBy(useCase).isUsed();
	}

	public final UseInfo inherited() {
		if (this.inheriedBy == null) {
			return NEVER_USED;
		}
		return this.inheriedBy;
	}

	public final UseInfo ancestorDefsUpdates() {
		if (getObject().getConstructionMode().isRuntime()) {
			return ALWAYS_USED;
		}
		if (this.ancestorDefsUpdatedBy == null) {
			return NEVER_USED;
		}
		return this.ancestorDefsUpdatedBy;
	}

	@Override
	public final User toUser() {
		return uses().toUser();
	}


	public final void inheritBy(UserInfo user) {
		if (!user.toUser().isDummy()) {
			inheritedBy().useBy(user);
		}
	}

	public final void updateAncestorDefsBy(UserInfo user) {
		if (!user.toUser().isDummy()) {
			ancestorDefsUpdatedBy().useBy(user);
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

	final Usable uses() {
		if (this.usedBy != null) {
			return this.usedBy;
		}

		this.usedBy = simpleUsable(this);
		getObjectValue().usable().useBy(this.usedBy);
		this.usedBy.useBy(getObjectValue().explicitUsable());

		return this.usedBy;
	}

	final Usable inheritedBy() {
		if (this.inheriedBy != null) {
			return this.inheriedBy;
		}

		this.inheriedBy = simpleUsable(
				getDefKind().displayName() + "InheritantsOf",
				getObject());
		uses().useBy(this.inheriedBy);

		return this.usedBy;
	}

	final Usable ancestorDefsUpdatedBy() {
		if (this.ancestorDefsUpdatedBy != null) {
			return this.ancestorDefsUpdatedBy;
		}
		return this.ancestorDefsUpdatedBy = simpleUsable(
				"Ancestor " + getDefKind().displayName() + "sUpdatesOf",
				getObject());
	}

	final void wrapBy(ObjectValuePart<?, ?> wrapPart) {
		uses().useBy(wrapPart.uses());
		inheritedBy().useBy(wrapPart.inheritedBy());
		ancestorDefsUpdatedBy().useBy(
				wrapPart.ancestorDefsUpdatedBy());
	}

}
