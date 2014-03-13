/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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

import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.Scoped;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.st.DefValue;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.value.Condition;
import org.o42a.core.value.Value;


public abstract class Action implements ScopeInfo {

	private final ScopeInfo statement;

	public Action(ScopeInfo statement) {
		this.statement = statement;
	}

	@Override
	public final Location getLocation() {
		return this.statement.getLocation();
	}

	public final CompilerContext getContext() {
		return getLocation().getContext();
	}

	@Override
	public final Scope getScope() {
		return this.statement.getScope();
	}

	public abstract boolean isAbort();

	public abstract Condition getCondition();

	public abstract Value<?> getValue();

	public abstract LoopAction toLoopAction(Block<?> block);

	public abstract DefValue toDefValue();

	@Override
	public void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '[' + this.statement + ']';
	}

}
