/*
    Compiler Core
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
package org.o42a.core.object;


public enum Accessor {

	/**
	 * Owner access to all of it's fields.
	 */
	OWNER() {

		@Override
		public boolean implies(Accessor accessor) {
			return true;
		}

	},

	/**
	 * Access to protected fields of ancestor.
	 */
	INHERITANT() {

		@Override
		public boolean implies(Accessor accessor) {
			return accessor != PUBLIC;
		}

	},

	/**
	 * Access to protected fields of enclosing object.
	 */
	ENCLOSED() {

		@Override
		public boolean implies(Accessor accessor) {
			return accessor != PUBLIC;
		}

	},

	/**
	 * Access to all fields explicitly declared or overridden within the same
	 * source.
	 */
	DECLARATION() {

		@Override
		public boolean implies(Accessor accessor) {
			return true;
		}

	},

	/**
	 * Access to public fields only.
	 */
	PUBLIC() {

		@Override
		public boolean implies(Accessor accessor) {
			return accessor == PUBLIC;
		}

	};

	public abstract boolean implies(Accessor accessor);

}
