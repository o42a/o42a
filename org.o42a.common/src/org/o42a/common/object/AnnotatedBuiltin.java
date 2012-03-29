/*
    Modules Commons
    Copyright (C) 2011,2012 Ruslan Lopatin

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
import org.o42a.core.member.MemberOwner;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.value.ValueStruct;


public abstract class AnnotatedBuiltin
		extends AnnotatedObject
		implements Builtin {

	public AnnotatedBuiltin(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public boolean isConstantBuiltin() {
		return false;
	}

	@Override
	protected final Definitions explicitDefinitions() {

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
