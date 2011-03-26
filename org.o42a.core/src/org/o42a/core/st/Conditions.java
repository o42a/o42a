/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Def;
import org.o42a.core.ref.Logical;
import org.o42a.core.value.ValueType;


public abstract class Conditions {

	public static Conditions emptyConditions(LocationInfo location) {
		return new Empty(location);
	}

	public static Conditions objectConditions(Obj object) {
		return new ObjectConditions(object);
	}

	private ValueType<?> expectedType;

	public final ValueType<?> getExpectedType() {
		if (this.expectedType != null) {
			if (this.expectedType == ValueType.NONE) {
				return null;
			}
			return this.expectedType;
		}

		final ValueType<?> expectedType = expectedType();

		if (expectedType == null) {
			this.expectedType = ValueType.NONE;
		} else {
			this.expectedType = expectedType;
		}

		return expectedType;
	}

	public abstract Logical prerequisite(Scope scope);

	public abstract Logical precondition(Scope scope);

	public boolean isEmpty(Scope scope) {
		return prerequisite(scope).isTrue() && precondition(scope).isTrue();
	}

	public Logical fullLogical(Scope scope) {
		return prerequisite(scope).and(precondition(scope));
	}

	public Conditions notCondition(LocationInfo location) {
		return new NotCondition(location, this);
	}

	public Def apply(Def def) {
		return def.addPrerequisite(prerequisite(def.getScope()).toLogicalDef())
		.and(precondition(def.getScope()));
	}

	protected abstract ValueType<?> expectedType();

	private static final class Empty extends Conditions {

		private final LocationInfo location;

		Empty(LocationInfo location) {
			this.location = location;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return logicalTrue(this.location, scope);
		}

		@Override
		public Logical precondition(Scope scope) {
			return logicalTrue(this.location, scope);
		}

		@Override
		public Def apply(Def def) {
			return def;
		}

		@Override
		public String toString() {
			return "EmptyConditions";
		}

		@Override
		protected ValueType<?> expectedType() {
			return null;
		}

	}

	private static final class NotCondition extends Conditions {

		private final LocationInfo location;
		private final Conditions conditions;
		private boolean errorReported;

		NotCondition(LocationInfo location, Conditions conditions) {
			this.location = location;
			this.conditions = conditions;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			reportError();
			return this.conditions.prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {
			reportError();
			return this.conditions.precondition(scope);
		}

		@Override
		public Conditions notCondition(LocationInfo location) {
			return new NotCondition(location, this.conditions);
		}

		@Override
		public Def apply(Def def) {
			reportError();
			return def;
		}

		@Override
		public String toString() {
			return this.location.toString();
		}

		@Override
		protected ValueType<?> expectedType() {
			return this.conditions.getExpectedType();
		}

		private void reportError() {
			if (this.errorReported) {
				return;
			}
			this.location.getContext().getLogger().notCondition(this.location);
		}

	}

	private static final class ObjectConditions extends Conditions {

		private final Obj object;

		ObjectConditions(Obj object) {
			this.object = object;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return logicalTrue(this.object, scope);
		}

		@Override
		public Logical precondition(Scope scope) {
			return logicalTrue(this.object, scope);
		}

		@Override
		public String toString() {
			return "ObjectConditions[" + this.object + ']';
		}

		@Override
		protected ValueType<?> expectedType() {
			return this.object.getValueType();
		}

	}

}
