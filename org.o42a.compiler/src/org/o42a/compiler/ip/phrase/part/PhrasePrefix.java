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
package org.o42a.compiler.ip.phrase.part;

import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueStructFinder;


public final class PhrasePrefix extends PhrasePart {

	private AscendantsDefinition ascendants;

	public PhrasePrefix(LocationInfo location, Phrase phrase) {
		super(location, phrase, null);
		this.ascendants = new AscendantsDefinition(this, phrase.distribute());
	}

	public final TypeRef getAncestor() {
		return getAscendants().getAncestor();
	}

	public final PhrasePrefix setAncestor(TypeRef ancestor) {
		this.ascendants = this.ascendants.setAncestor(ancestor);
		return this;
	}

	public final ValueStructFinder getValueStruct() {
		return this.ascendants.getValueStruct();
	}

	public final PhrasePrefix setValueStruct(ValueStructFinder valueStruct) {
		this.ascendants = this.ascendants.setValueStruct(valueStruct);
		return this;
	}

	public final StaticTypeRef[] getSamples() {
		return getAscendants().getSamples();
	}

	public void addSamples(StaticTypeRef... samples) {
		this.ascendants = this.ascendants.addSamples(samples);
	}

	public final AscendantsDefinition getAscendants() {
		return this.ascendants;
	}

	public String phraseString() {

		final StringBuilder out = new StringBuilder();
		out.append("<[");
		PhrasePart part = this;

		for (;;) {
			out.append(part);
			part = part.getFollowing();
			if (part == null) {
				break;
			}
			out.append(' ');
		}
		out.append("]>");

		return out.toString();
	}

	public void append(PhraseContinuation next) {
		setFollowing(next);
	}

	@Override
	public String toString() {
		return this.ascendants.toString();
	}

}
