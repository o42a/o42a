/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.ast.test.grammar.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.o42a.ast.Node;
import org.o42a.ast.ref.MemberRefNode;


public class MemberRefWithoutRetention<T extends Node> extends BaseMatcher<T> {

	@SuppressWarnings("rawtypes")
	public static final
	MemberRefWithoutRetention<?> MEMBER_REF_WITHOUT_RETENTION =
			new MemberRefWithoutRetention();

	private MemberRefWithoutRetention() {
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Member reference without retention");
	}

	@Override
	public boolean matches(Object item) {
		if (!(item instanceof MemberRefNode)) {
			return false;
		}

		final MemberRefNode node = (MemberRefNode) item;

		return node.getDeclaredIn() == null;
	}

}
