/*
    Root Object Definition
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.root;

import static org.o42a.core.ir.def.Eval.VOID_EVAL;
import static org.o42a.core.ir.def.InlineEval.voidInlineEval;
import static org.o42a.core.object.OwnerPath.NO_OWNER_PATH;
import static org.o42a.core.value.TypeParameters.typeParameters;

import org.o42a.codegen.Generator;
import org.o42a.common.builtin.BuiltinDef;
import org.o42a.common.builtin.BuiltinObject;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.OwnerPath;
import org.o42a.core.object.common.StandaloneObjectScope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.Void;
import org.o42a.util.string.ID;


public final class VoidObject extends BuiltinObject {

	public VoidObject(Scope topScope) {
		super(voidScope(topScope), ValueType.VOID);
	}

	@Override
	public boolean isConstantBuiltin() {
		return true;
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return ValueType.VOID.cast(type().getParameters())
				.compilerValue(Void.VOID);
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {
		return voidInlineEval();
	}

	@Override
	public Eval evalBuiltin() {
		return VOID_EVAL;
	}

	@Override
	public String toString() {
		return "void";
	}

	@Override
	protected OwnerPath createOwnerPath() {
		return NO_OWNER_PATH;
	}

	@Override
	protected Nesting createNesting() {
		return Nesting.NO_NESTING;
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this);
	}

	@Override
	protected Definitions explicitDefinitions() {
		return new BuiltinDef(this)
		.toDefinitions(typeParameters(this, ValueType.VOID));
	}

	private static VoidScope voidScope(Scope topScope) {
		return new VoidScope(topScope.getContext(), topScope.distribute());
	}

	private static final class VoidScope extends StandaloneObjectScope {

		private final ID id;

		VoidScope(LocationInfo location, Distributor enclosing) {
			super(location, enclosing);
			this.id = ID.id("VOID");
		}

		@Override
		public ID getId() {
			return this.id;
		}

		@Override
		protected ScopeIR createIR(Generator generator) {
			return new IR(generator, this);
		}

	}

	private static final class IR extends ScopeIR {

		IR(Generator generator, Scope scope) {
			super(generator, scope);
		}

		@Override
		public void allocate() {
			getScope().toObject().ir(getGenerator()).allocate();
		}

		@Override
		protected void targetAllocated() {
		}

	}

}
