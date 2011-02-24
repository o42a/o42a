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
package org.o42a.core.ref.phrase;

import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.artifact.common.DefinedObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.DeclarativeBlock;


final class ClauseInstantiation extends Wrap {

	private final ClauseInstance instance;
	private final boolean topLevel;

	ClauseInstantiation(
			ClauseInstance instance,
			Distributor distributor,
			boolean topLevel) {
		super(instance.getLocation(), distributor);
		this.instance = instance;
		this.topLevel = topLevel;
	}

	@Override
	public String toString() {
		return this.instance.toString();
	}

	@Override
	protected Ref resolveWrapped() {

		final AscendantsDefinition ascendants =
			this.instance.getContext().ascendants(this, distribute());

		return new ClauseConstructor(
				this.instance,
				distribute(),
				this.topLevel,
				ascendants);
	}

	private static final class ClauseConstructor extends ObjectConstructor {

		private final ClauseInstance instance;
		private final AscendantsDefinition ascendants;
		private final boolean topLevel;

		ClauseConstructor(
				ClauseInstance instance,
				Distributor distributor,
				boolean topLevel,
				AscendantsDefinition ascendants) {
			super(instance.getLocation(), distributor);
			this.instance = instance;
			this.ascendants = ascendants;
			this.topLevel = topLevel;
		}

		@Override
		public TypeRef ancestor(LocationInfo location) {
			return this.ascendants.getAncestor();
		}

		@Override
		public FieldDefinition toFieldDefinition() {

			final Phrase phrase = this.instance.getContext().getPhrase();

			if (this.topLevel) {
				return new TopInstanceDefinition(
						phrase.getMainContext(),
						super.toFieldDefinition());
			}

			return super.toFieldDefinition();
		}

		@Override
		protected Obj createObject() {
			return new InstantiationObject(
					this.instance,
					distribute(),
					this.ascendants);
		}

		@Override
		public Ref reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());

			final AscendantsDefinition ascendants =
				this.ascendants.reproduce(reproducer);

			if (ascendants == null) {
				return null;
			}

			return new ClauseConstructor(
					this.instance,
					reproducer.distribute(),
					this.topLevel,
					ascendants);
		}

	}

	private static final class TopInstanceDefinition extends FieldDefinition {

		private final FieldDefinition definition;
		private final MainPhraseContext mainContext;

		public TopInstanceDefinition(
				MainPhraseContext mainContext,
				FieldDefinition definition) {
			super(definition, definition.distribute());
			this.mainContext = mainContext;
			this.definition = definition;
		}

		@Override
		public ArtifactKind<?> determineArtifactKind() {
			return this.definition.determineArtifactKind();
		}

		@Override
		public void defineObject(ObjectDefiner definer) {
			this.mainContext.setImplicitAscendants(
					definer.getImplicitAscendants());
			this.definition.defineObject(definer);
		}

		@Override
		public AscendantsDefinition getAscendants() {
			return this.definition.getAscendants();
		}

		@Override
		public ArrayInitializer getArrayInitializer() {
			return this.definition.getArrayInitializer();
		}

		@Override
		public Ref getValue() {
			return this.definition.getValue();
		}

		@Override
		public FieldDefinition reproduce(Reproducer reproducer) {
			return this.definition.reproduce(reproducer);
		}

		@Override
		public String toString() {
			return this.definition.toString();
		}

	}

	private static final class InstantiationObject extends DefinedObject {

		private final ClauseInstance instance;
		private final AscendantsDefinition ascendants;

		InstantiationObject(
				ClauseInstance instance,
				Distributor enclosing,
				AscendantsDefinition ascendants) {
			super(instance.getLocation(), enclosing);
			this.instance = instance;
			this.ascendants = ascendants;
		}

		@Override
		public String toString() {
			return this.instance.toString();
		}

		@Override
		protected Ascendants buildAscendants() {
			return this.ascendants.updateAscendants(new Ascendants(this));
		}

		@Override
		protected void buildDefinition(DeclarativeBlock definition) {
			this.instance.getDefinition().buildBlock(definition);
		}

	}

}
