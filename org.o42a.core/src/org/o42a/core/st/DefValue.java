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
package org.o42a.core.st;

import org.o42a.core.ref.ScopeUpgrade;
import org.o42a.core.value.Condition;
import org.o42a.core.value.Value;


public final class DefValue {

	public static final DefValue TRUE_DEF_VALUE =
			new DefValue(Condition.TRUE, null);
	public static final DefValue FALSE_DEF_VALUE =
			new DefValue(Condition.FALSE, null);
	public static final DefValue RUNTIME_DEF_VALUE =
			new DefValue(Condition.RUNTIME, null);

	public static DefValue defValue(Value<?> value) {
		return new DefValue(value.getKnowledge().getCondition(), value);
	}

	private final Condition condition;
	private final Value<?> value;

	private DefValue(Condition condition, Value<?> value) {
		this.condition = condition;
		this.value = value;
	}

	public final Condition getCondition() {
		return this.condition;
	}

	public final Value<?> getValue() {
		return this.value;
	}

	public final boolean hasValue() {
		return this.value != null;
	}

	public final boolean hasKnownValue() {
		if (!getCondition().isConstant()) {
			return false;
		}
		if (!hasValue()) {
			return true;
		}
		return getValue().getKnowledge().isKnown();
	}

	public final DefValue upgradeScope(ScopeUpgrade upgrade) {
		if (!hasValue()) {
			return this;
		}

		final Value<?> oldValue = getValue();
		final Value<?> newValue = oldValue.prefixWith(upgrade.toPrefix());

		if (newValue == oldValue) {
			return this;
		}

		return defValue(newValue);
	}

	public final String valueString() {
		if (hasValue()) {
			return getValue().valueString();
		}
		return getCondition().toString();
	}

	@Override
	public String toString() {
		if (this.condition == null) {
			return super.toString();
		}
		if (this.value != null) {
			return this.value.toString();
		}
		return this.condition.toString();
	}

}
