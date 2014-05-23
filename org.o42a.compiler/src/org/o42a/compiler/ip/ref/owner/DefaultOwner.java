/*
    Compiler
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
package org.o42a.compiler.ip.ref.owner;

import org.o42a.compiler.ip.access.AccessRules;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Call;
import org.o42a.core.source.LocationInfo;


final class DefaultOwner extends Owner {

	DefaultOwner(AccessRules accessRules, Ref ownerRef) {
		super(accessRules, ownerRef);
	}

	@Override
	public Ref ref() {
		return ownerRef();
	}

	@Override
	public Owner deref(LocationInfo location, LocationInfo deref) {
		return new DefaultOwner(
				getAccessRules(),
				ref()
				.getPath()
				.append(new DerefFragment(deref))
				.target(location, distribute()));
	}

	@Override
	public Owner eagerRef(
			LocationInfo location,
			LocationInfo eagerRef) {

		final AscendantsDefinition ascendants =
				new AscendantsDefinition(
						location,
						distribute(),
						ref().toTypeRef())
				.setEager(true);

		return new DefaultOwner(
				getAccessRules(),
				new Call(eagerRef, distribute(), ascendants, null).toRef());
	}

}
