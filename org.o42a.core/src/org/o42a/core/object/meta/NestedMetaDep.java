/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.core.object.meta;

import org.o42a.core.object.ObjectMeta;


public abstract class NestedMetaDep extends MetaDep {

	private final MetaDep parent;

	public NestedMetaDep(MetaDep parent, ObjectMeta declaredIn) {
		super(declaredIn);
		assert parent.getDeclaredIn().is(declaredIn.getParentMeta()) :
			parent.getDeclaredIn() + " is not a parent of " + declaredIn;
		this.parent = parent;
	}

	@Override
	public final MetaDep parentDep() {
		return this.parent;
	}

	@Override
	protected boolean triggered(ObjectMeta meta) {
		return parentDep().triggered(parentMeta(meta));
	}

	@Override
	protected boolean changed(ObjectMeta meta) {
		return parentDep().changed(parentMeta(meta));
	}

}
