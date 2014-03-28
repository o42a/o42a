/*
    Any copyright is dedicated to the Public Domain.
    http://creativecommons.org/publicdomain/zero/1.0/
*/
package org.o42a.compiler.test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.theInstance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.o42a.analysis.use.User.useCase;
import static org.o42a.compiler.Compiler.compiler;
import static org.o42a.compiler.test.matchers.FalseValueMatcher.FALSE_VALUE_MATCHER;
import static org.o42a.compiler.test.matchers.RuntimeValueMatcher.RUNTIME_VALUE_MATCHER;
import static org.o42a.compiler.test.matchers.TrueValueMatcher.TRUE_VALUE_MATCHER;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.intrinsic.CompilerIntrinsics.intrinsics;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.o42a.analysis.Analyzer;
import org.o42a.analysis.use.UseCase;
import org.o42a.codegen.Generator;
import org.o42a.compiler.Compiler;
import org.o42a.compiler.test.matchers.ValueOfTypeMatcher;
import org.o42a.compiler.test.matchers.ValueTypeMatcher;
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
import org.o42a.core.value.Void;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.intrinsic.CompilerIntrinsics;
import org.o42a.util.string.Name;


public abstract class CompilerTestCase {

	private static final Matcher<Void> VOID_VALUE_MATCHER =
			theInstance(Void.VOID);

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

	public static <T> Value<T> valueOf(Field field, ValueType<T> valueType) {
		return valueOf(field.toObject(), valueType);
	}

	public static <T> Value<T> valueOf(Obj object, ValueType<T> valueType) {

		final Value<?> value = valueOf(object);

		assertThat(
				value + " has wrong type",
				value.getValueType(),
				valueType(valueType));

		return valueType.cast(value);
	}

	@SuppressWarnings("unchecked")
	public static <T> T definiteValue(Obj object) {

		final Value<?> value = valueOf(object);

		assertThat(
				"Value is not definite: " + value,
				value,
				not(runtimeValue()));
		assertThat(
				"Value is false: " + value,
				value,
				not(falseValue()));

		final Object definiteValue = value.getCompilerValue();

		assertThat(
				object + " value is not known to compiler",
				definiteValue,
				notNullValue());

		return (T) definiteValue;
	}

	public static <T> T definiteValue(Obj object, ValueType<T> valueType) {

		final Value<?> value = valueOf(object, valueType);

		assertThat(
				"Value is not definite: " + value,
				value,
				not(runtimeValue()));
		assertThat("Value is false: " + value, value, not(falseValue()));

		final Object definiteValue = value.getCompilerValue();

		assertThat(
				object + " value is not known to compiler",
				definiteValue,
				notNullValue());

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

	public static Matcher<Value<?>> falseValue() {
		return FALSE_VALUE_MATCHER;
	}

	public static Matcher<Value<?>> trueValue() {
		return TRUE_VALUE_MATCHER;
	}

	public static <T> Matcher<Value<?>> runtimeValue() {
		return RUNTIME_VALUE_MATCHER;
	}

	public static <T> Matcher<Value<T>> valueOfType(ValueType<T> valueType) {
		return new ValueOfTypeMatcher<>(valueType);
	}

	public static Matcher<ValueType<?>> valueType(ValueType<?> valueType) {
		return new ValueTypeMatcher(valueType);
	}

	public static Matcher<Void> voidValue() {
		return VOID_VALUE_MATCHER;
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
