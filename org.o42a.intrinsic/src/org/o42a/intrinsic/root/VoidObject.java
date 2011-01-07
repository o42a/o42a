/*
    Intrinsics
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
package org.o42a.intrinsic.root;

import static org.o42a.core.def.Def.voidDef;
import static org.o42a.core.value.Value.voidValue;
import static org.o42a.util.log.Logger.DECLARATION_LOGGER;

import org.o42a.codegen.code.Code;
import org.o42a.core.*;
import org.o42a.core.artifact.TypeRef;
import org.o42a.core.artifact.object.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.Source;


public final class VoidObject extends Obj {

	public VoidObject(Scope topScope) {
		super(new VoidScope(
				new Location(new VoidContext(topScope.getContext()), null),
				topScope.distribute()));
	}

	@Override
	public TypeRef getAncestor() {
		return null;
	}

	@Override
	public Path scopePath() {
		return null;
	}

	@Override
	public String toString() {
		return "void";
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(getScope());
	}

	@Override
	protected ValueType<?> resolveValueType() {
		return ValueType.VOID;
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions overrideDefinitions(
			Scope scope,
			Definitions ascendantDefinitions) {
		if (ascendantDefinitions != null) {
			return ascendantDefinitions;
		}
		return voidDef(this, distribute()).toDefinitions();
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {
		return voidValue();
	}

	private static final class VoidContext extends CompilerContext {

		VoidContext(CompilerContext topContext) {
			super(topContext, DECLARATION_LOGGER);
		}

		@Override
		public Source getSource() {
			return null;
		}

		@Override
		public CompilerContext contextFor(String path) throws Exception {
			throw new UnsupportedOperationException(
					this + " has no child contexts");
		}

		@Override
		public String toString() {
			return "void";
		}

	}

	private static final class VoidScope extends ObjectScope {

		VoidScope(LocationSpec location, Distributor enclosing) {
			super(location, enclosing);
		}

		@Override
		protected ScopeIR createIR(IRGenerator generator) {
			return new IR(generator, this);
		}

	}

	private static final class IR extends ScopeIR {

		IR(IRGenerator generator, Scope scope) {
			super(generator, scope);
		}

		@Override
		public String getId() {
			return "VOID";
		}

		@Override
		public String prefix(IRSymbolSeparator separator, String suffix) {
			return getId() + separator + suffix;
		}

		@Override
		public void allocate() {

			final Obj object = getScope().getContainer().toObject();

			object.ir(getGenerator()).getData();
		}

		@Override
		protected void targetAllocated() {
		}

		@Override
		protected HostOp createOp(CodeBuilder builder, Code code) {

			final Obj object = getScope().getContainer().toObject();

			return object.ir(getGenerator()).op(builder, code);
		}

	}

}
