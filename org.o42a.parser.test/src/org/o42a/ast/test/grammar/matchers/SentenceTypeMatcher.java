/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SentenceType;


public class SentenceTypeMatcher extends BaseMatcher<SentenceNode> {

	private final SentenceType sentenceType;

	public SentenceTypeMatcher(SentenceType sentenceType) {
		this.sentenceType = sentenceType;
	}

	@Override
	public boolean matches(Object item) {

		final SentenceNode sentence = (SentenceNode) item;
		final SignNode<SentenceType> mark = sentence.getMark();

		if (mark == null) {
			return this.sentenceType == null;
		}

		return mark.getType() == this.sentenceType;
	}

	@Override
	public void describeTo(Description description) {
		if (this.sentenceType == null) {
			description.appendText("Unterminated sentence");
		} else {
			description.appendText(
					"Sentence terminated with `"
					+ this.sentenceType.getSign() + "`");
		}
	}

}
