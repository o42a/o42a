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

import java.util.HashSet;
import java.util.Set;

import org.o42a.core.member.MemberKey;


public abstract class DefinitionTargets {

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

	public final boolean onlyConditions() {
		return this.mask == CONDITION_MASK;
	}

	public final boolean haveValue() {
		return (this.mask & VALUE_MASK) != 0;
	}

	public final boolean haveField() {
		return (this.mask & FIELD_MASK) != 0;
	}

	public abstract boolean haveField(MemberKey fieldKey);

	public final boolean haveDefinition() {
		return (this.mask & DEFINITION_MASK) != 0;
	}

	public final boolean haveDeclaration() {
		return (this.mask & DECLARATION_MASK) != 0;
	}

	public final DefinitionTargets add(DefinitionTargets other) {
		if (other.isEmpty()) {
			return this;
		}
		if (isEmpty()) {
			return other;
		}

		final Set<DefinitionKey> definitions1 = definitions();
		final Set<DefinitionKey> definitions2 = other.definitions();
		final HashSet<DefinitionKey> definitions = new HashSet<DefinitionKey>(
				definitions1.size() + definitions2.size());

		definitions.addAll(definitions1);
		if (!definitions.addAll(definitions2)) {
			return this;
		}
		if (definitions2.size() == definitions.size()) {
			return other;
		}

		return new MultiDefinitionTargets(
				(byte) (this.mask | other.mask),
				definitions);
	}

	final byte mask() {
		return this.mask;
	}

	abstract Set<DefinitionKey> definitions();

}
