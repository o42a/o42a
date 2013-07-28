/*
    Compiler Commons
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.common.ref.state;

import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefBuilder;
import org.o42a.core.source.Location;


public final class StatefulRef implements RefBuilder {

	private final RefBuilder ref;

	public StatefulRef(RefBuilder ref) {
		this.ref = ref;
	}

	@Override
	public Location getLocation() {
		return this.ref.getLocation();
	}

	@Override
	public Ref buildRef(Distributor distributor) {

		final Ref ref = this.ref.buildRef(distributor);
		final KeepValueFragment keepValue = new KeepValueFragment(ref);

		return SELF_PATH.bind(getLocation(), distributor.getScope())
				.append(keepValue)
				.target(distributor);
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return "\\\\" + this.ref;
	}

}
