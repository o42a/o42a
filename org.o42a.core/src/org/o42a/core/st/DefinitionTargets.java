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

import static org.o42a.core.st.DefinitionKey.CONDITION_DEFINITION_KEY;
import static org.o42a.core.st.DefinitionKey.VALUE_DEFINITION_KEY;

import java.util.Map;


public abstract class DefinitionTargets
		extends ImplicationTarget
		implements Iterable<DefinitionKey> {

	private static final EmptyDefinitionTargets EMPTY_DEFINITION_TARGETS =
			new EmptyDefinitionTargets();

	public static DefinitionTargets noDefinitions() {
		return EMPTY_DEFINITION_TARGETS;
	}

	static final byte CONDITION_MASK = 0x01;
	static final byte VALUE_MASK = 0x02;
	static final byte FIELD_MASK = 0x04;

	static final byte DEFINITION_MASK = CONDITION_MASK | VALUE_MASK;
	static final byte DECLARATION_MASK = VALUE_MASK | FIELD_MASK;

	private final byte mask;

	DefinitionTargets(byte mask) {
		this.mask = mask;
	}

	public final boolean isEmpty() {
		return this.mask == 0;
	}

	public final boolean haveCondition() {
		return (this.mask & CONDITION_MASK) != 0;
	}

	@Override
	public final boolean haveValue() {
		return (this.mask & VALUE_MASK) != 0;
	}

	public final boolean haveField() {
		return (this.mask & FIELD_MASK) != 0;
	}

	public final boolean haveDefinition() {
		return (this.mask & DEFINITION_MASK) != 0;
	}

	public final boolean haveDeclaration() {
		return (this.mask & DECLARATION_MASK) != 0;
	}

	public final DefinitionTarget firstCondition() {
		return first(CONDITION_DEFINITION_KEY);
	}

	public final DefinitionTarget lastCondition() {
		return last(CONDITION_DEFINITION_KEY);
	}

	public final DefinitionTarget firstValue() {
		return first(VALUE_DEFINITION_KEY);
	}

	public final DefinitionTarget lastValue() {
		return last(VALUE_DEFINITION_KEY);
	}

	public abstract DefinitionTarget firstDeclaration();

	public abstract DefinitionTarget lastDeclaration();

	public abstract DefinitionTarget first(DefinitionKey key);

	public abstract DefinitionTarget last(DefinitionKey key);

	public final DefinitionTargets add(DefinitionTargets other) {
		if (other.isEmpty()) {
			return this;
		}
		if (isEmpty()) {
			return other;
		}
		return new MultiDefinitionTargets(this, other);
	}

	final byte mask() {
		return this.mask;
	}

	abstract Map<DefinitionKey, Entry> targets();

	static final class Entry {

		private final DefinitionTarget first;
		private final DefinitionTarget last;

		Entry(DefinitionTarget target) {
			this.first = target;
			this.last = target;
		}

		private Entry(DefinitionTarget first, DefinitionTarget last) {
			this.first = first;
			this.last = last;
		}

		public final DefinitionTarget getFirst() {
			return this.first;
		}

		public final DefinitionTarget getLast() {
			return this.last;
		}

		public Entry add(Entry other) {
			if (other == null) {
				return this;
			}
			return new Entry(this.first, other.last);
		}

		@Override
		public String toString() {
			if (this.first == this.last) {
				return this.first.toString();
			}
			return "(first: " + this.first + ", last: " + this.last + ')';
		}

	}

}
