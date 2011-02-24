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
import org.o42a.core.Placed;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.sentence.BlockBuilder;


public class Phrase extends Placed {

	private PhrasePrefix prefix;
	private PhrasePart last;
	private MainPhraseContext mainContext;

	public Phrase(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	public final PhrasePrefix getPrefix() {
		return this.prefix;
	}

	public final TypeRef getAncestor() {
		return this.prefix != null ? this.prefix.getAncestor() : null;
	}

	public final Phrase setImpliedAncestor(LocationInfo location) {
		if (this.prefix == null) {
			this.last = this.prefix = new PhrasePrefix(location, this);
		}
		return this;
	}

	public final Phrase setAncestor(TypeRef ancestor) {
		if (this.prefix == null) {
			this.last = this.prefix = new PhrasePrefix(ancestor, this);
		}
		this.prefix.setAncestor(ancestor);
		return this;
	}

	public final StaticTypeRef[] getSamples() {
		if (this.prefix == null) {
			return new StaticTypeRef[0];
		}
		return this.prefix.getSamples();
	}

	public final Phrase addSamples(StaticTypeRef... samples) {
		this.prefix.addSamples(samples);
		return this;
	}

	public final Phrase name(LocationInfo location, String name) {
		return append(this.last.name(location, name));
	}

	public final Phrase emptyArgument(LocationInfo location) {
		return append(this.last.argument(location, null));
	}

	public final Phrase argument(Ref value) {
		value.assertSameScope(this);
		return append(this.last.argument(value, value));
	}

	public final Phrase string(LocationInfo location, String string) {
		return append(this.last.string(location, string));
	}

	public final Phrase declarations(BlockBuilder declarations) {
		return append(this.last.declarations(declarations));
	}

	public final Phrase imperative(BlockBuilder imperatives) {
		return append(this.last.imperative(imperatives));
	}

	public final Ref toRef() {
		return new PhraseEx(this);
	}

	@Override
	public String toString() {
		return this.prefix.phraseString();
	}

	final MainPhraseContext getMainContext() {
		if (this.mainContext != null) {
			return this.mainContext;
		}
		return this.mainContext = new MainPhraseContext(this);
	}

	private final Phrase append(PhrasePart part) {
		this.last = part;
		return this.last != null ? this : null;
	}

}
