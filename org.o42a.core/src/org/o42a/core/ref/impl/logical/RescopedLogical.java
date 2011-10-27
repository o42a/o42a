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
package org.o42a.core.ref.impl.logical;

import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;


public final class RescopedLogical extends Logical {

	private final Logical logical;
	private final PrefixPath prefix;

	public RescopedLogical(Logical logical, PrefixPath prefix) {
		super(logical, prefix.getStart());
		this.logical = logical;
		this.prefix = prefix;
	}

	@Override
	public LogicalValue getConstantValue() {
		return this.logical.getConstantValue();
	}

	@Override
	public LogicalValue logicalValue(Resolver resolver) {
		assertCompatible(resolver.getScope());
		return this.logical.logicalValue(this.prefix.rescope(resolver));
	}

	@Override
	public Logical reproduce(Reproducer reproducer) {
		getLogger().notReproducible(this);
		return null;
	}

	@Override
	public Logical prefixWith(PrefixPath prefix) {

		final PrefixPath oldPrefix = this.prefix;
		final PrefixPath newPrefix = oldPrefix.and(prefix);

		if (newPrefix == oldPrefix) {
			return this;
		}

		return new RescopedLogical(this.logical, newPrefix);
	}

	@Override
	public void write(CodeDirs dirs, HostOp host) {
		assert assertFullyResolved();
		this.logical.write(dirs, this.prefix.write(dirs, host));
	}

	@Override
	public String toString() {
		return this.logical.toString();
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		this.prefix.resolveAll(resolver);
		this.logical.resolveAll(this.prefix.rescope(resolver));
	}

}
