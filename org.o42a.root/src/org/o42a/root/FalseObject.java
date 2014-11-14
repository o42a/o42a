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

import static org.o42a.core.ir.def.Eval.FALSE_EVAL;
import static org.o42a.core.ir.def.InlineEval.falseInlineEval;
import static org.o42a.core.object.OwnerPath.NO_OWNER_PATH;
import static org.o42a.core.object.meta.EscapeMode.ESCAPE_IMPOSSIBLE;
import static org.o42a.core.ref.Ref.voidRef;
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
import org.o42a.core.object.meta.EscapeMode;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.string.ID;


public class FalseObject extends BuiltinObject {

	public FalseObject(Scope topScope) {
		this(topScope, ID.id("FALSE"));
	}

	protected FalseObject(Scope topScope, ID id) {
		super(falseScope(topScope, id), ValueType.VOID);
	}

	@Override
	public boolean isConstantBuiltin() {
		return true;
	}

	@Override
	public EscapeMode escapeMode(Scope scope) {
		return ESCAPE_IMPOSSIBLE;
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return type().getParameters().falseValue();
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {
		return falseInlineEval();
	}

	@Override
	public Eval evalBuiltin() {
		return FALSE_EVAL;
	}

	@Override
	public String toString() {
		return "false";
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
		return new Ascendants(this).setAncestor(voidRef(
				this,
				getScope().getEnclosingScope().distribute()).toStaticTypeRef());
	}

	@Override
	protected final Definitions explicitDefinitions() {
		return new BuiltinDef(this)
				.toDefinitions(typeParameters(this, ValueType.VOID));
	}

	private static FalseScope falseScope(Scope topScope, ID id) {
		return new FalseScope(
				topScope.getContext(),
				topScope.distribute(),
				id);
	}

	private static final class FalseScope extends StandaloneObjectScope {

		private final ID id;

		FalseScope(LocationInfo location, Distributor enclosing, ID id) {
			super(location, enclosing);
			this.id = id;
		}

		@Override
		public ID getId() {
			return this.id;
		}

		@Override
		protected ScopeIR createIR(Generator generator) {
			return new FalseIR(generator, this);
		}

	}

	private static final class FalseIR extends ScopeIR {

		FalseIR(Generator generator, Scope scope) {
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
