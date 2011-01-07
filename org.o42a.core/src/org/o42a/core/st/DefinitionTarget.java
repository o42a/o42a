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
package org.o42a.core.st;

import org.o42a.core.Scope;
import org.o42a.core.member.MemberKey;
import org.o42a.core.value.ValueType;


public class DefinitionTarget {

	private final Scope scope;
	private final ValueType<?> expectedType;
	private final MemberKey memberKey;

	public DefinitionTarget(Scope scope) {
		this(scope, null, null);
	}

	public DefinitionTarget(Scope scope, ValueType<?> expectedType) {
		this(scope, expectedType, null);
	}

	public DefinitionTarget(
			Scope scope,
			ValueType<?> expectedType,
			MemberKey memberKey) {
		this.scope = scope;
		this.expectedType = expectedType;
		this.memberKey = memberKey;
	}

	public final Scope getScope() {
		return this.scope;
	}

	public final ValueType<?> getExpectedType() {
		return this.expectedType;
	}

	public final MemberKey getMemberKey() {
		return this.memberKey;
	}

	public final boolean isField() {
		return this.memberKey != null;
	}

	public final boolean isTypeExpected() {
		return this.expectedType != null;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append("StatementScope[");
		out.append(this.scope);
		out.append(']');
		if (this.memberKey != null) {
			out.append(":").append(this.memberKey);
		}

		return out.toString();
	}

}
