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
package org.o42a.core.def.impl;

import static org.o42a.core.ref.Logical.logicalTrue;
import static org.o42a.core.ref.path.PrefixPath.emptyPrefix;

import org.o42a.core.def.CondDef;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;


public final class LogicalCondDef extends CondDef {

	private final Logical logical;

	public LogicalCondDef(Logical logical) {
		super(
				sourceOf(logical),
				logical,
				emptyPrefix(logical.getScope()));
		this.logical = logical;
	}

	private LogicalCondDef(LogicalCondDef prototype, PrefixPath prefix) {
		super(prototype, prefix);
		this.logical = prototype.logical;
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.logical.getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.logical.getScope());
	}

	@Override
	protected final Logical buildLogical() {
		return this.logical;
	}

	@Override
	protected CondDef create(
			PrefixPath prefix,
			PrefixPath additionalPrefix) {
		return new LogicalCondDef(this, prefix);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.logical.resolveAll(resolver);
	}

}
