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
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.sentence.ImperativeBlock;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.util.log.Loggable;


public abstract class Action implements ScopeInfo {

	private final ScopeInfo statement;

	public Action(ScopeInfo statement) {
		this.statement = statement;
	}

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

	public abstract boolean isAbort();

	public abstract LogicalValue getLogicalValue();

	public abstract Value<?> getValue();

	public abstract Action toInitialLogicalValue();

	public abstract LoopAction toLoopAction(ImperativeBlock block);

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

	static final boolean blockMatchesName(
			ImperativeBlock block,
			String blockName) {
		return blockName == null || blockName.equals(block.getName());
	}

}
