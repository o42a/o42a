/*
    Compiler Commons
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
package org.o42a.common.phrase.part;

import org.o42a.common.phrase.Phrase;
import org.o42a.core.ref.RefBuilder;
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

	public final SuffixedByPhrase suffix(
			LocationInfo location,
			RefBuilder prefix) {
		return setFollowing(new SuffixedByPhrase(location, this, prefix));
	}

	public final PhraseName name(LocationInfo location, Name name) {
		return setFollowing(new PhraseName(location, this, name));
	}

	public final PhraseArgument argument(
			LocationInfo location,
			RefBuilder value) {
		return setFollowing(new PhraseArgument(location, this, value));
	}

	public final PhraseInitializer initializer(
			LocationInfo location,
			RefBuilder value) {
		return setFollowing(new PhraseInitializer(location, this, value));
	}

	public final PhraseArray array(RefBuilder array) {
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

	public final IntervalBound interval(
			LocationInfo leftLocation,
			RefBuilder leftBound,
			boolean leftOpen,
			LocationInfo rightLocation,
			RefBuilder rightBound,
			boolean rightOpen) {

		final IntervalBound left = new IntervalBound(
				leftLocation,
				this,
				leftBound,
				leftOpen,
				rightOpen,
				true);

		setFollowing(left);

		final IntervalBound right = new IntervalBound(
				rightLocation,
				left,
				rightBound,
				leftOpen,
				rightOpen,
				false);

		return left.setFollowing(right);
	}

	public final HalfBoundedInterval halfBoundedInterval(
			LocationInfo location,
			RefBuilder bound,
			boolean open,
			boolean leftBounded) {

		final HalfBoundedInterval interval = new HalfBoundedInterval(
				location,
				this,
				bound,
				open,
				leftBounded);

		return setFollowing(interval);
	}

	public final UnboundedInterval unboundedInterval(LocationInfo location) {
		return setFollowing(new UnboundedInterval(location, this));
	}

	public final UnaryPhrasePart unary(
			LocationInfo location,
			UnaryPhraseOperator operator) {
		return setFollowing(new UnaryPhrasePart(location, operator, this));
	}

	public final BinaryPhrasePart binary(
			LocationInfo location,
			BinaryPhraseOperator operator,
			RefBuilder rightOperand) {
		return setFollowing(
				new BinaryPhrasePart(location, operator, this, rightOperand));
	}

	public final PhraseAssignment assign(
			LocationInfo location,
			RefBuilder value) {
		return setFollowing(new PhraseAssignment(location, this, value));
	}

	<P extends PhraseContinuation> P setFollowing(P following) {
		assert this.following == null :
			this + " already followed by " + this.following;
		this.following = following;
		return following;
	}

}
