/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.object.link.impl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.object.ConstructionMode.RUNTIME_CONSTRUCTION;
import static org.o42a.core.object.def.Definitions.emptyDefinitions;

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.object.ConstructionMode;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.link.ObjectLink;
import org.o42a.core.object.type.Ascendants;


public final class RuntimeLinkTarget extends Obj {

	private final ObjectLink link;

	public RuntimeLinkTarget(ObjectLink link) {
		super(link, link.distributeIn(link.getScope().getContainer()));
		this.link = link;
	}

	@Override
	public final ObjectLink getDereferencedLink() {
		return this.link;
	}

	@Override
	public ConstructionMode getConstructionMode() {
		return RUNTIME_CONSTRUCTION;
	}

	@Override
	public String toString() {
		return this.link.toString();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(this.link.getTypeRef());
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return emptyDefinitions(this, getScope());
	}

	@Override
	protected Obj findObjectIn(Scope enclosing) {
		return this.link.findIn(enclosing).getTarget();
	}

	@Override
	protected void fullyResolve() {
		super.fullyResolve();

		final Artifact<?> target =
				this.link.getTargetRef().artifact(dummyUser());

		if (target != null) {
			target.materialize().type().wrapBy(type());
		}
	}

	@Override
	protected void fullyResolveDefinitions() {
		super.fullyResolveDefinitions();

		final Artifact<?> target =
				this.link.getTargetRef().artifact(dummyUser());

		if (target != null) {
			target.materialize().value().wrapBy(value());
		}
	}

	@Override
	protected ObjectIR createIR(Generator generator) {
		return this.link.getTargetRef().artifact(dummyUser())
				.toObject().ir(generator);
	}

}
