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
package org.o42a.core.value;


public final class ValueRequest {

	public static final ValueRequest NO_VALUE_REQUEST =
			new ValueRequest(null, false);

	private final ValueStruct<?, ?> expectedStruct;
	private final boolean transformAllowed;

	public ValueRequest(ValueStruct<?, ?> expectedStruct) {
		assert expectedStruct != null :
			"Expected value structure not specified";
		this.expectedStruct = expectedStruct;
		this.transformAllowed = true;
	}

	private ValueRequest(
			ValueStruct<?, ?> expectedStruct,
			boolean transformAllowed) {
		this.expectedStruct = expectedStruct;
		this.transformAllowed = transformAllowed;
	}

	public final ValueStruct<?, ?> getExpectedStruct() {
		return this.expectedStruct;
	}

	public final boolean isTransformAllowed() {
		return this.transformAllowed;
	}

	public final ValueRequest dontTransofm() {
		if (!isTransformAllowed()) {
			return this;
		}
		return new ValueRequest(getExpectedStruct(), false);
	}

	@Override
	public String toString() {
		if (this.expectedStruct == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append("ValueRequest[");
		out.append(this.expectedStruct);
		if (this.transformAllowed) {
			out.append(", allow transform]");
		} else {
			out.append(", don't transform]");
		}

		return out.toString();
	}
}
