/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test.ref.phrase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.o42a.compiler.test.CompilerTestCase;
import org.o42a.core.value.ValueType;


public class ReproductionTest extends CompilerTestCase {

	@Test
	public void referOutsideObject() {
		compile(
				"Container := void (",
				"  Referred := 2",
				")",
				"Object :=> integer (",
				"  <Refer> = Container: referred",
				")",
				"Result := object _refer");

		assertThat(definiteValue(field("result"), ValueType.INTEGER), is(2L));
	}

	@Test
	public void descendantReferOutsideObject() {
		compile(
				"Container := void (",
				"  Referred := 2",
				")",
				"Object :=> integer (",
				"  <Refer> = Container: referred",
				")",
				"Descendant :=> object",
				"Result := descendant _refer");

		assertThat(definiteValue(field("result"), ValueType.INTEGER), is(2L));
	}

	@Test
	public void ancestorRefersOutsideObject() {
		compile(
				"Container := void (",
				"  Referred := 2",
				")",
				"Object :=> void (",
				"  Value :=< integer",
				"  <Refer> Value = container: referred",
				")",
				"Result := object _refer");

		assertThat(
				definiteValue(field("result", "value"), ValueType.INTEGER),
				is(2L));
	}

	@Test
	public void descendantAncestorRefersOutsideObject() {
		compile(
				"Container := void (",
				"  Referred := 2",
				")",
				"Object :=> void (",
				"  Value :=< integer",
				"  <Refer> Value = container: referred",
				")",
				"Descendant :=> object",
				"Result := descendant _refer");

		assertThat(
				definiteValue(field("result", "value"), ValueType.INTEGER),
				is(2L));
	}

	@Test
	public void expressionRefersOutsideObject() {
		compile(
				"Container := void (",
				"  Referred := False",
				")",
				"Object :=> void (",
				"  <Refer> Container: referred",
				")",
				"Result := object _refer");

		assertFalseVoid(field("result"));
	}

	@Test
	public void descendantExpressionRefersOutsideObject() {
		compile(
				"Container := void (",
				"  Referred := False",
				")",
				"Object :=> void (",
				"  <Refer> Container: referred",
				")",
				"Descendant :=> object",
				"Result := descendant _refer");

		assertFalseVoid(field("result"));
	}

	@Test
	public void nestedExpressionRefersOutsideObject() {
		compile(
				"Container := void (",
				"  Referred := False",
				")",
				"Object :=> void (",
				"  <Construct> Container (",
				"    <Refer> Referred",
				"  )",
				")",
				"Result := object _construct _refer");

		assertFalseVoid(field("result"));
	}

	@Test
	public void descendantNestedExpressionRefersOutsideObject() {
		compile(
				"Container := void (",
				"  Referred := False",
				")",
				"Object :=> void (",
				"  <Construct> Container (",
				"    <Refer> Referred",
				"  )",
				")",
				"Descendant :=> object",
				"Result := descendant _construct _refer");

		assertFalseVoid(field("result"));
	}

	@Test
	public void nestedAncestorRefersOutsideObject() {
		compile(
				"Container := void (",
				"  Referred := 2",
				")",
				"Object :=> void (",
				"  Foo := void (",
				"    Value := `1",
				"  )",
				"  <*> Foo = * (",
				"    <Refer> Value = container: referred",
				"  )",
				")",
				"Result := object _refer");

		assertThat(
				definiteValue(
						linkTarget(field("result", "foo", "value")),
						ValueType.INTEGER),
				is(2L));
	}

	@Test
	public void descendantNestedAncestorRefersOutsideObject() {
		compile(
				"Container := void (",
				"  Referred := 2",
				")",
				"Object :=> void (",
				"  Foo := void (",
				"    Value := `1",
				"  )",
				"  <*> Foo = * (",
				"    <Refer> Value = container: referred",
				"  )",
				")",
				"Descendant :=> object",
				"Result := descendant _refer");

		assertThat(
				definiteValue(
						linkTarget(field("result", "foo", "value")),
						ValueType.INTEGER),
				is(2L));
	}

}
