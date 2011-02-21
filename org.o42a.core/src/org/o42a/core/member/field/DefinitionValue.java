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
package org.o42a.core.member.field;

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.common.DefinedObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.DeclarativeBlock;


public class DefinitionValue extends ObjectConstructor {

	private final AscendantsDefinition ascendants;
	private final BlockBuilder declarations;

	public DefinitionValue(
			LocationInfo location,
			Distributor distributor,
			AscendantsDefinition ascendants,
			BlockBuilder declarations) {
		super(location, distributor);
		this.ascendants = ascendants;
		this.declarations = declarations;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return this.ascendants.getAncestor();
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final AscendantsDefinition ascendants =
			this.ascendants.reproduce(reproducer);

		if (ascendants == null) {
			return null;
		}

		return new DefinitionValue(
				this,
				reproducer.distribute(),
				ascendants,
				this.declarations);
	}

	@Override
	protected Obj createObject() {
		return new ValueObject(
				this,
				distribute(),
				this.ascendants,
				this.declarations);
	}

	private static final class ValueObject extends DefinedObject {

		private final AscendantsDefinition ascendants;
		private final BlockBuilder declarations;

		ValueObject(
				LocationInfo location,
				Distributor enclosing,
				AscendantsDefinition ascendants,
				BlockBuilder declarations) {
			super(location, enclosing);
			this.ascendants = ascendants;
			this.declarations = declarations;
		}

		@Override
		protected Ascendants buildAscendants() {
			return this.ascendants.updateAscendants(new Ascendants(this));
		}

		@Override
		protected void buildDefinition(DeclarativeBlock definition) {
			this.declarations.buildBlock(definition);
		}

	}

}
