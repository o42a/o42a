/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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


public interface ValueConverter<T> {

	boolean convertibleFrom(ValueType<?> other);

	boolean convertibleParameters(
			TypeParameters<T> destination,
			TypeParameters<?> source);

	/**
	 * Converts a type paraameters to this value type.
	 *
	 * <p>This method is called only if this value type is not the same
	 * as the one the given parameters created for, and is
	 * {@link ValueType#convertibleFrom(ValueType) convertible} from it.</p>
	 *
	 * @param parameters parameters to convert.
	 * @return converted type parameters.
	 *
	 * @throws UnsupportedOperationException by default.
	 */
	TypeParameters<T> convertParameters(TypeParameters<?> parameters);
}
