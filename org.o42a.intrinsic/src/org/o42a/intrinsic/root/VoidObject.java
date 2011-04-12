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

import static org.o42a.core.ref.Ref.voidRef;
import static org.o42a.core.value.Value.voidValue;
import static org.o42a.util.log.Logger.DECLARATION_LOGGER;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.core.*;
import org.o42a.core.artifact.object.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.Source;
import org.o42a.util.log.LoggableData;


public final class VoidObject extends Obj {

	public VoidObject(Scope topScope) {
		super(voidScope(topScope));
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
		return voidRef(this, scope.distribute()).toValueDef().toDefinitions();
	}

	@Override
	protected Value<?> calculateValue(Scope scope) {
		return voidValue();
	}

	private static VoidScope voidScope(Scope topScope) {

		final VoidContext context = new VoidContext(topScope.getContext());

		return new VoidScope(
				new Location(context, new LoggableData(context)),
				topScope.distribute());
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

		VoidScope(LocationInfo location, Distributor enclosing) {
			super(location, enclosing);
		}

		@Override
		protected ScopeIR createIR(Generator generator) {
			return new IR(generator, this);
		}

	}

	private static final class IR extends ScopeIR {

		private CodeId id;

		IR(Generator generator, Scope scope) {
			super(generator, scope);
			this.id = generator.id("VOID");
		}

		@Override
		public CodeId getId() {
			return this.id;
		}

		@Override
		public void allocate() {

			final Obj object = getScope().getContainer().toObject();

			object.ir(getGenerator()).allocate();
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
