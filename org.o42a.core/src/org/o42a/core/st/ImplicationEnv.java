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
package org.o42a.core.st;

import static org.o42a.core.value.ValueRequest.NO_VALUE_REQUEST;

import org.o42a.core.value.ValueRequest;


public abstract class ImplicationEnv {

	private ValueRequest valueRequest;

	public final ValueRequest getValueRequest() {
		if (this.valueRequest != null) {
			return this.valueRequest;
		}

		final ValueRequest valueRequest = buildValueRequest();

		if (valueRequest == null) {
			return this.valueRequest = NO_VALUE_REQUEST;
		}

		return this.valueRequest = valueRequest;
	}

	protected abstract ValueRequest buildValueRequest();

}
