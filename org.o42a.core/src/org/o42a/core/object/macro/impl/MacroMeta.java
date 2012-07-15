/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.macro.impl;

import org.o42a.core.object.Meta;
import org.o42a.core.object.meta.MetaTrigger;
import org.o42a.core.object.meta.MetaUpdate;
import org.o42a.core.object.meta.ObjectMeta;


public abstract class MacroMeta extends ObjectMeta {

	void addDep(MetaTrigger trigger, MetaUpdate update) {
		addDep(createDep(trigger, update));
		addToParent(trigger, update);
	}

	private final Meta meta() {
		return (Meta) this;
	}

	private void addToParent(MetaTrigger trigger, MetaUpdate update) {

		MetaUpdate currentUpdate = update;
		Meta currentMeta = meta();

		for (;;) {

			final MetaUpdate parentUpdate = currentUpdate.parentUpdate();

			if (parentUpdate == null) {
				return;
			}

			final Meta parentMeta = currentUpdate.parentMeta(currentMeta);
			final MacroMeta macroMeta = parentMeta;

			macroMeta.addDep(createDep(trigger, parentUpdate));

			currentMeta = parentMeta;
			currentUpdate = parentUpdate;
		}
	}

}
