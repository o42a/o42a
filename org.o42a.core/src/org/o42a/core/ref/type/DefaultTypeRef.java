/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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

import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueStructFinder;


final class DefaultTypeRef extends TypeRef {

	DefaultTypeRef(
			Ref ref,
			PrefixPath prefix,
			ValueStructFinder valueStructFinder,
			ValueStruct<?, ?> valueStruct) {
		super(ref, prefix, valueStructFinder, valueStruct);
	}

	@Override
	public boolean isStatic() {
		return getRescopedRef().isStatic();
	}

	@Override
	public final Ref getIntactRef() {
		return getRef();
	}

	@Override
	protected DefaultTypeRef create(
			Ref ref,
			Ref intactRef,
			PrefixPath prefix,
			ValueStructFinder valueStructFinder,
			ValueStruct<?, ?> valueStruct) {
		assert ref == intactRef :
			ref + " should be the same as " + intactRef;
		return new DefaultTypeRef(
				ref,
				prefix,
				valueStructFinder,
				valueStruct);
	}

}
