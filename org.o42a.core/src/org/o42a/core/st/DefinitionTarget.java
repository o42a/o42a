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

import static org.o42a.core.st.DefinitionKey.CONDITION_DEFINITION_KEY;
import static org.o42a.core.st.DefinitionKey.VALUE_DEFINITION_KEY;
import static org.o42a.core.st.DefinitionKey.fieldDefinitionKey;

import java.util.Collections;
import java.util.Set;

import org.o42a.core.member.MemberKey;


public final class DefinitionTarget extends DefinitionTargets {

	private static final DefinitionTarget CONDITION =
		new DefinitionTarget(CONDITION_MASK, CONDITION_DEFINITION_KEY);
	private static final DefinitionTarget VALUE =
		new DefinitionTarget(VALUE_MASK, VALUE_DEFINITION_KEY);

	public static DefinitionTarget conditionDefinition() {
		return CONDITION;
	}

	public static DefinitionTarget valueDefinition() {
		return VALUE;
	}

	public static DefinitionTarget fieldDeclaration(MemberKey fieldKey) {
		return new DefinitionTarget(FIELD_MASK, fieldDefinitionKey(fieldKey));
	}

	private final DefinitionKey definitionKey;

	private DefinitionTarget(byte mask, DefinitionKey definitionKey) {
		super(mask);
		this.definitionKey = definitionKey;
	}

	public final MemberKey getFieldKey() {
		return this.definitionKey.getFieldKey();
	}

	public final boolean isCondition() {
		return haveCondition();
	}

	public final boolean isValue() {
		return haveValue();
	}

	public final boolean isField() {
		return haveField();
	}

	public final boolean isDefinition() {
		return haveDefinition();
	}

	public final boolean isDeclaration() {
		return haveDeclaration();
	}

	@Override
	public boolean haveField(MemberKey memberKey) {
		if (!isField()) {
			return false;
		}
		return getFieldKey().equals(memberKey);
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "DefinitionTarget[]";
		}

		final StringBuilder out = new StringBuilder();

		out.append("DefinitionTargets[");
		if (isCondition()) {
			out.append("logical");
		} else if (isValue()) {
			out.append("value");
		} else if (isField()) {
			out.append("field");
		}
		out.append(']');

		return out.toString();
	}

	@Override
	Set<DefinitionKey> definitions() {
		return Collections.singleton(this.definitionKey);
	}

}
