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

import static org.o42a.core.value.Value.voidValue;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.codegen.code.Code;
import org.o42a.common.object.BuiltinObject;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.ObjectScope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;


public final class VoidObject extends BuiltinObject {

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
	public void resolveBuiltin(Resolver resolver) {
	}

	@Override
	public ValOp writeBuiltin(ValDirs dirs, HostOp host) {
		return voidValue().op(dirs.getBuilder(), dirs.code());
	}

	@Override
	public String toString() {
		return "void";
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this);
	}

	private static VoidScope voidScope(Scope topScope) {
		return new VoidScope(topScope.getContext(), topScope.distribute());
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
