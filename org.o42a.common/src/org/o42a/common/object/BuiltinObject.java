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
package org.o42a.common.object;

import org.o42a.common.def.Builtin;
import org.o42a.common.def.BuiltinValueDef;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.StandaloneObjectScope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueStruct;


public abstract class BuiltinObject extends Obj implements Builtin {

	public BuiltinObject(
			LocationInfo location,
			Distributor enclosing,
			ValueStruct<?, ?> valueStruct) {
		super(location, enclosing);
		setValueStruct(valueStruct);
	}

	protected BuiltinObject(StandaloneObjectScope scope, ValueStruct<?, ?> valueStruct) {
		super(scope);
		setValueStruct(valueStruct);
	}

	protected BuiltinObject(Scope scope, ValueStruct<?, ?> valueStruct) {
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

		final ValueStruct<?, ?> ancestorValueStruct =
				type().getAncestor().getValueStruct();
		final ValueStruct<?, ?> valueStruct;

		if (!ancestorValueStruct.isScoped()) {
			valueStruct = ancestorValueStruct;
		} else {

			final PrefixPath prefix =
					getScope().getEnclosingScopePath().toPrefix(getScope());

			valueStruct = ancestorValueStruct.prefixWith(prefix);
		}

		return new BuiltinValueDef(this).toDefinitions(valueStruct);
	}

}
