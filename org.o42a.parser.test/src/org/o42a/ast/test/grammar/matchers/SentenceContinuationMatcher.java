/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.matchers;

import static org.o42a.ast.sentence.SentenceType.DECLARATION;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.o42a.ast.atom.NameNode;
import org.o42a.ast.atom.SignNode;
import org.o42a.ast.sentence.ContinuationNode;
import org.o42a.ast.sentence.SentenceNode;
import org.o42a.ast.sentence.SentenceType;


public class SentenceContinuationMatcher extends BaseMatcher<SentenceNode> {

	private final String label;
	private boolean withPeriod;

	public SentenceContinuationMatcher(String label, boolean withPeriod) {
		this.label = label;
		this.withPeriod = withPeriod;
	}

	@Override
	public boolean matches(Object item) {

		final SentenceNode sentence = (SentenceNode) item;

		if (!sentence.getType().supportsContinuation()) {
			return false;
		}

		final ContinuationNode continuation = sentence.getContinuation();

		if (continuation == null) {
			return this.label == null && !this.withPeriod;
		}

		final NameNode label = continuation.getLabel();

		if (label == null) {
			if (this.label != null) {
				return false;
			}
		} else {
			if (!label.getName().toString().equals(this.label)) {
				return false;
			}
		}

		final SignNode<SentenceType> period = continuation.getPeriod();

		if (period == null) {
			return !this.withPeriod;
		}

		return this.withPeriod && period.getType() == DECLARATION;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Sentence with continuation");
		if (this.label != null) {
			description.appendText(" `" + this.label + "`");
		} else {
			description.appendText(" without label");
		}
		if (this.withPeriod) {
			description.appendText(" ending with period");
		} else {
			description.appendText(" not ending with period");
		}
	}

}
