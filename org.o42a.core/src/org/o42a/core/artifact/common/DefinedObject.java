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
package org.o42a.core.artifact.common;

import static org.o42a.core.st.StatementEnv.objectEnv;

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.st.sentence.DeclarativeBlock;


public abstract class DefinedObject extends PlainObject {

	private ObjectMemberRegistry memberRegistry;
	private DeclarativeBlock definition;
	private boolean definitionBuilt;

	public DefinedObject(LocationInfo location, Distributor enclosing) {
		super(location, enclosing);
	}

	public DefinedObject(ObjectScope scope) {
		super(scope);
	}

	public DefinedObject(Scope scope) {
		super(scope);
	}

	protected DefinedObject(Scope scope, Obj sample) {
		super(scope, sample);
	}

	public DeclarativeBlock getDefinition() {
		if (!this.definitionBuilt) {
			buildDefinition(definition());
			this.definitionBuilt = true;
		}
		return this.definition;
	}

	@Override
	protected void postResolve() {
		getDefinition();
		super.postResolve();
	}

	@Override
	protected Definitions explicitDefinitions() {
		return getDefinition().define(getScope());
	}

	protected abstract void buildDefinition(DeclarativeBlock definition);

	@Override
	protected void declareMembers(ObjectMembers members) {
		getMemberRegistry().registerMembers(members);
	}

	@Override
	protected void updateMembers() {

		final DeclarativeBlock definition = getDefinition();

		if (definition != null) {
			definition.executeInstructions();
		}
	}

	private DeclarativeBlock definition() {
		if (this.definition != null) {
			return this.definition;
		}

		final DeclarativeBlock definition =
			new DeclarativeBlock(this, this, getMemberRegistry());

		definition.setEnv(objectEnv(this));

		return this.definition = definition;
	}

	private ObjectMemberRegistry getMemberRegistry() {
		if (this.memberRegistry == null) {
			this.memberRegistry = new ObjectMemberRegistry(this);
		}
		return this.memberRegistry;
	}

}
