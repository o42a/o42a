/*
    Compiler
    Copyright (C) 2010-2013 Ruslan Lopatin

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

import org.o42a.ast.expression.BinaryNode;
import org.o42a.ast.expression.UnaryNode;
import org.o42a.ast.statement.AssignmentNode;
import org.o42a.compiler.ip.phrase.ref.Phrase;
import org.o42a.compiler.ip.ref.array.ArrayConstructor;
import org.o42a.core.ref.Ref;
import org.o42a.core.source.Located;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.util.string.Name;


public abstract class PhrasePart extends Located {

	private final Phrase phrase;
	private final PhrasePart preceding;
	private PhraseContinuation following;

	PhrasePart(LocationInfo location, Phrase phrase, PhrasePart preceding) {
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

	public final SuffixedByPhrase suffix(LocationInfo location, Ref prefix) {
		return setFollowing(new SuffixedByPhrase(location, this, prefix));
	}

	public final PhraseName name(LocationInfo location, Name name) {
		return setFollowing(new PhraseName(location, this, name));
	}

	public final PhraseArgument argument(LocationInfo location, Ref value) {
		return setFollowing(new PhraseArgument(location, this, value));
	}

	public final PhraseArray array(ArrayConstructor array) {
		return setFollowing(new PhraseArray(this, array));
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

	public final UnaryPhrasePart unary(UnaryNode node) {
		return setFollowing(new UnaryPhrasePart(node, this));
	}

	public final BinaryPhrasePart binary(BinaryNode node) {
		return setFollowing(new BinaryPhrasePart(node, this));
	}

	public final PhraseAssignment assign(AssignmentNode node) {
		return setFollowing(new PhraseAssignment(node, this));
	}

	public final OperandPhrasePart operand(Ref value) {
		return setFollowing(new OperandPhrasePart(value, this));
	}

	<P extends PhraseContinuation> P setFollowing(P following) {
		assert this.following == null :
			this + " already followed by " + this.following;
		this.following = following;
		return following;
	}

}
