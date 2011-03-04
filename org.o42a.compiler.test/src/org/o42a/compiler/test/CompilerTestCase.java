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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.o42a.compiler.Compiler.compiler;
import static org.o42a.intrinsic.CompilerIntrinsics.intrinsics;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.o42a.compiler.Compiler;
import org.o42a.core.CompilerContext;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.common.Module;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.IRGenerator;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.CompilerIntrinsics;
import org.o42a.util.Source;
import org.o42a.util.log.LogRecord;
import org.o42a.util.log.Logger;


public abstract class CompilerTestCase {

	private static final Compiler COMPILER = compiler();
	private static final CompilerIntrinsics INTRINSICS = intrinsics(COMPILER);

	public static Obj toObject(Artifact<?> artifact) {

		final Obj object = artifact.toObject();

		assertNotNull("Not an object: " + artifact, object);

		return object;
	}

	@SuppressWarnings("unchecked")
	public static <T> T definiteValue(Artifact<?> value) {
		return (T) definiteValue(value, Object.class);
	}

	public static <T> T definiteValue(
			Artifact<?> artifact,
			Class<? extends T> type) {

		final Obj object = artifact.materialize();
		final Value<?> value = object.getValue();

		assertTrue("Value is not definite: " + value, value.isDefinite());

		final Object definiteValue = value.getDefiniteValue();

		assertThat("Wrong value type", definiteValue, instanceOf(type));

		return type.cast(definiteValue);
	}

	@SuppressWarnings("unchecked")
	public static <T> T definiteValue(Field<?> field) {
		return (T) definiteValue(field, Object.class);
	}

	public static <T> T definiteValue(Field<?> field, Class<? extends T> type) {
		return definiteValue(field.getArtifact(), type);
	}

	public static void assertTrueValue(LogicalValue condition) {
		assertTrue(condition + " is not true", condition.isTrue());
	}

	public static void assertTrueValue(Value<?> value) {
		assertTrue(value + " is not true", value.getLogicalValue().isTrue());
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

	public static void assertUnknownValue(Value<?> value) {
		assertTrue(value + " is known", value.isUnknown());
	}

	public static void assertTrueVoid(Obj object) {
		assertEquals(
				object + " is not void",
				ValueType.VOID,
				object.getValueType());
		assertTrueValue(object.getValue());
	}

	public static void assertFalseVoid(Obj object) {
		assertEquals(
				object + " is not void",
				ValueType.VOID,
				object.getValueType());
		assertFalseValue(object.getValue());
	}

	public static void assertUnknownVoid(Obj object) {
		assertEquals(
				object + " is not void",
				ValueType.VOID,
				object.getValueType());
		assertKnownValue(object.getValue());
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
		final Member member = object.member(name, accessor);

		if (member == null) {

			final Member m = object.member(name, Accessor.OWNER);

			if (m == null) {
				fail("No such field: " + name);
			} else {
				fail("Field " + name + " is not available to " + accessor);
			}

			return null;
		}

		final Field<?> field = member.toField();

		assertNotNull(member + " is not a field", field);

		return field;
	}

	protected CompilerContext context;
	protected Module module;
	private String moduleName;
	private final LinkedList<String> expectedErrors = new LinkedList<String>();

	public final String getModuleName() {
		return this.moduleName;
	}

	public final void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	@Before
	public void setUpCompiler() {
		this.context = new Context();
		this.expectedErrors.clear();
		this.moduleName = getClass().getSimpleName();
	}

	@After
	public void ensureErrorsLogged() {
		assertTrue(
				"Errors expected, but not logged: " + this.expectedErrors,
				this.expectedErrors.isEmpty());
	}

	public void expectError(String code) {
		this.expectedErrors.addLast(code);
	}

	public Field<?> field(String name, String... names) {
		return field(this.module, name, names);
	}

	protected void addSource(
			String path,
			String line,
			String... lines) {
		addSource(
				path,
				new Src(getModuleName() + '/' + path, buildCode(line, lines)));
	}

	protected void addSource(String path, Source source) {
		((Context) this.context).addSource(path, source);
	}

	protected final void compile(String line, String... lines) {
		compile(new Src(buildCode(line, lines)));
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

	protected void compile(Source source) {
		((Context) this.context).setSource(source);
		this.module = new Module(this.context, this.moduleName);
		INTRINSICS.setMainModule(this.module);
		this.module.resolveAll();
	}

	protected void generateCode(IRGenerator generator) {
		this.module.resolveAll();
		INTRINSICS.generateAll(generator);
	}

	protected final class Src extends Source {

		private static final long serialVersionUID = -1127243814012269504L;

		private final String name;
		private final String code;

		public Src(String code) {
			this.code = code;
			this.name = getModuleName();
		}

		Src(String name, String code) {
			this.code = code;
			this.name = name;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public Reader open() throws IOException {
			return new StringReader(this.code);
		}

	}

	private class Context extends CompilerContext {

		private Source source;
		private final HashMap<String, Context> subContexts =
			new HashMap<String, Context>();

		Context() {
			super(COMPILER, INTRINSICS, new TestLogger());
			this.source = new Src("");
		}

		Context(CompilerContext parent, Source source) {
			super(parent, parent.getLogger());
			this.source = source;
		}

		@Override
		public CompilerContext contextFor(String path) throws Exception {

			final int idx = path.indexOf('/');
			final String src;

			if (idx < 0) {
				src = path;
			} else {
				src = path.substring(0, idx);
			}

			final Context context = this.subContexts.get(src);

			if (context == null) {
				throw new IllegalStateException(src + " not found in " + this);
			}
			if (idx < 0) {
				return context;
			}

			return context.contextFor(path.substring(idx + 1));
		}

		@Override
		public Source getSource() {
			return this.source;
		}

		void setSource(Source source) {
			this.source = source;
		}

		void addSource(String path, Source source) {

			final int idx = path.indexOf('/');

			if (idx < 0) {
				this.subContexts.put(path, new Context(this, source));
				return;
			}

			final String src = path.substring(0, idx);
			final Context context = this.subContexts.get(src);

			if (context == null) {
				throw new IllegalStateException(src + " not found in " + this);
			}

			context.addSource(path.substring(idx + 1), source);
		}

	}

	private final class TestLogger implements Logger {

		@Override
		public void log(LogRecord record) {

			final String code = record.getCode();
			final String expected =
				CompilerTestCase.this.expectedErrors.poll();

			if (expected == null) {
				fail("Error occurred: " + record);
			}

			assertEquals(
					"Unexpected error occurred: " + record,
					expected,
					code);
		}

	}

}
