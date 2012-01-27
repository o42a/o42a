/*
    Compiler
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
package org.o42a.compiler.ip.phrase.ref;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.DefinedObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.path.ObjectConstructor;
import org.o42a.core.ref.path.PathReproducer;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.DeclarativeBlock;


final class ClauseInstantiation extends ObjectConstructor {

	private final ClauseInstance instance;
	private AscendantsDefinition ascendants;

	ClauseInstantiation(
			ClauseInstance instance,
			Distributor distributor) {
		super(
				new Location(distributor.getContext(), instance.getLocation()),
				distributor);
		this.instance = instance;
	}

	private ClauseInstantiation(
			ClauseInstance instance,
			Distributor distributor,
			AscendantsDefinition ascendants) {
		super(
				new Location(distributor.getContext(), instance.getLocation()),
				distributor);
		this.instance = instance;
		this.ascendants = ascendants;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return getAscendants().getAncestor();
	}

	@Override
	public ClauseInstantiation reproduce(PathReproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final AscendantsDefinition ascendants =
				this.ascendants.reproduce(reproducer.getReproducer());

		if (ascendants == null) {
			return null;
		}

		return new ClauseInstantiation(
				this.instance,
				reproducer.distribute(),
				ascendants);
	}

	@Override
	public String toString() {
		return this.instance.toString();
	}

	@Override
	protected Obj createObject() {
		return new InstantiationObject(this);
	}

	private AscendantsDefinition getAscendants() {
		if (this.ascendants != null) {
			return this.ascendants;
		}
		return this.ascendants =
				this.instance.getContext().ascendants(this, distribute());
	}

	private static final class InstantiationObject extends DefinedObject {

		private final ClauseInstantiation instantiation;

		InstantiationObject(ClauseInstantiation instantiation) {
			super(
					instantiation.instance.getLocation(),
					instantiation.distribute());
			this.instantiation = instantiation;
		}

		@Override
		public String toString() {
			return this.instantiation.toString();
		}

		@Override
		protected Ascendants buildAscendants() {
			return this.instantiation.getAscendants().updateAscendants(
					new Ascendants(this));
		}

		@Override
		protected void buildDefinition(DeclarativeBlock definition) {
			this.instantiation.instance.getDefinition().buildBlock(definition);
		}

		@Override
		protected Obj findObjectIn(Scope enclosing) {
			return this.instantiation.resolve(enclosing);
		}

	}

}
