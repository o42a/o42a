/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;

import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.*;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.object.def.Defs;
import org.o42a.core.ref.FullResolver;


public final class ObjectValueDefs implements UserInfo {

	private final ObjectValue objectValue;
	private final ValueDefsUses partUses;
	private Usable<SimpleUsage> ancestorDefsUpdates;

	ObjectValueDefs(ObjectValue objectValue) {
		this.objectValue = objectValue;
		this.partUses = new ValueDefsUses(this);
	}

	public final Obj getObject() {
		return getObjectValue().getObject();
	}

	public final ObjectValue getObjectValue() {
		return this.objectValue;
	}

	public final Defs getDefs() {
		return getObjectValue().getDefinitions().defs();
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
		return partUses().toUser();
	}

	public final boolean isUsed(
			Analyzer analyzer,
			UseSelector<ValuePartUsage> selector) {
		return this.partUses.isUsed(analyzer, selector);
	}

	public final void accessBy(UserInfo user) {
		partUses().useBy(user);
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
				.fullResolver(partUses().refUser(), VALUE_REF_USAGE);
	}

	public final void wrapBy(ObjectValueDefs wrapPart) {
		partUses().wrapBy(wrapPart.partUses());
		ancestorDefsUpdateUses().useBy(
				wrapPart.ancestorDefsUpdateUses(),
				SIMPLE_USAGE);
	}

	@Override
	public String toString() {
		if (this.objectValue == null) {
			return super.toString();
		}
		return "ValueDefsOf[" + getObject() + ']';
	}

	private ValueDefsUses partUses() {
		return this.partUses;
	}

	private final Usable<SimpleUsage> ancestorDefsUpdateUses() {
		if (this.ancestorDefsUpdates != null) {
			return this.ancestorDefsUpdates;
		}

		this.ancestorDefsUpdates = simpleUsable(
				"AncestorDefsUpdates",
				getObject());
		this.ancestorDefsUpdates.useBy(getObjectValue().rtUses(), SIMPLE_USAGE);

		return this.ancestorDefsUpdates;
	}

}