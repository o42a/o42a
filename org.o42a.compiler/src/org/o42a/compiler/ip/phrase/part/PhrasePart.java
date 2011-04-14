/*
    Compiler
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
package org.o42a.compiler.ip.phrase.part;

import org.o42a.ast.expression.UnaryNode;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.core.Location;
import org.o42a.core.LocationInfo;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.BlockBuilder;


public abstract class PhrasePart extends Location {

	private final Phrase phrase;
	private final PhrasePart preceding;
	private PhraseContinuation following;

	PhrasePart(
			LocationInfo location,
			Phrase phrase,
			PhrasePart preceding) {
		super(location);
		this.phrase = phrase;
		this.preceding = preceding;
	}

	public final Phrase getPhrase() {
		return this.phrase;
	}

	public final PhrasePart getPreceding() {
		return this.preceding;
	}

	public final PhraseContinuation getFollowing() {
		return this.following;
	}

	public final PhraseName name(LocationInfo location, String name) {
		return setFollowing(new PhraseName(location, this, name));
	}

	public final PhraseArgument argument(LocationInfo location, Ref value) {
		return setFollowing(new PhraseArgument(this, location, value));
	}

	public final PhraseString string(LocationInfo location, String string) {
		return setFollowing(new PhraseString(location, this, string));
	}

	public final PhraseDeclarations declarations(BlockBuilder declarations) {
		return setFollowing(new PhraseDeclarations(this, declarations));
	}

	public final PhraseImperative imperative(BlockBuilder imperatives) {
		return setFollowing(new PhraseImperative(this, imperatives));
	}

	public final UnaryPhrasePart plus(UnaryNode node) {
		return setFollowing(new UnaryPhrasePart(node, this));
	}

	private <P extends PhraseContinuation> P setFollowing(P following) {
		assert this.following == null :
			this + " already followed by " + this.following;
		this.following = following;
		return following;
	}

}
