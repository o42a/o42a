/*
    Compiler Core
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.core.object.value;

import org.o42a.core.value.link.LinkValueType;


/**
 * Object value statefulness.
 */
public enum Statefulness {

	/**
	 * Stateless value.
	 *
	 * <p>This means that object value is re-evaluated on each request.</p>
	 *
	 * <p>Object values are stateless by default, except for some specific value
	 * types, like variables and arrays. Note however, that despite the object
	 * value is stateless, the descendant's value can be stateful.</p>
	 */
	STATELESS,

	/**
	 * Eagerly evaluated value.
	 *
	 * <p>This means that the value is evaluated before object construction.
	 * The value definition function is never executed at run time in this case.
	 * </p>
	 */
	EAGER,

	/**
	 * Stateful value.
	 *
	 * <p>This means that object value is evaluated at most once, and then
	 * cached in object. All subsequent value requests will return the cached
	 * value.<p>
	 *
	 * <p>The object value is always stateful for some specific value types like
	 * arrays. For other value types the object value is stateful, when the
	 * object is constructed using a 'keep value' (<code>\\</code>) operator.
	 * </p>
	 *
	 * <p>If object value is stateful, then it's descendants' values are
	 * stateful too.</p>
	 */
	STATEFUL,

	/**
	 * Variable value.
	 *
	 * <p>A kind of stateful values, which can be modified at run time. The only
	 * value type with this kind of statefulness is
	 * {@link LinkValueType#VARIABLE}.</p>
	 */
	VARIABLE;

	/**
	 * Object value is stateless.
	 *
	 * @return <code>true</code> for {@link #STATELESS}.
	 */
	public final boolean isStateless() {
		return this == STATELESS;
	}

	/**
	 * Object value is eagerly evaluated.
	 *
	 * @return <code>true</code> for {@link #EAGER}.
	 */
	public final boolean isEager() {
		return this == EAGER;
	}

	/**
	 * Object value is stateful.
	 *
	 * @return <code>true</code> for {@link #STATEFUL} and {@link #VARIABLE}.
	 */
	public final boolean isStateful() {
		return this == STATEFUL || this == VARIABLE;
	}

	public final Statefulness setEager(boolean eager) {
		if (!eager) {
			return this;
		}
		return EAGER;
	}

	public final Statefulness makeEager() {
		if (isEager()) {
			return this;
		}
		return EAGER;
	}

}
