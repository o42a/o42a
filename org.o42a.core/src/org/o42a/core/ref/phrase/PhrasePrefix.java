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

import org.o42a.core.LocationSpec;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.member.field.AscendantsDefinition;


public class PhrasePrefix extends PhrasePart {

	private AscendantsDefinition ascendants;

	PhrasePrefix(LocationSpec location, Phrase phrase) {
		super(location, null);
		this.ascendants = new AscendantsDefinition(this, phrase.distribute());
	}

	public final TypeRef getAncestor() {
		return getAscendants().getAncestor();
	}

	public final StaticTypeRef[] getSamples() {
		return getAscendants().getSamples();
	}

	public final AscendantsDefinition getAscendants() {
		return this.ascendants;
	}

	public String phraseString() {

		final StringBuilder out = new StringBuilder();
		PhrasePart part = this;

		for (;;) {
			out.append(part);
			part = part.getFollowing();
			if (part == null) {
				break;
			}
			out.append(' ');
		}

		return out.toString();
	}

	@Override
	public String toString() {
		return this.ascendants.toString();
	}

	@Override
	protected PhraseName name(LocationSpec location, String name) {
		getLogger().prohibitedPhraseName(location);
		return null;
	}

	PhrasePrefix setAncestor(TypeRef ancestor) {
		this.ascendants = this.ascendants.setAncestor(ancestor);
		return this;
	}

	void addSamples(StaticTypeRef... samples) {
		this.ascendants = this.ascendants.addSamples(samples);
	}

}
