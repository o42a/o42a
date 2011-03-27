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

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.o42a.core.st.DefinitionKey.CONDITION_DEFINITION_KEY;
import static org.o42a.core.st.DefinitionKey.VALUE_DEFINITION_KEY;
import static org.o42a.core.st.DefinitionKey.fieldDefinitionKey;

import java.util.Iterator;
import java.util.Map;

import org.o42a.core.member.DeclarationStatement;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.Ref;
import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;


public final class DefinitionTarget
		extends DefinitionTargets
		implements LogInfo {

	public static DefinitionTarget conditionDefinition(Ref ref) {
		return new DefinitionTarget(
				CONDITION_MASK,
				ref,
				CONDITION_DEFINITION_KEY);
	}

	public static DefinitionTarget valueDefinition(Ref ref) {
		return new DefinitionTarget(VALUE_MASK, ref, VALUE_DEFINITION_KEY);
	}

	public static DefinitionTarget fieldDeclaration(
			DeclarationStatement statement) {
		return new DefinitionTarget(
				FIELD_MASK,
				statement,
				fieldDefinitionKey(statement.toMember().getKey()));
	}

	private final Statement statement;
	private final DefinitionKey definitionKey;

	private DefinitionTarget(
			byte mask,
			Statement statement,
			DefinitionKey definitionKey) {
		super(mask);
		this.statement = statement;
		this.definitionKey = definitionKey;
	}

	@Override
	public final Loggable getLoggable() {
		return getStatement().getLoggable();
	}

	public final Statement getStatement() {
		return this.statement;
	}

	public final DefinitionKey getDefinitionKey() {
		return this.definitionKey;
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
	public final DefinitionTarget firstDeclaration() {
		if (!isDeclaration()) {
			return null;
		}
		return this;
	}

	@Override
	public final DefinitionTarget lastDeclaration() {
		if (!isDeclaration()) {
			return null;
		}
		return this;
	}

	@Override
	public final DefinitionTarget first(DefinitionKey key) {
		if (!key.equals(this.definitionKey)) {
			return null;
		}
		return this;
	}

	@Override
	public final DefinitionTarget last(DefinitionKey key) {
		if (!key.equals(this.definitionKey)) {
			return null;
		}
		return this;
	}

	@Override
	public final Iterator<DefinitionKey> iterator() {
		return singletonList(this.definitionKey).iterator();
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
	Map<DefinitionKey, Entry> targets() {
		return singletonMap(this.definitionKey, new Entry(this));
	}

}
