/*
    Compiler Core
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
package org.o42a.core.value;

import org.o42a.core.Scope;
import org.o42a.core.member.local.LocalResolver;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.value.impl.RawValueAdapter;


public abstract class ValueAdapter {

	public static ValueAdapter rawValueAdapter(Ref ref) {
		return new RawValueAdapter(ref);
	}

	public abstract Def valueDef();

	public abstract Logical logical(Scope scope);

	public abstract Value<?> value(Resolver resolver);

	public Value<?> initialValue(LocalResolver resolver) {
		return value(resolver);
	}

	public abstract LogicalValue initialCond(LocalResolver resolver);

}
