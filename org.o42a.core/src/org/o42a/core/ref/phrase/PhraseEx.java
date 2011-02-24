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

import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.Wrap;
import org.o42a.core.st.Reproducer;


class PhraseEx extends Wrap {

	private final Phrase phrase;

	PhraseEx(Phrase phrase) {
		super(phrase, phrase.distribute());
		this.phrase = phrase;
	}

	public Phrase getPhrase() {
		return this.phrase;
	}

	@Override
	public FieldDefinition toFieldDefinition() {
		return new Definition(this.phrase, super.toFieldDefinition());
	}

	@Override
	protected Ref resolveWrapped() {

		final MainPhraseContext context = this.phrase.getMainContext();

		if (!context.createsObject()) {
			return context.standaloneRef();
		}

		return new PhraseObject.Ex(this);
	}

	@Override
	public String toString() {
		return this.phrase.toString();
	}

	private static final class Definition extends FieldDefinition {

		private final Phrase phrase;
		private final FieldDefinition definition;

		Definition(Phrase phrase, FieldDefinition definition) {
			super(definition, definition.distribute());
			this.phrase = phrase;
			this.definition = definition;
		}

		@Override
		public ArtifactKind<?> determineArtifactKind() {
			return this.definition.determineArtifactKind();
		}

		@Override
		public void defineObject(ObjectDefiner definer) {
			this.phrase.getMainContext().setImplicitAscendants(
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
			if (this.definition == null) {
				return super.toString();
			}
			return this.definition.toString();
		}

	}

}
