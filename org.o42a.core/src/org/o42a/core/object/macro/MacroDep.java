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
package org.o42a.core.object.macro;

import org.o42a.core.Scope;
import org.o42a.core.object.Meta;
import org.o42a.core.object.macro.impl.MacroDepBuilder;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;


public abstract class MacroDep<D extends MetaDep> {

	public final D buildDep(Ref ref) {
		return buildDep(ref.getPath(), ref.getScope());
	}

	public final D buildDep(BoundPath path, Scope start) {
		if (path.isStatic()) {
			return null;
		}

		final MacroDepBuilder<D> walker = new MacroDepBuilder<D>(this);

		return walker.buildDep(path, start);
	}

	public abstract D newDep(Meta meta);

	public abstract void setParentDep(D dep, MetaDep parentDep);

}
