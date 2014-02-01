/*
    Compiler Commons
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
package org.o42a.common.macro.st;

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.core.object.Meta;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;


final class TempMetaDep extends MetaDep {

	private final TempMacroDep dep;
	private final Ref macroRef;
	private MetaDep parentDep;
	private MetaDep nestedDep;

	TempMetaDep(Meta declaredIn, TempMacroDep dep, Ref macroRef) {
		super(declaredIn);
		this.dep = dep;
		this.macroRef = macroRef;
	}

	@Override
	public MetaDep parentDep() {
		return this.parentDep;
	}

	@Override
	public MetaDep nestedDep() {
		if (this.nestedDep != null) {
			return this.nestedDep;
		}

		final Meta nestedMeta =
				this.dep.getTempField()
				.toField()
				.field(dummyUser())
				.toObject()
				.meta();

		return this.nestedDep = new TempFieldUpdate(this, nestedMeta);
	}

	@Override
	protected boolean triggered(Meta meta) {

		final Resolution resolution =
				this.macroRef.resolve(meta.getObject().getScope().resolver());

		if (!resolution.isResolved()) {
			return false;
		}

		return resolution.toObject().meta().isUpdated();
	}

	@Override
	protected boolean changed(Meta meta) {
		return true;
	}

	final void setParentDep(MetaDep parentDep) {
		this.parentDep = parentDep;
	}

}
