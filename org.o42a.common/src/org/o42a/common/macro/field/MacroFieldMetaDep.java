/*
    Compiler Commons
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.common.macro.field;

import org.o42a.core.object.ObjectMeta;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;


final class MacroFieldMetaDep extends MetaDep {

	private final Ref macroRef;
	private MetaDep parentDep;

	MacroFieldMetaDep(ObjectMeta declaredIn, Ref macroRef) {
		super(declaredIn);
		this.macroRef = macroRef;
	}

	@Override
	public MetaDep parentDep() {
		return this.parentDep;
	}

	@Override
	public MetaDep nestedDep() {
		return null;
	}

	@Override
	protected boolean triggered(ObjectMeta meta) {

		final Resolution resolution =
				this.macroRef.resolve(meta.getObject().getScope().resolver());

		if (!resolution.isResolved()) {
			return false;
		}

		return resolution.toObject().meta().isUpdated();
	}

	@Override
	protected boolean changed(ObjectMeta meta) {
		return true;
	}

	final void setParentDep(MetaDep parentDep) {
		this.parentDep = parentDep;
	}

}
