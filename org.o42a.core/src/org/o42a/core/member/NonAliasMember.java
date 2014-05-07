/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.member;

import static org.o42a.core.ref.path.Path.staticPath;

import org.o42a.core.Distributor;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Local;


public abstract class NonAliasMember extends Member implements MemberPath {

	public NonAliasMember(
			LocationInfo location,
			Distributor distributor,
			Obj owner) {
		super(location, distributor, owner);
	}

	@Override
	public MemberPath getMemberPath() {
		return this;
	}

	@Override
	public final Path pathToMember() {
		if (isStatic()) {
			return staticPath(getScope(), getScope()).append(getMemberKey());
		}
		return getMemberKey().toPath();
	}

	@Override
	public final Member toMember() {
		return this;
	}

	@Override
	public final Local toLocal() {
		return null;
	}

}
