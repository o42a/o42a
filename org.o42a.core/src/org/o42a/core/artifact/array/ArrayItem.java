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
package org.o42a.core.artifact.array;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.artifact.array.impl.ArrayItemContainer;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberContainer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.log.Loggable;


public class ArrayItem extends AbstractScope {

	private final Ref ref;
	private final Obj owner;
	private ArrayItemContainer container;

	public ArrayItem(Ref ref) {
		this.ref = ref;
		this.owner = ref.getScope().toObject();
		assert this.owner != null :
			"Enclosing scope is not object: " + ref.getScope();
	}

	@Override
	public final ScopePlace getPlace() {
		return this.owner.getPlace();
	}

	@Override
	public final CompilerContext getContext() {
		return this.ref.getContext();
	}

	@Override
	public final Loggable getLoggable() {
		return this.ref.getLoggable();
	}

	@Override
	public MemberContainer getContainer() {
		if (this.container != null) {
			return this.container;
		}
		return this.container = new ArrayItemContainer(this);
	}

	@Override
	public Container getEnclosingContainer() {
		return this.ref.getContainer();
	}

	@Override
	public Path getEnclosingScopePath() {
		return null;
	}

	@Override
	public Member toMember() {
		return null;
	}

	@Override
	public boolean derivedFrom(Scope other) {
		if (this == other) {
			return true;
		}
		return getArtifact().materialize().type().derivedFrom(
				other.getArtifact().materialize().type());
	}

	@Override
	public ScopeIR ir(Generator generator) {
		// TODO Auto-generated method stub
		return null;
	}

}
