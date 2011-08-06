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
	private Usable usable;
	private Usable ancestorDefsUpdates;

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
		if (this.usable == null) {
			return useCase.toUseCase().unusedFlag();
		}
		return this.usable.getUseBy(useCase);
	}

	@Override
	public final boolean isUsedBy(UseCaseInfo useCase) {
		return getUseBy(useCase).isUsed();
	}

	public final UseFlag ancestorDefsUpdatedBy(UseCaseInfo useCase) {
		if (getObject().getConstructionMode().isRuntime()) {
			return useCase.toUseCase().usedFlag();
		}
		if (this.ancestorDefsUpdates == null) {
			return useCase.toUseCase().unusedFlag();
		}
		return this.ancestorDefsUpdates.getUseBy(useCase);
	}

	public final boolean isAncestorDefsUpdatedBy(UseCaseInfo useCase) {
		return ancestorDefsUpdatedBy(useCase).isUsed();
	}

	@Override
	public final User toUser() {
		return usable().toUser();
	}

	public final void updateAncestorDefsBy(UserInfo user) {
		if (!user.toUser().isDummy()) {
			ancestorDefsUpdates().useBy(user);
		}
	}

	public final Resolver resolver() {
		return getObject().getScope().newResolver(usable());
	}

	@Override
	public String toString() {
		if (this.defKind == null) {
			return super.toString();
		}
		return this.defKind.displayName() + "Of[" + getObject() + ']';
	}

	final Usable usable() {
		if (this.usable != null) {
			return this.usable;
		}

		this.usable = simpleUsable(this);
		getObjectValue().usable().useBy(this.usable);
		this.usable.useBy(getObjectValue().explicitUsable());

		return this.usable;
	}

	final Usable ancestorDefsUpdates() {
		if (this.ancestorDefsUpdates != null) {
			return this.ancestorDefsUpdates;
		}
		return this.ancestorDefsUpdates = simpleUsable(
				"Ancestor " + getDefKind().displayName() + "sUpdatesOf",
				getObject());
	}

	final void wrapBy(ObjectValuePart<?, ?> wrapPart) {
		usable().useBy(wrapPart.usable());
		ancestorDefsUpdates().useBy(wrapPart.ancestorDefsUpdates());
	}

}
