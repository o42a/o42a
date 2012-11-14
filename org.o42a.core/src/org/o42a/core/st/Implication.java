/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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

import org.o42a.core.*;
import org.o42a.core.object.def.Def;
import org.o42a.core.object.def.DefTarget;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.log.Loggable;


public abstract class Implication<L extends Implication<L>>
		implements PlaceInfo {

	private final Statement statement;

	public Implication(Statement statement) {
		this.statement = statement;
	}

	public final Statement getStatement() {
		return this.statement;
	}

	@Override
	public final CompilerContext getContext() {
		return getStatement().getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return getStatement().getLoggable();
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	@Override
	public final Scope getScope() {
		return getStatement().getScope();
	}

	@Override
	public final ScopePlace getPlace() {
		return getStatement().getPlace();
	}

	@Override
	public final Container getContainer() {
		return getStatement().getContainer();
	}

	/**
	 * Called to replace the statement with another one.
	 *
	 * <p>Supported only for inclusion statement.<p>
	 *
	 * @param statement replacement statement.
	 *
	 * @return replacement definer.
	 */
	public L replaceWith(Statement statement) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Determines a type parameters of the implication's value in the given
	 * scope.
	 *
	 * <p>This value is used by {@link Def#getTypeParameters() value definition}
	 * and for compatibility checks.</p>
	 *
	 * @param scope the scope the type parameters to determine in.
	 *
	 * @return the type parameters or <code>null</code> if this implication
	 * does not produce any values.
	 */
	public abstract TypeParameters<?> typeParameters(Scope scope);

	public abstract Instruction toInstruction(Resolver resolver);

	public abstract DefTarget toTarget(Scope origin);

	public abstract void resolveTargets(TargetResolver resolver, Scope origin);

	@Override
	public final Distributor distribute() {
		return Placed.distribute(this);
	}

	@Override
	public final Distributor distributeIn(Container container) {
		return Placed.distributeIn(this, container);
	}

	@Override
	public final void assertScopeIs(Scope scope) {
		Scoped.assertScopeIs(this, scope);
	}

	@Override
	public final void assertCompatible(Scope scope) {
		Scoped.assertCompatible(this, scope);
	}

	@Override
	public final void assertSameScope(ScopeInfo other) {
		Scoped.assertSameScope(this, other);
	}

	@Override
	public final void assertCompatibleScope(ScopeInfo other) {
		Scoped.assertCompatibleScope(this, other);
	}

	@Override
	public String toString() {
		if (this.statement == null) {
			return super.toString();
		}
		return this.statement.toString();
	}

}
