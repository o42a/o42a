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

import org.o42a.core.Location;
import org.o42a.core.LocationInfo;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.sentence.BlockBuilder;


public abstract class PhrasePart extends Location {

	private final PhrasePart preceding;
	private PhraseContinuation following;

	PhrasePart(LocationInfo location, PhrasePart preceding) {
		super(location);
		this.preceding = preceding;
	}

	public final PhrasePart getPreceding() {
		return this.preceding;
	}

	public final PhraseContinuation getFollowing() {
		return this.following;
	}

	protected PhraseName name(LocationInfo location, String name) {
		return setFollowing(new PhraseName(location, this, name));
	}

	protected PhraseArgument argument(LocationInfo location, Ref value) {
		return setValue(new PhraseArgument(this, location, value));
	}

	protected PhraseString string(LocationInfo location, String string) {
		return setValue(new PhraseString(location, this, string));
	}

	protected PhraseDeclarations declarations(BlockBuilder declarations) {
		return setValue(new PhraseDeclarations(this, declarations));
	}

	protected PhraseImperative imperative(BlockBuilder imperatives) {
		return setValue(new PhraseImperative(this, imperatives));
	}

	protected <P extends PhraseContinuation> P setFollowing(P following) {
		assert this.following == null :
			this + " already followed by " + this.following;
		this.following = following;
		return following;
	}

	protected <P extends ValuedPhrasePart> P setValue(P following) {
		return setFollowing(following);
	}

}
