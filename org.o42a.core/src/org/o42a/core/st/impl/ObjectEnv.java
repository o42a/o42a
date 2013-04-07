/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.core.st.impl;

import org.o42a.core.object.Obj;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.value.ValueRequest;


public final class ObjectEnv extends CommandEnv {

	private final Obj object;
	private ValueRequest valueRequest;

	public ObjectEnv(Obj object) {
		this.object = object;
	}

	@Override
	public ValueRequest getValueRequest() {
		if (this.valueRequest != null) {
			return this.valueRequest;
		}
		return this.valueRequest = new ValueRequest(
				this.object.type().getParameters(),
				this.object.getLogger());
	}

	@Override
	public String toString() {
		if (this.object == null) {
			return super.toString();
		}
		return this.object.toString();
	}

}
