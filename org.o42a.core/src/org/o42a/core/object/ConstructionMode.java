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
	 * Full (unrestricted) object construction mode.
	 *
	 * <p>This mode is always applied to top-level objects, such as modules.</p>
	 *
	 * <p>Object can be fully constructed only when all of the following
	 * requirements satisfied:
	 * <ul>
	 * <li>object is inherited from another fully constructed object;</li>
	 * <li>object construction appears within declarative code;</li>
	 * <li>object constructed inside definition of another fully constructed
	 * object.</li>
	 * </ul>
	 */
	FULL_CONSTRUCTION,

	/**
	 * Strict object construction mode.
	 *
	 * <p>This mode applied when object can not be {@link #FULL_CONSTRUCTION
	 * fully constructed} and object is not inherited from variable target.</p>
	 *
	 * <p>Strict object construction can not change interface of the object.
	 * The following restrictions apply to strictly constructed object:
	 * <ul>
	 * <li>object can not have samples;</li>
	 * <li>new adapters can not be declared;</li>
	 * <li>object field ancestors can not be upgraded;</li>
	 * <li>type parameters can not be changed.</li>
	 * <ul>
	 *
	 * <p>Object is strictly constructed e.g. when it's ancestor is link target.
	 * In some cases, the value of strictly constructed object can be determined
	 * at compile time.</p>
	 */
	STRICT_CONSTRUCTION,

	/**
	 * Run time object construction mode.
	 *
	 * <p>Applied when ancestor object can be determined at run time only.</p>
	 *
	 * <p>This mode is the same as the {@link #STRICT_CONSTRUCTION strict} one,
	 * but is applied when object ancestor is  variable. In contrast to strictly
	 * constructed object, the value of run-time constructed one can not be
	 * evaluated at compile time.</p>
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
	 * <p>This may happen, when object ancestor is unknown or it can not
	 * be inherited (e.g. when it is array).
	 */
	PROHIBITED_CONSTRUCTION;

	public final boolean isStrict() {
		return this != FULL_CONSTRUCTION;
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

}
