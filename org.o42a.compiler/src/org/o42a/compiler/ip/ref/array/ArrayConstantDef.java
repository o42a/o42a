/*
    Compiler
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.ref.array;

import static org.o42a.core.ir.object.op.ObjHolder.tempObjHolder;
import static org.o42a.core.ref.ScopeUpgrade.noScopeUpgrade;
import static org.o42a.core.st.DefValue.defValue;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.object.ObjectOp;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.value.ObjectValFunc;
import org.o42a.core.ir.value.array.ArrayIR;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Def;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayValueType;


final class ArrayConstantDef extends Def {

	private final Value<Array> value;

	ArrayConstantDef(Obj source, Array value) {
		super(source, source, noScopeUpgrade(source.getScope()));
		this.value = value.toValue();
	}

	private ArrayConstantDef(
			ArrayConstantDef prototype,
			ScopeUpgrade scopeUpgrade) {
		super(prototype, scopeUpgrade);
		this.value = prototype.value;
	}

	@Override
	public boolean unconditional() {
		return true;
	}

	public final Array getArray() {
		return this.value.getCompilerValue();
	}

	@Override
	public InlineEval inline(Normalizer normalizer) {
		normalizer.cancelAll();
		return null;
	}

	@Override
	public void normalize(RootNormalizer normalizer) {
	}

	@Override
	public Eval eval() {
		if (hasConstantValue()) {
			return new ConstArrayEval(this.value);
		}
		return new ArrayConstantEval(this.value.getCompilerValue());
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return this.value.toString();
	}

	@Override
	protected boolean hasConstantValue() {

		final Array array = getArray();

		return !array.isVariable() && array.hasStaticItems();
	}

	@Override
	protected TypeParameters<Array> typeParameters(Scope scope) {
		return this.value.getTypeParameters().toArrayParameters();
	}

	@Override
	protected DefValue calculateValue(Resolver resolver) {
		return defValue(this.value);
	}

	@Override
	protected ArrayConstantDef create(
			ScopeUpgrade upgrade,
			ScopeUpgrade additionalUpgrade) {
		return new ArrayConstantDef(this, upgrade);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		this.value.resolveAll(resolver);
	}

	private static final class ConstArrayEval implements Eval {

		private final Value<Array> value;

		public ConstArrayEval(Value<Array> value) {
			this.value = value;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			dirs.returnValue(this.value.op(dirs.getBuilder(), dirs.code()));
		}

		@Override
		public String toString() {
			if (this.value == null) {
				return super.toString();
			}
			return this.value.toString();
		}

	}

	private static final class ArrayConstantEval implements Eval {

		private final Array array;

		ArrayConstantEval(Array value) {
			this.array = value;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ArrayValueType valueType = this.array.getValueType();
			final ArrayIR arrayIR =
					this.array.ir(valueType.irGenerator(dirs.getGenerator()));
			final ObjectOp array =
					this.array.getPrefix()
					.write(dirs.dirs(), host)
					.target()
					.materialize(
							dirs.dirs(),
							tempObjHolder(dirs.getAllocator()));
			final ObjectValFunc constructor =
					arrayIR.getConstructor().op(arrayIR.getId(), dirs.code());

			constructor.call(dirs, array);
		}

		@Override
		public String toString() {
			if (this.array == null) {
				return super.toString();
			}
			return this.array.toString();
		}

	}

}
