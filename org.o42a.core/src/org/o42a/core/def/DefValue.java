/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.def;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public class DefValue {

	static DefValue unknownValue(Def<?> def) {
		return new Unknown(def);
	}

	static DefValue alwaysIgnoredValue(Def<?> def) {
		return new AlwaysIgnored(def);
	}

	static DefValue nonExistingValue(Definitions definitions) {
		return new NonExisting(definitions);
	}

	static DefValue value(Def<?> def, Value<?> value) {
		return new DefValue(def, value);
	}

	static DefValue alwaysMeaningfulValue(Def<?> def, Value<?> value) {
		return new AlwaysMeaningful(def, value);
	}

	final SourceInfo sourced;
	private final Value<?> value;

	private DefValue(SourceInfo sourced, Value<?> value) {
		this.sourced = sourced;
		this.value = value;
		assert value != null :
			"Value not specified";
	}

	public final Obj getSource() {
		return this.sourced != null ? this.sourced.getSource() : null;
	}

	public Def<?> getDef() {
		return (Def<?>) this.sourced;
	}

	/**
	 * Returns real value.
	 *
	 * <p>Some definitions may produce logical value, but not a real one.
	 * For example, when {@link Def#getPrerequisite() prerequisite} fails,
	 * condition is <code>false</code>, or no definitions at all.</p>
	 *
	 * @return real value, or <code>null</code> when value is logical.
	 */
	public Value<?> getRealValue() {
		return this.value;
	}

	/**
	 * Returns value.
	 *
	 * @return either real or logical value. Never returns <code>null</code>.
	 */
	public final Value<?> getValue() {
		return this.value;
	}

	public boolean isRequirement() {

		final Def<?> def = getDef();

		return def != null && def.getKind().isClaim();
	}

	/**
	 * Value does not exist.
	 *
	 * <p>This may happen e.g. for {@link Definitions#isEmpty() empty
	 * definitions}.</p>
	 *
	 * @return <code>true</code> when value does not exist,
	 * or <code>false</code> otherwise.
	 */
	public boolean exists() {
		return this.sourced != null;
	}

	/**
	 * Value is unknown.
	 *
	 * <p>This may happen e.g. when {@link Def#getPrerequisite() definition
	 * prerequisite} not satisfied or {@link Def#definitionValue(
	 * org.o42a.core.Scope) definition value} can not be calculated, which is
	 * possible for imperative blocks.</p>
	 *
	 * <p>Value is always unknown if it does not {@link #exists() exist} or
	 * is {@link #isAlwaysIgnored() always ignored}.</p>
	 *
	 * @return <code>true</code> when value is unknown,
	 * or <code>false</code> otherwise.
	 */
	public boolean isUnknown() {
		return false;
	}

	/**
	 * This value is always ignored.
	 *
	 * <p>This happens e.g. when {@link Def#getPrerequisite() definition
	 * prerequisite} is constantly false.</p>
	 *
	 * <p>Value is ignored also when it doesn't {@link #exists() exist}.</p>
	 *
	 * @return <code>true</code> when this value can be always ignored,
	 * or <code>false</code> otherwise.
	 */
	public boolean isAlwaysIgnored() {
		return false;
	}

	/**
	 * This value is always meaningful.
	 *
	 * <p>This happens e.g. when {@link Def#getPrerequisite() definition
	 * prerequisite} is constantly true.</p>
	 *
	 * @return <code>true</code> when this value is always meaningful,
	 * or <code>false</code> otherwise.
	 */
	public boolean isAlwaysMeaningful() {
		return false;
	}

	public final boolean isFalse() {
		return this.value.isFalse();
	}

	public final boolean isDefinite() {
		return this.value.isDefinite();
	}

	public final LogicalValue getLogicalValue() {
		return this.value.getLogicalValue();
	}

	public final <T> Value<T> value(ValueType<T> valueType) {
		return valueType.cast(this.value);
	}

	public DefValue and(DefValue logicalDef) {
		if (logicalDef.isUnknown()) {
			return this;
		}

		final Value<?> newValue =
			this.value.require(logicalDef.getLogicalValue());

		if (newValue == this.value) {
			return this;
		}

		return new DefValue(getDef(), newValue);
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

	private static class Unknown extends DefValue {

		Unknown(Def<?> def) {
			super(def, Value.unknownValue());
		}

		@Override
		public final Value<?> getRealValue() {
			return null;
		}

		@Override
		public final boolean isUnknown() {
			return true;
		}

		@Override
		public String toString() {
			return "UNKNOWN";
		}

	}

	private static class AlwaysIgnored extends Unknown {

		AlwaysIgnored(Def<?> def) {
			super(def);
		}

		@Override
		public boolean isAlwaysIgnored() {
			return true;
		}

		@Override
		public String toString() {
			return "ALWAYS IGNORED";
		}

	}

	private static final class NonExisting extends AlwaysIgnored {

		NonExisting(Definitions definitions) {
			super(null);
		}

		@Override
		public Def<?> getDef() {
			return null;
		}

		@Override
		public String toString() {
			return "NON-EXISTING";
		}

	}

	private static final class AlwaysMeaningful extends DefValue {

		AlwaysMeaningful(Def<?> def, Value<?> value) {
			super(def, value);
		}

		@Override
		public boolean isAlwaysMeaningful() {
			return true;
		}

		@Override
		public String toString() {
			return "ALWAYS " + super.toString();
		}

	}

}
