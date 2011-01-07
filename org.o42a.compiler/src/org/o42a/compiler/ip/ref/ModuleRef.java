/*
    Compiler
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
package org.o42a.compiler.ip.ref;

import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.path.Path;


public class ModuleRef extends Wrap {

	public ModuleRef(LocationSpec location, Distributor distributor) {
		super(location, distributor);
	}

	@Override
	protected Ref resolveWrapped() {

		Container container = getContainer();

		if (container.getScope().isTopScope()) {
			return ROOT_PATH.target(this, distribute());
		}

		Path path = null;

		for (;;) {

			final Container enclosing =
				container.getScope().getEnclosingContainer();

			if (enclosing.getScope().isTopScope()) {
				if (path == null) {
					return SELF_PATH.target(this, distribute());
				}
				return path.target(this, distribute());
			}

			if (path != null) {
				path =
					path.append(container.getScope().getEnclosingScopePath());
			} else {
				path = container.getScope().getEnclosingScopePath();
			}

			container = enclosing;
		}

	}

}
