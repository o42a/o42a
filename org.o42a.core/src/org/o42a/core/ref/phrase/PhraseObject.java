/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.DefinedObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.NewObjectEx;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.DeclarativeBlock;


class PhraseObject extends DefinedObject {

	private final MainPhraseContext mainContext;
	private final AscendantsDefinition ascendants;

	private PhraseObject(
			MainPhraseContext mainContext,
			Distributor distributor,
			AscendantsDefinition ascendants) {
		super(mainContext.getPhrase(), distributor);
		this.mainContext = mainContext;
		this.ascendants = ascendants;
	}

	private PhraseObject(Scope scope, PhraseObject sample) {
		super(scope, sample);
		this.mainContext = sample.mainContext;
		this.ascendants = sample.ascendants;
	}

	@Override
	public String toString() {
		return this.mainContext.getPhrase().toString();
	}

	@Override
	protected Ascendants buildAscendants() {
		return this.ascendants.updateAscendants(new Ascendants(this));
	}

	@Override
	protected void buildDefinition(DeclarativeBlock definition) {

		final BlockBuilder definitionBuilder =
			this.mainContext.getInstances()[0].getDefinition();

		definitionBuilder.buildBlock(definition);
	}

	static final class Ex extends NewObjectEx {

		private final MainPhraseContext mainContext;
		private final AscendantsDefinition ascendants;

		Ex(MainPhraseContext mainContext) {
			super(
					mainContext.getPhrase(),
					mainContext.getPhrase().distribute());
			this.mainContext = mainContext;
			this.ascendants = mainContext.getAscendants();
			this.ascendants.assertCompatibleScope(this);
		}

		private Ex(
				LocationSpec location,
				Distributor distributor,
				MainPhraseContext mainContext,
				AscendantsDefinition ascendants) {
			super(location, distributor);
			this.mainContext = mainContext;
			this.ascendants = ascendants;
		}

		@Override
		protected Obj createObject() {
			return new PhraseObject(
					this.mainContext,
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

			return new Ex(
					this,
					reproducer.distribute(),
					this.mainContext,
					ascendants);
		}

	}

}
