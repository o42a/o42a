/*
    Compiler Tests
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
package org.o42a.compiler.test;

import static org.junit.Assert.*;
import static org.o42a.compiler.Compiler.compiler;
import static org.o42a.intrinsic.CompilerIntrinsics.intrinsics;
import static org.o42a.util.use.User.useCase;

import org.junit.Before;
import org.junit.Rule;
import org.o42a.codegen.Generator;
import org.o42a.compiler.Compiler;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Module;
import org.o42a.core.value.*;
import org.o42a.intrinsic.CompilerIntrinsics;
import org.o42a.util.use.UseCase;


public abstract class CompilerTestCase {

	public static final UseCase USE_CASE = useCase("test");

	static final Compiler COMPILER = compiler();
	static final CompilerIntrinsics INTRINSICS = intrinsics(COMPILER);

	public static Value<?> valueOf(Artifact<?> artifact) {
		return artifact.materialize().value().getValue();
	}

	public static Value<?> valueOf(Field<?> field) {
		return valueOf(field.getArtifact());
	}

	public static <T> Value<T> valueOf(
			Field<?> field,
			SingleValueType<T> valueType) {
		return valueOf(field, valueType.struct());
	}

	public static <T> Value<T> valueOf(
			Artifact<?> artifact,
			SingleValueType<T> valueType) {
		return valueOf(artifact, valueType.struct());
	}

	public static <T> Value<T> valueOf(
			Field<?> field,
			ValueStruct<?, T> valueStruct) {
		return valueOf(field.getArtifact(), valueStruct);
	}

	public static <T> Value<T> valueOf(
			Artifact<?> artifact,
			ValueStruct<?, T> valueStruct) {

		final Value<?> value = valueOf(artifact);

		assertEquals(
				value + " has wrong type",
				valueStruct,
				value.getValueStruct());

		return valueStruct.cast(value);
	}

	public static Obj toObject(Artifact<?> artifact) {

		final Obj object = artifact.toObject();

		assertNotNull("Not an object: " + artifact, object);

		return object;
	}

	@SuppressWarnings("unchecked")
	public static <T> T definiteValue(Artifact<?> artifact) {

		final Value<?> value = valueOf(artifact);

		assertTrue("Value is not definite: " + value, value.isDefinite());
		assertFalse("Value is unknown: " + value, value.isUnknown());
		assertFalse("Value is false: " + value, value.isFalse());

		final Object definiteValue = value.getDefiniteValue();

		assertNotNull(artifact + " has not definite value", definiteValue);

		return (T) definiteValue;
	}

	public static <T> T definiteValue(
			Artifact<?> artifact,
			SingleValueType<T> valueType) {
		return definiteValue(artifact, valueType.struct());
	}

	public static <T> T definiteValue(
			Artifact<?> artifact,
			ValueStruct<?, T> valueStruct) {

		final Value<?> value = valueOf(artifact, valueStruct);

		assertTrue("Value is not definite: " + value, value.isDefinite());
		assertFalse("Value is unknown: " + value, value.isUnknown());
		assertFalse("Value is false: " + value, value.isFalse());

		final Object definiteValue = value.getDefiniteValue();

		assertNotNull(artifact + " has not definite value", definiteValue);

		return valueStruct.cast(definiteValue);
	}

	public static <T> T definiteValue(Field<?> field) {
		return definiteValue(field.getArtifact());
	}

	public static <T> T definiteValue(
			Field<?> field,
			SingleValueType<T> valueType) {
		return definiteValue(field.getArtifact(), valueType.struct());
	}

	public static <T> T definiteValue(
			Field<?> field,
			ValueStruct<?, T> valueStruct) {
		return definiteValue(field.getArtifact(), valueStruct);
	}

	public static void assertTrueValue(LogicalValue condition) {
		assertTrue(condition + " is not true", condition.isTrue());
	}

	public static void assertTrueValue(Value<?> value) {
		assertTrue(value + " is not true", value.getCondition().isTrue());
	}

	public static void assertFalseValue(LogicalValue condition) {
		assertTrue(condition + " is not false", condition.isFalse());
	}

	public static void assertFalseValue(Value<?> value) {
		assertTrue(value + " is not false", value.isFalse());
	}

	public static void assertKnownValue(Value<?> value) {
		assertFalse(value + " is unknown", value.isUnknown());
	}

	public static void assertRuntimeValue(Value<?> value) {
		assertKnownValue(value);
		assertTrue(value + " is definite", !value.isDefinite());
	}

	public static void assertUnknownValue(Value<?> value) {
		assertTrue(value + " is known", value.isUnknown());
	}

	public static void assertTrueVoid(Field<?> field) {
		assertTrueVoid(field.getArtifact().materialize());
	}

	public static void assertTrueVoid(Obj object) {
		assertEquals(
				object + " is not void",
				ValueType.VOID,
				object.value().getValueType());
		assertTrueValue(object.value().getValue());
	}

	public static void assertFalseVoid(Field<?> field) {
		assertFalseVoid(field.getArtifact().materialize());
	}

	public static void assertFalseVoid(Obj object) {
		assertEquals(
				object + " is not void",
				ValueType.VOID,
				object.value().getValueType());
		assertFalseValue(object.value().getValue());
	}

	public static void assertUnknownVoid(Field<?> field) {
		assertUnknownVoid(field.getArtifact().materialize());
	}

	public static void assertUnknownVoid(Obj object) {
		assertEquals(
				object + " is not void",
				ValueType.VOID,
				object.value().getValueType());
		assertKnownValue(object.value().getValue());
	}

	public static Field<?> field(
			Field<?> container,
			String name,
			String... names) {
		return field(container.getArtifact(), name, names);
	}

	public static Field<?> field(
			Artifact<?> container,
			String name,
			String... names) {

		Field<?> field = field(container, name, Accessor.PUBLIC);

		for (String n : names) {
			field = field(field, n, Accessor.PUBLIC);
		}

		return field;
	}

	public static Field<?> field(
			Field<?> container,
			String name,
			Accessor accessor) {
		return field(container.getArtifact(), name, accessor);
	}

	public static Field<?> field(
			Artifact<?> container,
			String name,
			Accessor accessor) {

		final Obj object = container.materialize().toArtifact();
		final Member member = object.field(name, accessor);

		if (member == null) {

			final Member m = object.field(name, Accessor.OWNER);

			if (m == null) {
				fail("No such field: " + name);
			} else {
				fail("Field " + name + " is not available to " + accessor);
			}

			return null;
		}

		final Field<?> field = member.toField(USE_CASE);

		assertNotNull(member + " is not a field", field);

		return field;
	}

	@Rule
	public final TestErrors errors = new TestErrors();

	@Rule
	public final ModuleName moduleName = new ModuleName();

	private TestSource source;
	protected CompilerContext context;
	private final CompilerContext topContext =
			new TestCompilerContext(this, this.errors);
	protected Module module;

	public final String getModuleName() {
		return this.moduleName.getModuleName();
	}

	public final void setModuleName(String moduleName) {
		this.moduleName.setModuleName(moduleName);
	}

	@Before
	public void createSource() {
		this.source = new TestSource(this);
	}

	public void expectError(String code) {
		this.errors.expectError(code);
	}

	public Field<?> field(String name, String... names) {
		return field(this.module, name, names);
	}

	protected void addSource(
			String path,
			String line,
			String... lines) {
		this.source.addSource(path, buildCode(line, lines));
	}

	protected void compile(String line, String... lines) {
		this.source = new TestSource(this, buildCode(line, lines), this.source);
		this.context = new TestSourceTree(this.source).context(this.topContext);

		this.context.fullResolution().reset();
		this.module = new Module(this.context, getModuleName());
		INTRINSICS.setMainModule(this.module);
		INTRINSICS.resolveAll();
		assert this.module.getContext().fullResolution().isComplete() :
			"Full resolution is incomplete";
	}

	protected void generateCode(Generator generator) {
		INTRINSICS.generateAll(generator);
	}

	private String buildCode(String line, String... lines) {

		final String code;

		if (lines.length == 0) {
			code = line;
		} else {

			final StringBuilder text = new StringBuilder();

			text.append(line);
			for (String l : lines) {
				text.append('\n').append(l);
			}

			code = text.toString();
		}

		return code;
	}

}
