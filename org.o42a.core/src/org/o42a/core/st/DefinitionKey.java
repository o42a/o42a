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

import org.o42a.core.member.MemberKey;


public abstract class DefinitionKey {

	public static final DefinitionKey CONDITION_DEFINITION_KEY =
			new ConditionKey();
	public static final DefinitionKey VALUE_DEFINITION_KEY = new ValueKey();

	public static DefinitionKey fieldDefinitionKey(MemberKey fieldKey) {
		assert fieldKey != null :
			"Field key not specified";
		return new FieldKey(fieldKey);
	}

	public final boolean isCondition() {
		return this == CONDITION_DEFINITION_KEY;
	}

	public final boolean isValue() {
		return this == VALUE_DEFINITION_KEY;
	}

	public final boolean isField() {
		return getFieldKey() != null;
	}

	public final boolean isDeclaration() {
		return !isCondition();
	}

	public abstract MemberKey getFieldKey();

	private static final class ConditionKey extends DefinitionKey {

		@Override
		public MemberKey getFieldKey() {
			return null;
		}

		@Override
		public String toString() {
			return "CONDITION";
		}

	}

	private static final class ValueKey extends DefinitionKey {

		@Override
		public MemberKey getFieldKey() {
			return null;
		}

		@Override
		public String toString() {
			return "VALUE";
		}

	}

	private static final class FieldKey extends DefinitionKey {

		private final MemberKey fieldKey;

		FieldKey(MemberKey fieldKey) {
			this.fieldKey = fieldKey;
		}

		@Override
		public MemberKey getFieldKey() {
			return this.fieldKey;
		}

		@Override
		public int hashCode() {
			return this.fieldKey.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}

			final FieldKey other = (FieldKey) obj;

			return this.fieldKey.equals(other.fieldKey);
		}

		@Override
		public String toString() {
			return "FIELD[" + this.fieldKey + ']';
		}

	}

}
