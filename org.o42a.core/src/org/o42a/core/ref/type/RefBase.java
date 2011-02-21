/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.ref.type;

import static org.o42a.core.def.Rescoper.transparentRescoper;

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.ref.Ref;


public abstract class RefBase extends org.o42a.core.def.RefBase {

	protected static TypeRef typeRef(Ref ref) {
		if (ref == null) {
			throw new NullPointerException("Type reference not specified");
		}
		return new DefaultTypeRef(
				ref,
				transparentRescoper(ref.getScope()));
	}

	protected static StaticTypeRef staticTypeRef(Ref ref) {
		if (ref == null) {
			throw new NullPointerException("Type reference not specified");
		}
		return new DefaultStaticTypeRef(
				ref,
				ref,
				transparentRescoper(ref.getScope()));
	}

	public RefBase(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

}
