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
	FULL_CONSTRUCTION() {

		@Override
		public boolean canUpgradeAncestor() {
			return true;
		}

		@Override
		public boolean canDeclareFields() {
			return true;
		}

	},

	/**
	 * Strict object construction mode.
	 *
	 * <p>This mode applied when object can not be statically or dynamically
	 * constructed, and the object is not inherited from variable target.</p>
	 *
	 * <p>Strict object construction can not change interface of the object.
	 * The following restrictions apply to strictly constructed objects:
	 * <ul>
	 * <li>new members can not be declared;</li>
	 * <li>object ancestors can not be upgraded;</li>
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
	 * be inherited. This only happens due to compilation errors.</a>
	 */
	PROHIBITED_CONSTRUCTION;

	public boolean canUpgradeAncestor() {
		return false;
	}

	public boolean canDeclareFields() {
		return false;
	}

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
