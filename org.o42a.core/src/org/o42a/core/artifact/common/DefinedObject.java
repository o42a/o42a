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

import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.st.DefinitionTarget;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.Declaratives;


public abstract class DefinedObject extends PlainObject {

	private ObjectMemberRegistry memberRegistry;
	private DeclarativeBlock definition;
	private boolean definitionBuilt;

	public DefinedObject(LocationSpec location, Distributor enclosing) {
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

	protected DeclarativeBlock createDeclarativeBlock() {
		return new DeclarativeBlock(this, this, getMemberRegistry());
	}

	@Override
	protected void postResolve() {
		getDefinition();
		super.postResolve();
	}

	@Override
	protected Definitions explicitDefinitions() {

		final Block<Declaratives> definition = getDefinition();

		if (!definition.getKind().hasLogicalValue()) {
			return null;
		}

		return definition.define(
				new DefinitionTarget(getScope(), getValueType()));
	}

	protected abstract void buildDefinition(DeclarativeBlock definition);

	@Override
	protected void declareMembers(ObjectMembers members) {

		final DeclarativeBlock definition = getDefinition();

		if (definition != null) {
			definition.executeInstructions();
		}

		getMemberRegistry().registerMembers(members);
	}

	private DeclarativeBlock definition() {
		if (this.definition != null) {
			return this.definition;
		}
		return this.definition = createDeclarativeBlock();
	}

	private ObjectMemberRegistry getMemberRegistry() {
		if (this.memberRegistry == null) {
			this.memberRegistry = new ObjectMemberRegistry(this);
		}
		return this.memberRegistry;
	}

}
