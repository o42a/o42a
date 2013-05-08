/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.macro;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.core.value.link.LinkValueType.LINK;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.object.Obj;
import org.o42a.core.value.ValueType;


public class MacroDerivationTest extends CompilerTestCase {

	@Test
	public void overrideMacro() {
		compile(
				"A := void (",
				"  #T := void",
				"  F := #t` link = 1",
				")",
				"B := a (",
				"  T = integer",
				")");

		final Obj af = field("a", "f").toObject();
		final Obj bf = field("b", "f").toObject();

		assertThat(
				LINK.interfaceRef(af.type().getParameters()).getType(),
				is(this.context.getIntrinsics().getVoid()));
		assertThat(definiteValue(linkTarget(af), ValueType.INTEGER), is(1L));

		assertThat(
				LINK.interfaceRef(bf.type().getParameters()).getType(),
				is(this.context.getIntrinsics().getInteger()));
		assertThat(definiteValue(linkTarget(bf), ValueType.INTEGER), is(1L));

		assertThat(bf.meta().isUpdated(), is(true));
	}

	@Test
	public void propagateMacro() {
		compile(
				"A := void (",
				"  #T := void",
				"  F := #t` link = 1",
				")",
				"B := a");

		final Obj af = field("a", "f").toObject();
		final Obj bf = field("b", "f").toObject();

		assertThat(
				LINK.interfaceRef(af.type().getParameters()).getType(),
				is(this.context.getIntrinsics().getVoid()));
		assertThat(definiteValue(linkTarget(af), ValueType.INTEGER), is(1L));

		assertThat(
				LINK.interfaceRef(bf.type().getParameters()).getType(),
				is(this.context.getIntrinsics().getVoid()));
		assertThat(definiteValue(linkTarget(bf), ValueType.INTEGER), is(1L));

		assertThat(bf.meta().isUpdated(), is(false));
	}

	@Test
	public void propagateDeepMacro() {
		compile(
				"A := void (",
				"  Inner := void (",
				"    #T := void",
				"  )",
				"  F := #inner: t` link = 1",
				")",
				"B := a");

		final Obj af = field("a", "f").toObject();
		final Obj bf = field("b", "f").toObject();

		assertThat(
				LINK.interfaceRef(af.type().getParameters()).getType(),
				is(this.context.getIntrinsics().getVoid()));
		assertThat(definiteValue(linkTarget(af), ValueType.INTEGER), is(1L));

		assertThat(
				LINK.interfaceRef(bf.type().getParameters()).getType(),
				is(this.context.getIntrinsics().getVoid()));
		assertThat(definiteValue(linkTarget(bf), ValueType.INTEGER), is(1L));

		assertThat(bf.meta().isUpdated(), is(false));
	}

}
