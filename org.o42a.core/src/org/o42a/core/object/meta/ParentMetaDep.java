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


public abstract class ParentMetaDep extends MetaDep {

	private final MetaDep nested;

	public ParentMetaDep(MetaDep nested) {
		super(nested.getDeclaredIn().getParentMeta());
		this.nested = nested;
	}

	@Override
	public final MetaDep nestedDep() {
		return this.nested;
	}

	@Override
	protected boolean triggered(ObjectMeta meta) {
		return nestedDep().triggered(nestedMeta(meta));
	}

	@Override
	protected boolean changed(ObjectMeta meta) {
		return nestedDep().changed(nestedMeta(meta));
	}

}
