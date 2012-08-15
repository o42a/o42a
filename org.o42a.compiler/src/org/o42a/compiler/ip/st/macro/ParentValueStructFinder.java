/*
    Compiler
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
package org.o42a.compiler.ip.st.macro;

import static org.o42a.core.object.link.LinkValueType.GETTER;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.LinkValueStruct;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueStructFinder;


final class ParentValueStructFinder implements ValueStructFinder {

	private final Scope scope;

	ParentValueStructFinder(Scope scope) {
		this.scope = scope;
	}

	@Override
	public ValueStruct<?, ?> valueStructBy(ValueStruct<?, ?> defaultStruct) {
		return valueStruct();
	}

	@Override
	public ValueStructFinder reproduce(Reproducer reproducer) {
		this.scope.assertCompatibleScope(reproducer.getReproducingScope());
		return new ParentValueStructFinder(reproducer.getScope());
	}

	@Override
	public ValueStructFinder prefixWith(PrefixPath prefix) {
		return valueStruct().prefixWith(prefix);
	}

	private ValueStruct<?, ?> valueStruct() {

		final Obj parent = this.scope.toObject();
		final ValueStruct<?, ?> parentValueStruct =
				parent.value().getValueStruct();
		final LinkValueStruct parentLinkStruct =
				parentValueStruct.toLinkStruct();

		if (parentLinkStruct != null) {
			// Parent object is link.
			if (parentLinkStruct.getValueType().isStateless()) {
				// Parent object is getter.
				return parentLinkStruct;
			}
			// Construct a getter with the same interface.
			return GETTER.linkStruct(parentLinkStruct.getTypeRef());
		}

		final StaticTypeRef parentValueTypeRef =
				parentValueStruct.getValueType()
				.typeRef(this.scope, this.scope.getEnclosingScope())
				.setValueStruct(parentValueStruct)
				.rescope(this.scope);

		// Construct a getter.
		return GETTER.linkStruct(parentValueTypeRef);
	}

}