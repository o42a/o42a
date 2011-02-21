/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.ref;

import static org.o42a.core.member.AdapterId.adapterId;

import org.o42a.core.LocationSpec;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.StaticTypeRef;


final class Adapter extends Wrap {

	private final Ref ref;
	private final StaticTypeRef adapterType;

	Adapter(LocationSpec location, Ref ref, StaticTypeRef adapterType) {
		super(location, ref.distribute());
		this.ref = ref;
		this.adapterType = adapterType;
	}

	@Override
	protected Ref resolveWrapped() {

		final Resolution resolution = this.ref.getResolution();

		if (resolution.isError()) {
			return errorRef(resolution);
		}

		final Obj objectType = resolution.materialize();

		if (objectType.derivedFrom(this.adapterType.getType())) {
			return this.ref;
		}

		final Member adapterMember =
			objectType.member(adapterId(this.adapterType));

		if (adapterMember == null) {
			getLogger().incompatible(this.ref, this.adapterType);
			return errorRef(this);
		}

		final Path adapterPath = adapterMember.getKey().toPath();

		return adapterPath.target(this, distribute(), this.ref);
	}

}
