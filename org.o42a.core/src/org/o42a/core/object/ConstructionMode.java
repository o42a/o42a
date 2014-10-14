/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.core.source.Intrinsics;


/**
 * Object construction mode.
 *
 * <p>When constructing new object at compile time, it is not always possible
 * to know exactly the objects it is derived from. This may cause the
 * restrictions on object definition and field declarations.</p>
 */
public enum ConstructionMode {

	/**
	 * Full construction mode.
	 *
	 * <p>Only fully constructed objects can declare new fields and upgrade
	 * their ancestors when overridden.</p>
	 *
	 * <p>This mode is applied when the object ancestor is a reference
	 * resolvable at compile time, and the object is either a module, a field
	 * derived from fully constructed one, and it is nested inside another
	 * fully constructed object.</p>
	 */
	FULL_CONSTRUCTION(),

	/**
	 * Strict object construction mode.
	 *
	 * <p>This mode is applied when object can not be constructed at compile
	 * time, but its value still can be evaluated.</p>
	 *
	 * <p>An example of strictly constructed object is a link target.</p>
	 */
	STRICT_CONSTRUCTION,

	/**
	 * Run time object construction mode.
	 *
	 * <p>This mode is applied when object can not be constructed at compile
	 * time, and its value can not be evaluated.</p>
	 *
	 * <p>An example of run time constructed object is a variable value.</p>
	 */
	RUNTIME_CONSTRUCTION,

	/**
	 * Predefined object construction mode.
	 *
	 * <p>This is applied to objects, which should be constructed without
	 * any optimization attempt. The only such object is
	 * {@link Intrinsics#getNone() NONE}. This object may be accessed by runtime
	 * directly, without proper full resolution.<p>
	 */
	PREDEFINED_CONSTRUCTION,

	/**
	 * Object construction is prohibited.
	 *
	 * <p>This may happen when object ancestor is unknown or it can not
	 * be inherited. This only happens due to compilation errors.</a>
	 */
	PROHIBITED_CONSTRUCTION;

	public final boolean isStrict() {
		return ordinal() >= STRICT_CONSTRUCTION.ordinal();
	}

	public final boolean isPredefined() {
		return this == PREDEFINED_CONSTRUCTION;
	}

	public final boolean isRuntime() {
		return ordinal() >= RUNTIME_CONSTRUCTION.ordinal();
	}

	public final boolean isProhibited() {
		return this == PROHIBITED_CONSTRUCTION;
	}

	/**
	 * Applies additional restrictions to this construction mode.
	 *
	 * @param other the construction mode, which restrictions should be applied
	 * to this one.
	 *
	 * @return construction mode with additional restrictions applied.
	 */
	public ConstructionMode restrictBy(ConstructionMode other) {
		return other.ordinal() > ordinal() ? other : this;
	}

	/**
	 * Applies the restrictions of this mode to another one.
	 *
	 * <p>If {@code target} mode is not specified, then returns this mode.
	 * Otherwise simply invokes
	 * {@link #restrictBy(ConstructionMode) target.restrictBy(this)}.</p>
	 *
	 * @param target the constructions mode to apply restrictions to,
	 * or <code>null</code>
	 *
	 * @return construction mode with additional restrictions applied.
	 */
	public final ConstructionMode restrict(ConstructionMode target) {
		if (target == null) {
			return this;
		}
		return target.restrictBy(this);
	}

}
