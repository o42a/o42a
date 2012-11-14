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

import static org.o42a.core.value.TypeParameters.typeParameters;
import static org.o42a.core.value.Value.voidValue;

import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.common.builtin.BuiltinDef;
import org.o42a.common.builtin.BuiltinObject;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.common.StandaloneObjectScope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Cancelable;
import org.o42a.util.string.ID;


public final class VoidObject extends BuiltinObject {

	private static final VoidEval VOID_EVAL = new VoidEval();

	public VoidObject(Scope topScope) {
		super(voidScope(topScope), ValueStruct.VOID);
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
		return voidValue();
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {
		return VOID_EVAL;
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

		@Override
		protected HostOp createOp(CodeBuilder builder, Code code) {
			return getScope().toObject().ir(getGenerator()).op(builder, code);
		}

	}

	private static final class VoidEval extends InlineEval {

		VoidEval() {
			super(null);
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			dirs.returnValue(voidValue().op(dirs.getBuilder(), dirs.code()));
		}

		@Override
		public String toString() {
			return "VOID";
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}
