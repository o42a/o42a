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
package org.o42a.core.ref.common;

import org.o42a.core.Distributor;
import org.o42a.core.artifact.common.DefinedObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.DeclarativeBlock;


public class Call extends org.o42a.core.ref.path.ObjectConstructor {

	private final AscendantsDefinition ascendants;
	private final BlockBuilder definitions;

	public Call(
			LocationInfo location,
			Distributor distributor,
			AscendantsDefinition ascendants,
			BlockBuilder definitions) {
		super(location, distributor);
		this.ascendants = ascendants;
		this.definitions = definitions;
	}

	public final AscendantsDefinition getAscendants() {
		return this.ascendants;
	}

	public final BlockBuilder getDefinitions() {
		return this.definitions;
	}

	@Override
	public TypeRef ancestor(LocationInfo location) {
		return this.ascendants.getAncestor();
	}

	@Override
	public FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return FieldDefinition.fieldDefinition(
				this,
				this.ascendants,
				this.definitions);
	}

	@Override
	public Call reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final AscendantsDefinition ascendants =
				this.ascendants.reproduce(reproducer);

		if (ascendants == null) {
			return null;
		}

		return new Call(
				this,
				reproducer.distribute(),
				ascendants,
				this.definitions);
	}

	@Override
	public String toString() {
		if (this.ascendants == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append(this.ascendants).append('(');
		if (this.definitions != null) {
			out.append(this.definitions);
		}
		out.append(')');

		return out.toString();
	}

	@Override
	protected Obj createObject() {
		return new CallObject(
				this,
				distribute(),
				this.ascendants,
				this.definitions);
	}

	private static final class CallObject extends DefinedObject {

		private final AscendantsDefinition ascendants;
		private final BlockBuilder definitions;

		CallObject(
				LocationInfo location,
				Distributor enclosing,
				AscendantsDefinition ascendants,
				BlockBuilder definitions) {
			super(location, enclosing);
			this.ascendants = ascendants;
			this.definitions = definitions;
		}

		@Override
		public String toString() {
			if (this.ascendants == null) {
				return super.toString();
			}

			final StringBuilder out = new StringBuilder();

			out.append(this.ascendants).append('(');
			if (this.definitions != null) {
				out.append(this.definitions);
			}
			out.append(')');

			return out.toString();
		}

		@Override
		protected Ascendants buildAscendants() {
			return this.ascendants.updateAscendants(new Ascendants(this));
		}

		@Override
		protected void buildDefinition(DeclarativeBlock definition) {
			this.definitions.buildBlock(definition);
		}

	}

}
