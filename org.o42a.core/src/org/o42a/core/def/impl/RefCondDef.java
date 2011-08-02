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
package org.o42a.core.def.impl;

import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.CondDef;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;


public final class RefCondDef extends CondDef {

	private final Ref ref;

	public RefCondDef(Obj source, Ref ref) {
		super(source, ref, transparentRescoper(ref.getScope()));
		this.ref = ref;
	}

	RefCondDef(RefCondDef prototype, Rescoper rescoper) {
		super(prototype, rescoper);
		this.ref = prototype.ref;
	}

	@Override
	protected Logical buildPrerequisite() {
		return logicalTrue(this, this.ref.getScope());
	}

	@Override
	protected Logical buildPrecondition() {
		return logicalTrue(this, this.ref.getScope());
	}

	@Override
	protected Logical buildLogical() {
		return this.ref.getLogical();
	}

	@Override
	protected RefCondDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper) {
		return new RefCondDef(this, rescoper);
	}

	@Override
	protected void fullyResolveDef(Resolver resolver) {
		this.ref.resolveValues(resolver);
	}

}
