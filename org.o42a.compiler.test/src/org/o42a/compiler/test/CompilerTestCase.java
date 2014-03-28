/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.o42a.analysis.use.User.useCase;
import static org.o42a.compiler.Compiler.compiler;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.intrinsic.CompilerIntrinsics.intrinsics;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.junit.Before;
import org.junit.Rule;
import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.UseCase;
import org.o42a.codegen.Generator;
import org.o42a.compiler.Compiler;
import org.o42a.core.Scope;
import org.o42a.core.member.Accessor;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberName;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Module;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.intrinsic.CompilerIntrinsics;
import org.o42a.util.string.Name;


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
			ValueType<T> valueStruct) {
		return valueOf(field.toObject(), valueStruct);
	}

	@SuppressWarnings("rawtypes")
	public static <T> Value<T> valueOf(
			Obj object,
			ValueType<T> valueType) {

		final Value<?> value = valueOf(object);

		assertThat(
				value + " has wrong type",
				value.getValueType(),
				is((ValueType) valueType));

		return valueType.cast(value);
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

	public static <T> T definiteValue(Obj object, ValueType<T> valueType) {

		final Value<?> value = valueOf(object, valueType);

		assertTrue(
				"Value is not definite: " + value,
				value.getKnowledge().isKnownToCompiler());
		assertFalse(
				"Value is false: " + value,
				value.getKnowledge().isFalse());

		final Object definiteValue = value.getCompilerValue();

		assertNotNull(object + " has not definite value", definiteValue);

		return valueType.cast(definiteValue);
	}

	public static <T> T definiteValue(Field field) {
		return definiteValue(field.toObject());
	}

	public static <T> T definiteValue(
			Field field,
			ValueType<T> valueStruct) {
		return definiteValue(field.toObject(), valueStruct);
	}

	public static Obj linkTarget(Obj object) {
		return definiteValue(object, LinkValueType.LINK).getTarget();
	}

	public static Obj linkTarget(Scope scope) {
		return linkTarget(scope.toObject());
	}

	public static void assertTrueValue(Value<?> value) {
		assertTrue(
				value + " is not true",
				value.getKnowledge().getCondition().isTrue());
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
				object.type().getValueType());
		assertTrueValue(object.value().getValue());
	}

	public static void assertFalseVoid(Field field) {
		assertFalseVoid(field.toObject());
	}

	public static void assertFalseVoid(Obj object) {
		assertEquals(
				object + " is not void",
				ValueType.VOID,
				object.type().getValueType());
		assertFalseValue(object.value().getValue());
	}

	public Field field(String name, String... names) {
		return field(this.module, name, names);
	}

	public Member member(String name, String... names) {
		return member(this.module, name, names);
	}

	public Field field(String name, Accessor accessor) {
		return field(this.module, name, accessor);
	}

	public Member member(String name, Accessor accessor) {
		return member(this.module, name, accessor);
	}

	public static Field field(
			Field container,
			String name,
			String... names) {
		return field(container.toObject(), name, names);
	}

	public static Member member(
			Field container,
			String name,
			String... names) {
		return member(container.toObject(), name, names);
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

	public static Member member(
			Obj container,
			String name,
			String... names) {
		if (names.length == 0) {
			return member(container, name, Accessor.PUBLIC);
		}

		Field field = field(container, name, Accessor.PUBLIC);
		final int numFields = names.length - 1;

		for (int i = 0; i < numFields; ++i) {
			field = field(field, names[i], Accessor.PUBLIC);
		}

		return member(field, names[numFields], Accessor.PUBLIC);
	}

	public static Field field(
			Field container,
			String name,
			Accessor accessor) {
		return field(container.toObject(), name, accessor);
	}

	public static Member member(
			Field container,
			String name,
			Accessor accessor) {
		return member(container.toObject(), name, accessor);
	}

	public static Field field(Obj container, String name, Accessor accessor) {

		final Member member = member(container, name, accessor);
		final Field field = member.toField().field(USE_CASE);

		assertNotNull(member + " is not a field", field);

		return field;
	}

	public static Member member(
			Obj container,
			String name,
			Accessor accessor) {

		final MemberName fieldName =
				fieldName(CASE_INSENSITIVE.canonicalName(name));
		final Member member = container.member(fieldName, accessor);

		if (member == null) {

			final Member m = container.member(fieldName, Accessor.OWNER);

			if (m == null) {
				fail("No such member: " + name);
			} else {
				fail("Member " + name + " is not available to " + accessor);
			}

			return null;
		}

		return member;
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

	public final Name getModuleName() {
		return this.moduleName.getModuleName();
	}

	public final void setModuleName(Name moduleName) {
		this.moduleName.setModuleName(moduleName);
	}

	@Before
	public void createSource() {
		this.source = new TestSource(this);
	}

	public void expectError(String code) {
		this.errors.expectError(code);
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
		INTRINSICS.resolveAll(this.analyzer, this.errors);
		assert this.module.getContext().fullResolution().isComplete() :
			"Full resolution is incomplete";
	}

	protected Analyzer createAnalyzer() {
		return new Analyzer(getModuleName().toString());
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
