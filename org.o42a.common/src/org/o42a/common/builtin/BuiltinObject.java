/*
    Modules Commons
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
package org.o42a.common.builtin;

import org.o42a.core.Distributor;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.ObjectScope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueStruct;


public abstract class BuiltinObject extends Obj implements Builtin {

	public BuiltinObject(
			LocationInfo location,
			Distributor enclosing,
			ValueStruct<?, ?> valueStruct) {
		super(location, enclosing);
		setValueStruct(valueStruct);
	}

	protected BuiltinObject(ObjectScope scope, ValueStruct<?, ?> valueStruct) {
		super(scope);
		setValueStruct(valueStruct);
	}

	@Override
	public boolean isConstantBuiltin() {
		return false;
	}

	@Override
	public abstract String toString();

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(
				value().getValueType().typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {

		final TypeParameters<?> ancestorTypeParameters =
				type().getAncestor().getParameters();
		final TypeParameters<?> typeParameters;

		if (ancestorTypeParameters.isEmpty()) {
			typeParameters = ancestorTypeParameters;
		} else {

			final PrefixPath prefix =
					getScope().getEnclosingScopePath().toPrefix(getScope());

			typeParameters = ancestorTypeParameters.prefixWith(prefix);
		}

		return new BuiltinDef(this).toDefinitions(typeParameters);
	}

}
