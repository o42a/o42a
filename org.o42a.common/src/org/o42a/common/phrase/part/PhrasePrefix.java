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
package org.o42a.common.phrase.part;

import org.o42a.common.phrase.Phrase;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ObjectTypeParameters;


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

	public final ObjectTypeParameters getTypeParameters() {
		return this.ascendants.getTypeParameters();
	}

	public final PhrasePrefix setTypeParameters(
			ObjectTypeParameters typeParameters) {
		this.ascendants = this.ascendants.setTypeParameters(typeParameters);
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

	public void append(PhraseContinuation next) {
		setFollowing(next);
	}

	@Override
	public String toString() {
		return this.ascendants.toString();
	}

}
