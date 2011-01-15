package org.o42a.core.st;

import static org.o42a.core.ref.Cond.trueCondition;

import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.ref.Cond;


public abstract class Conditions {

	public static Conditions newConditions(LocationSpec location, Scope scope) {
		return new Empty(location, scope);
	}

	private Cond fullCondition;

	public abstract Cond getPrerequisite();

	public abstract Cond getCondition();

	public Cond fullCondition() {
		if (this.fullCondition != null) {
			return this.fullCondition;
		}
		return this.fullCondition = getPrerequisite().and(getCondition());
	}

	public Conditions notCondition(LocationSpec location) {
		return new NotCondition(location, this);
	}

	private static final class Empty extends Conditions {

		private final Cond condition;

		Empty(LocationSpec location, Scope scope) {
			this.condition = trueCondition(location, scope);
		}

		@Override
		public Cond getPrerequisite() {
			return this.condition;
		}

		@Override
		public Cond getCondition() {
			return this.condition;
		}

		@Override
		public String toString() {
			return "EmptyConditions";
		}

	}

	private static final class NotCondition extends Conditions {

		private final LocationSpec location;
		private final Conditions conditions;
		private boolean errorReported;

		NotCondition(LocationSpec location, Conditions conditions) {
			this.location = location;
			this.conditions = conditions;
		}

		@Override
		public Cond getPrerequisite() {
			reportError();
			return this.conditions.getPrerequisite();
		}

		@Override
		public Cond getCondition() {
			reportError();
			return this.conditions.getCondition();
		}

		@Override
		public Conditions notCondition(LocationSpec location) {
			return new NotCondition(location, this.conditions);
		}

		@Override
		public String toString() {
			return this.location.toString();
		}

		private void reportError() {
			if (this.errorReported) {
				return;
			}
			this.location.getContext().getLogger().notCondition(this.location);
		}

	}

}
