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

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Def;
import org.o42a.core.ref.Logical;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueStruct;


public abstract class StatementEnv {

	public static StatementEnv defaultEnv(LocationInfo location) {
		return new DefaultEnv(location);
	}

	public static StatementEnv objectEnv(Obj object) {
		return new ObjectEnv(object);
	}

	private ValueStruct<?, ?> expectedStruct;

	public final ValueStruct<?, ?> getExpectedValueStruct() {
		if (this.expectedStruct != null) {
			if (this.expectedStruct == ValueStruct.NONE) {
				return null;
			}
			return this.expectedStruct;
		}

		final ValueStruct<?, ?> expectedType = expectedValueStruct();

		if (expectedType == null) {
			this.expectedStruct = ValueStruct.NONE;
		} else {
			this.expectedStruct = expectedType;
		}

		return expectedType;
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

	public StatementEnv notCondition(LocationInfo location) {
		return new NotCondition(location, this);
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

	protected abstract ValueStruct<?, ?> expectedValueStruct();

	private static final class DefaultEnv extends StatementEnv {

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

	private static final class NotCondition extends StatementEnv {

		private final LocationInfo location;
		private final StatementEnv env;
		private boolean errorReported;

		NotCondition(LocationInfo location, StatementEnv env) {
			this.location = location;
			this.env = env;
		}

		@Override
		public boolean hasPrerequisite() {
			return false;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			reportError();
			return this.env.prerequisite(scope);
		}

		@Override
		public boolean hasPrecondition() {
			return false;
		}

		@Override
		public Logical precondition(Scope scope) {
			reportError();
			return this.env.precondition(scope);
		}

		@Override
		public StatementEnv notCondition(LocationInfo location) {
			return new NotCondition(location, this.env);
		}

		@Override
		public <D extends Def<D>> D apply(D def) {
			reportError();
			return def;
		}

		@Override
		public String toString() {
			return this.location.toString();
		}

		@Override
		protected ValueStruct<?, ?> expectedValueStruct() {
			return this.env.getExpectedValueStruct();
		}

		private void reportError() {
			if (this.errorReported) {
				return;
			}
			this.location.getContext().getLogger().notCondition(this.location);
		}

	}

	private static final class ObjectEnv extends StatementEnv {

		private final Obj object;

		ObjectEnv(Obj object) {
			this.object = object;
		}

		@Override
		public boolean hasPrerequisite() {
			return false;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return logicalTrue(this.object, scope);
		}

		@Override
		public boolean hasPrecondition() {
			return false;
		}

		@Override
		public Logical precondition(Scope scope) {
			return logicalTrue(this.object, scope);
		}

		@Override
		public String toString() {
			return "ObjectEnv[" + this.object + ']';
		}

		@Override
		protected ValueStruct<?, ?> expectedValueStruct() {
			return this.object.value().getValueStruct();
		}

	}

}
