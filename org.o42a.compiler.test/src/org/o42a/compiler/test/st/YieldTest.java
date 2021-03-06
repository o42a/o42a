/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.st;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.MemberIdKind.LOCAL_MEMBER_NAME;
import static org.o42a.util.string.Name.caseInsensitiveName;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.Accessor;
import org.o42a.core.member.Member;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class YieldTest extends CompilerTestCase {

	@Test
	public void yield() {
		compile(
				"A := integer (",
				"  << 1",
				")");

		assertThat(valueOf(field("a"), ValueType.INTEGER), runtimeValue());
	}

	@Test
	public void yieldTwice() {
		compile(
				"A := integer (",
				"  << 1, << 2",
				")");

		assertThat(valueOf(field("a"), ValueType.INTEGER), runtimeValue());
	}

	@Test
	public void convertLocalToMember() {
		compile(
				"A := integer (",
				"  2 $ F (",
				"    << 1",
				"    F",
				"  )",
				")");

		final Obj a = field("a").toObject();
		final Member memberF = a.member(
				LOCAL_MEMBER_NAME.memberName(caseInsensitiveName("f")),
				Accessor.OWNER);

		assertThat("Local field `f` not created", memberF, notNullValue());

		final Obj f = memberF.toLocal().substance(dummyUser());

		assertThat(definiteValue(f, ValueType.INTEGER), is(2L));
		assertThat(valueOf(a, ValueType.INTEGER), runtimeValue());
	}

	@Test
	public void convertLocalLinkToField() {
		compile(
				"A := integer (",
				"  `2 $ F (",
				"    << 1",
				"    F",
				"  )",
				")");

		final Obj a = field("a").toObject();
		final Member memberF = a.member(
				LOCAL_MEMBER_NAME.memberName(caseInsensitiveName("f")),
				Accessor.OWNER);

		assertThat("Local field `f` not created", memberF, notNullValue());

		final Obj f = memberF.toLocal().substance(dummyUser());

		assertThat(
				definiteValue(linkTarget(f), ValueType.INTEGER),
				is(2L));
		assertThat(valueOf(a, ValueType.INTEGER), runtimeValue());
	}

}
