/*
    Compiler Tests
    Copyright (C) 2010-2012 Ruslan Lopatin

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
import static org.o42a.analysis.use.User.useCase;
import static org.o42a.compiler.Compiler.compiler;
import static org.o42a.intrinsic.CompilerIntrinsics.intrinsics;

import org.junit.Before;
import org.junit.Rule;
import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.UseCase;
import org.o42a.codegen.Generator;
import org.o42a.compiler.Compiler;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Accessor;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.Link;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Module;
import org.o42a.core.value.*;
import org.o42a.intrinsic.CompilerIntrinsics;


public abstract class CompilerTestCase {

	public static final UseCase USE_CASE = useCase("test");
	protected Analyzer analyzer = new Analyzer("test");

	static final Compiler COMPILER = compiler();
	static final CompilerIntrinsics INTRINSICS = intrinsics(COMPILER);

	public static Value<?> valueOf(Obj object) {
		return object.value().getValue();
	}

	public static Value<?> valueOf(Field field) {
		return valueOf(field.toObject());
	}

	public static <T> Value<T> valueOf(
			Field field,
			SingleValueType<T> valueType) {
		return valueOf(field, valueType.struct());
	}

	public static <T> Value<T> valueOf(
			Obj object,
			SingleValueType<T> valueType) {
		return valueOf(object, valueType.struct());
	}

	public static <T> Value<T> valueOf(
			Field field,
			ValueStruct<?, T> valueStruct) {
		return valueOf(field.toObject(), valueStruct);
	}

	public static <T> Value<T> valueOf(
			Obj object,
			ValueStruct<?, T> valueStruct) {

		final Value<?> value = valueOf(object);

		assertEquals(
				value + " has wrong type",
				valueStruct,
				value.getValueStruct());

		return valueStruct.cast(value);
	}

	@SuppressWarnings("unchecked")
	public static <T> T definiteValue(Obj object) {

		final Value<?> value = valueOf(object);

		assertTrue(
				"Value is not definite: " + value,
				value.getKnowledge().isKnownToCompiler());
		assertFalse(
				"Value is false: " + value,
				value.getKnowledge().isFalse());

		final Object definiteValue = value.getCompilerValue();

		assertNotNull(object + " has not definite value", definiteValue);

		return (T) definiteValue;
	}

	public static <T> T definiteValue(
			Obj object,
			SingleValueType<T> valueType) {
		return definiteValue(object, valueType.struct());
	}

	public static <T> T definiteValue(
			Obj object,
			ValueStruct<?, T> valueStruct) {

		final Value<?> value = valueOf(object, valueStruct);

		assertTrue(
				"Value is not definite: " + value,
				value.getKnowledge().isKnownToCompiler());
		assertFalse(
				"Value is false: " + value,
				value.getKnowledge().isFalse());

		final Object definiteValue = value.getCompilerValue();

		assertNotNull(object + " has not definite value", definiteValue);

		return valueStruct.cast(definiteValue);
	}

	public static <T> T definiteValue(Field field) {
		return definiteValue(field.toObject());
	}

	public static <T> T definiteValue(
			Field field,
			SingleValueType<T> valueType) {
		return definiteValue(field.toObject(), valueType.struct());
	}

	public static <T> T definiteValue(
			Field field,
			ValueStruct<?, T> valueStruct) {
		return definiteValue(field.toObject(), valueStruct);
	}

	public static <T> T definiteValue(
			Obj object,
			Class<? extends T> valueClass) {
		return valueClass.cast(definiteValue(object));
	}

	public static <T> T definiteValue(
			Field field,
			Class<? extends T> valueClass) {
		return valueClass.cast(definiteValue(field));
	}

	public static Obj linkTarget(Obj object) {
		return definiteValue(object, Link.class).getTarget();
	}

	public static Obj linkTarget(Scope scope) {
		return linkTarget(scope.toObject());
	}

	public static void assertTrueValue(LogicalValue condition) {
		assertTrue(condition + " is not true", condition.isTrue());
	}

	public static void assertTrueValue(Value<?> value) {
		assertTrue(
				value + " is not true",
				value.getKnowledge().getCondition().isTrue());
	}

	public static void assertFalseValue(LogicalValue condition) {
		assertTrue(condition + " is not false", condition.isFalse());
	}

	public static void assertFalseValue(Value<?> value) {
		assertTrue(
				value + " is not false",
				value.getKnowledge().isFalse());
	}

	public static void assertRuntimeValue(Value<?> value) {
		assertFalse(
				value + " is definite",
				value.getKnowledge().isKnownToCompiler());
	}

	public static void assertTrueVoid(Field field) {
		assertTrueVoid(field.toObject());
	}

	public static void assertTrueVoid(Obj object) {
		assertEquals(
				object + " is not void",
				ValueType.VOID,
				object.value().getValueType());
		assertTrueValue(object.value().getValue());
	}

	public static void assertFalseVoid(Field field) {
		assertFalseVoid(field.toObject());
	}

	public static void assertFalseVoid(Obj object) {
		assertEquals(
				object + " is not void",
				ValueType.VOID,
				object.value().getValueType());
		assertFalseValue(object.value().getValue());
	}

	public static Field field(
			Field container,
			String name,
			String... names) {
		return field(container.toObject(), name, names);
	}

	public static Field field(
			Obj container,
			String name,
			String... names) {

		Field field = field(container, name, Accessor.PUBLIC);

		for (String n : names) {
			field = field(field, n, Accessor.PUBLIC);
		}

		return field;
	}

	public static Field field(
			Field container,
			String name,
			Accessor accessor) {
		return field(container.toObject(), name, accessor);
	}

	public static Field field(Obj container, String name, Accessor accessor) {

		final Member member = container.field(name, accessor);

		if (member == null) {

			final Member m = container.field(name, Accessor.OWNER);

			if (m == null) {
				fail("No such field: " + name);
			} else {
				fail("Field " + name + " is not available to " + accessor);
			}

			return null;
		}

		final Field field = member.toField().field(USE_CASE);

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

	public Field field(String name, String... names) {
		return field(this.module, name, names);
	}

	protected void addSource(
			String path,
			String line,
			String... lines) {
		this.source.addSource(path, buildCode(line, lines));
	}

	protected void compile(String line, String... lines) {
		this.analyzer = createAnalyzer();
		this.source = new TestSource(this, buildCode(line, lines), this.source);
		this.context = new TestSourceTree(this.source).context(this.topContext);

		this.context.fullResolution().reset();
		this.module = new Module(this.context, getModuleName());
		INTRINSICS.setMainModule(this.module);
		INTRINSICS.resolveAll(this.analyzer);
		assert this.module.getContext().fullResolution().isComplete() :
			"Full resolution is incomplete";
	}

	protected Analyzer createAnalyzer() {
		return new Analyzer(getModuleName());
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
