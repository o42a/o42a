/*
    Intrinsics
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
package org.o42a.intrinsic.root;

import static org.o42a.core.ir.def.Eval.FALSE_EVAL;
import static org.o42a.core.ir.def.InlineEval.falseInlineEval;
import static org.o42a.core.ref.Ref.voidRef;
import static org.o42a.core.value.Value.falseValue;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.common.def.BuiltinDef;
import org.o42a.common.object.BuiltinObject;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.Obj;
import org.o42a.core.object.common.StandaloneObjectScope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.string.ID;


public final class False extends BuiltinObject {

	public False(Scope topScope) {
		super(falseScope(topScope), ValueStruct.VOID);
	}

	@Override
	public Path scopePath() {
		return null;
	}

	@Override
	public boolean isConstantBuiltin() {
		return true;
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {
		return falseValue();
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
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(voidRef(
				this,
				getScope().getEnclosingScope().distribute()).toStaticTypeRef());
	}

	@Override
	protected final Definitions explicitDefinitions() {
		return new BuiltinDef(this)
				.claim()
				.toDefinitions(ValueStruct.VOID);
	}

	@Override
	protected Obj findObjectIn(Scope enclosing) {
		throw new IllegalArgumentException(
				"Not an enclosing scope: " + enclosing);
	}

	private static FalseScope falseScope(Scope topScope) {
		return new FalseScope(topScope.getContext(), topScope.distribute());
	}

	private static final class FalseScope extends StandaloneObjectScope {

		FalseScope(LocationInfo location, Distributor enclosing) {
			super(location, enclosing);
		}

		@Override
		protected ScopeIR createIR(Generator generator) {
			return new IR(generator, this);
		}

	}

	private static final class IR extends ScopeIR {

		private ID id;

		IR(Generator generator, Scope scope) {
			super(generator, scope);
			this.id = ID.id("FALSE");
		}

		@Override
		public ID getId() {
			return this.id;
		}

		@Override
		public void allocate() {
			getScope().toObject().ir(getGenerator()).allocate();
		}

		@Override
		protected void targetAllocated() {
		}

		@Override
		protected HostOp createOp(CodeBuilder builder, Code code) {
			return getScope().toObject().ir(getGenerator()).op(builder, code);
		}

	}

}
