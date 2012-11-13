/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.value;

import static org.o42a.core.ref.type.TypeParameters.typeParameters;
import static org.o42a.util.log.Logger.DECLARATION_LOGGER;

import org.o42a.core.*;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeParameters;
import org.o42a.core.source.*;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.value.array.ArrayValueStruct;
import org.o42a.core.value.link.LinkValueStruct;
import org.o42a.util.io.EmptySource;
import org.o42a.util.io.Source;
import org.o42a.util.string.Name;


public abstract class SingleValueStruct<T>
		extends ValueStruct<SingleValueStruct<T>, T> {

	private static final FakeContext FAKE_CONTEXT = new FakeContext();
	private static final Location BUILTIN =
			new Location(FAKE_CONTEXT, FAKE_CONTEXT.getSource());

	private final TypeParameters<T> parameters;

	public SingleValueStruct(SingleValueType<T> valueType) {
		super(valueType);
		this.parameters = typeParameters(BUILTIN, valueType);
	}

	@Override
	public SingleValueType<T> getValueType() {
		return (SingleValueType<T>) super.getValueType();
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public final TypeParameters<T> getParameters() {
		return this.parameters;
	}

	public final boolean is(ValueStruct<?, ?> valueStruct) {
		return this == valueStruct;
	}

	@Override
	public final ScopeInfo toScoped() {
		return null;
	}

	@Override
	public final LinkValueStruct toLinkStruct() {
		return null;
	}

	@Override
	public final ArrayValueStruct toArrayStruct() {
		return null;
	}

	@Override
	public final SingleValueStruct<T> prefixWith(PrefixPath prefix) {
		return this;
	}

	@Override
	public final SingleValueStruct<T> upgradeScope(Scope toScope) {
		return this;
	}

	@Override
	public final SingleValueStruct<T> rebuildIn(Scope scope) {
		return this;
	}

	@Override
	public SingleValueStruct<T> reproduce(Reproducer reproducer) {
		return this;
	}

	@Override
	public void resolveAll(FullResolver resolver) {
	}

	@Override
	public String toString() {

		final SingleValueType<T> valueType = getValueType();

		if (valueType == null) {
			return super.toString();
		}

		return valueType.toString();
	}

	private static final class FakeIntrinsics extends Intrinsics {

		@Override
		public Obj getVoid() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj getFalse() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj getNone() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Container getTop() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Namespace getModuleNamespace() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj getRoot() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj getDirective() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj getMacro() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj getInteger() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj getFloat() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj getString() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj getLink() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj getVariable() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj getArray() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Obj getRow() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Module getModule(Name moduleName) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Module getMainModule() {
			throw new UnsupportedOperationException();
		}

	}

	private static final class FakeCompiler implements SourceCompiler {

		@Override
		public ModuleCompiler compileModule(ObjectSource source) {
			throw new UnsupportedOperationException();
		}

		@Override
		public FieldCompiler compileField(ObjectSource source) {
			throw new UnsupportedOperationException();
		}

		@Override
		public DefinitionCompiler compileDefinition(DefinitionSource source) {
			throw new UnsupportedOperationException();
		}

		@Override
		public PathWithAlias compilePath(
				Scope scope,
				Name moduleName,
				LocationInfo location,
				String string) {
			throw new UnsupportedOperationException();
		}

	}

	private static final class FakeContext extends CompilerContext {

		private final EmptySource source = new EmptySource("built-in");

		FakeContext() {
			super(new FakeCompiler(), new FakeIntrinsics(), DECLARATION_LOGGER);
		}

		@Override
		public Source getSource() {
			return this.source;
		}

		@Override
		public ModuleCompiler compileModule() {
			throw new UnsupportedOperationException();
		}

		@Override
		public FieldCompiler compileField() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void include(DeclarativeBlock block, SectionTag tag) {
			throw new UnsupportedOperationException();
		}

	}

}
