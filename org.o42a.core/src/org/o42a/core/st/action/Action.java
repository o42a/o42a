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
package org.o42a.core.st.action;

import org.o42a.core.*;
import org.o42a.core.value.LogicalValue;
import org.o42a.util.log.Loggable;


public abstract class Action implements ScopeSpec {

	private final ScopeSpec statement;

	public Action(ScopeSpec statement) {
		this.statement = statement;
	}

	public boolean isAbort() {
		return true;
	}

	public abstract LogicalValue getLogicalValue();

	public final <T> T accept(ActionVisitor<Void, T> visitor) {
		return accept(visitor, null);
	}

	public abstract <P, T> T accept(ActionVisitor<P, T> visitor, P p);

	@Override
	public Loggable getLoggable() {
		return this.statement.getLoggable();
	}

	@Override
	public final CompilerContext getContext() {
		return this.statement.getContext();
	}

	@Override
	public final Scope getScope() {
		return this.statement.getScope();
	}

	@Override
	public void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public void assertSameScope(ScopeSpec other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public void assertCompatibleScope(ScopeSpec other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '[' + this.statement + ']';
	}

}
