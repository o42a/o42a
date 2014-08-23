/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.link;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.o42a.core.object.ConstructionMode.FULL_CONSTRUCTION;
import static org.o42a.core.object.ConstructionMode.RUNTIME_CONSTRUCTION;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.link.LinkValueType;


public class VariableTest extends CompilerTestCase {

	@Test
	public void variableTarget() {
		compile("A := integer` variable = 1");

		final Value<KnownLink> value =
				valueOf(field("a"), LinkValueType.VARIABLE);

		assertThat(value.getKnowledge().isVariable(), is(true));

		final Obj target = value.getCompilerValue().getTarget();

		assertThat(target.getConstructionMode(), is(RUNTIME_CONSTRUCTION));

		final Value<Long> targetValue = valueOf(target, ValueType.INTEGER);

		assertThat(targetValue.getKnowledge().isKnownToCompiler(), is(false));
	}

	@Test
	public void variableTargetValue() {
		compile(
				"A := integer` variable = 1",
				"B := integer (= a->)");

		final Field b = field("b");

		assertThat(b.getConstructionMode(), is(FULL_CONSTRUCTION));

		final Value<Long> value = valueOf(b, ValueType.INTEGER);

		assertThat(value.getKnowledge().isKnownToCompiler(), is(false));
	}

}
