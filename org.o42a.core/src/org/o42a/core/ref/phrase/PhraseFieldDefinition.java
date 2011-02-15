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

import org.o42a.core.artifact.array.ArrayInitializer;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;


final class PhraseFieldDefinition extends FieldDefinition {

	private final Phrase phrase;
	private FieldDefinition definition;
	private Ref value;

	PhraseFieldDefinition(Phrase phrase) {
		super(phrase, phrase.distribute());
		this.phrase = phrase;
	}

	@Override
	public AscendantsDefinition getAscendants() {
		return getDefinition().getAscendants();
	}

	@Override
	public BlockBuilder getDeclarations() {
		return getDefinition().getDeclarations();
	}

	@Override
	public ArrayInitializer getArrayInitializer() {
		return null;
	}

	@Override
	public Ref getValue() {
		if (this.value != null) {
			return this.value;
		}
		return this.value = this.phrase.toRef();
	}

	@Override
	public FieldDefinition reproduce(Reproducer reproducer) {
		return getDefinition().reproduce(reproducer);
	}

	@Override
	public String toString() {
		return this.phrase.toString();
	}

	private final FieldDefinition getDefinition() {
		if (this.definition != null) {
			return this.definition;
		}

		final MainPhraseContext mainContext = this.phrase.getMainContext();

		if (!mainContext.createsObject()) {
			return this.definition =
				valueDefinition(mainContext.standaloneRef());
		}

		return this.definition = fieldDefinition(
				this,
				mainContext.getAscendants(),
				mainContext.getInstances()[0].getDefinition());
	}

}
