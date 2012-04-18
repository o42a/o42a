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
package org.o42a.core.ref.impl.logical;

import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineCond;
import org.o42a.core.ref.*;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;


public final class RescopedLogical extends Logical {

	private final Logical logical;
	private final ScopeUpgrade scopeUpgrade;

	public RescopedLogical(Logical logical, ScopeUpgrade scopeUpgrade) {
		super(logical, scopeUpgrade.getFinalScope());
		this.logical = logical;
		this.scopeUpgrade = scopeUpgrade;
	}

	@Override
	public LogicalValue getConstantValue() {
		return this.logical.getConstantValue();
	}

	@Override
	public LogicalValue logicalValue(Resolver resolver) {
		assertCompatible(resolver.getScope());
		return this.logical.logicalValue(this.scopeUpgrade.rescope(resolver));
	}

	@Override
	public Logical reproduce(Reproducer reproducer) {
		getLogger().notReproducible(this);
		return null;
	}

	@Override
	public Logical upgradeScope(ScopeUpgrade scopeUpgrade) {

		final ScopeUpgrade oldUpgrade = this.scopeUpgrade;
		final ScopeUpgrade newUpgrade = oldUpgrade.and(scopeUpgrade);

		if (newUpgrade == oldUpgrade) {
			return this;
		}

		return new RescopedLogical(this.logical, newUpgrade);
	}

	@Override
	public InlineCond inline(Normalizer normalizer, Scope origin) {
		return this.logical.inline(normalizer, origin);
	}

	@Override
	public void write(CodeDirs dirs, HostOp host) {
		assert assertFullyResolved();
		this.logical.write(dirs, host);
	}

	@Override
	public String toString() {
		return this.logical.toString();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		this.logical.resolveAll(this.scopeUpgrade.rescope(resolver));
	}

}
