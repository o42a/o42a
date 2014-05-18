/*
    Compiler Commons
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.common.phrase;

import org.o42a.core.member.field.*;
import org.o42a.core.object.type.Ascendants;


final class PhraseFieldDefinition extends FieldDefinition {

	private final Phrase phrase;
	private FieldDefinition definition;

	PhraseFieldDefinition(Phrase phrase) {
		super(phrase, phrase.distribute());
		this.phrase = phrase;
	}

	@Override
	public void init(Field field, Ascendants implicitAscendants) {
		getDefinition().init(field, implicitAscendants);
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {

		final MainPhraseContext mainContext = this.phrase.getMainContext();

		return mainContext.getAscendants().getDefinitionTarget();
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		getDefinition().defineObject(definer);
	}

	@Override
	public void overrideObject(ObjectDefiner definer) {
		getDefinition().overrideObject(definer);
	}

	@Override
	public void defineMacro(MacroDefiner definer) {
		getDefinition().defineMacro(definer);
	}

	@Override
	public String toString() {
		return "FieldDefinition[" + this.phrase + ']';
	}

	private final FieldDefinition getDefinition() {
		if (this.definition != null) {
			return this.definition;
		}

		final MainPhraseContext mainContext = this.phrase.getMainContext();

		return this.definition = mainContext.getAscendants().fieldDefinition(
				this,
				mainContext.getInstances()[0].getDefinition()::definitions);
	}

}
