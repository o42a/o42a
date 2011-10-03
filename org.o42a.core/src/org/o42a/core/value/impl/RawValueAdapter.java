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
package org.o42a.core.value.impl;

import org.o42a.core.def.CondDef;
import org.o42a.core.def.ValueDef;
import org.o42a.core.ref.Ref;
import org.o42a.core.value.ValueAdapter;


public class RawValueAdapter extends ValueAdapter {

	private final Ref ref;

	public RawValueAdapter(Ref ref) {
		this.ref = ref;
	}

	@Override
	public Ref ref() {
		return this.ref;
	}

	@Override
	public ValueDef valueDef() {
		return this.ref.toValueDef();
	}

	@Override
	public CondDef condDef() {
		return this.ref.toCondDef();
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return this.ref.toString();
	}

}
