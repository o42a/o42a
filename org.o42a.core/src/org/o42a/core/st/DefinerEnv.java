/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.core.Scope;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.Logical;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueStruct;


public abstract class DefinerEnv extends ImplicationEnv {

	public static DefinerEnv defaultEnv(LocationInfo location) {
		return new DefaultEnv(location);
	}

	public final boolean isConditional() {
		return hasPrerequisite() || hasPrecondition();
	}

	public abstract boolean hasPrerequisite();

	public abstract Logical prerequisite(Scope scope);

	public abstract boolean hasPrecondition();

	public abstract Logical precondition(Scope scope);

	public Logical fullLogical(Scope scope) {
		return prerequisite(scope).and(precondition(scope));
	}

	public DefinerEnv notCondition(LocationInfo location) {
		return new DefaultEnv(location);
	}

	public <D extends Def<D>> D apply(D def) {

		final D prereqDef;

		if (hasPrerequisite()) {
			prereqDef = def.addPrerequisite(prerequisite(def.getScope()));
		} else {
			prereqDef = def;
		}

		return prereqDef.addPrecondition(precondition(def.getScope()));
	}

	private static final class DefaultEnv extends DefinerEnv {

		private final LocationInfo location;

		DefaultEnv(LocationInfo location) {
			this.location = location;
		}

		@Override
		public boolean hasPrerequisite() {
			return false;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return logicalTrue(this.location, scope);
		}

		@Override
		public boolean hasPrecondition() {
			return false;
		}

		@Override
		public Logical precondition(Scope scope) {
			return logicalTrue(this.location, scope);
		}

		@Override
		public <D extends Def<D>> D apply(D def) {
			return def;
		}

		@Override
		public String toString() {
			return "DefaultEnv";
		}

		@Override
		protected ValueStruct<?, ?> expectedValueStruct() {
			return null;
		}

	}

}
