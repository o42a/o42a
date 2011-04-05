/*
    Compiler Core
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
package org.o42a.core.value;

import org.o42a.codegen.code.Code;
import org.o42a.core.Distributor;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.common.Expression;
import org.o42a.core.st.Reproducer;


final class DefiniteRef<T> extends Expression {

	private final ValueType<T> valueType;
	private final T value;

	DefiniteRef(
			LocationInfo location,
			Distributor distributor,
			ValueType<T> valueType,
			T value) {
		super(location, distributor);
		this.valueType = valueType;
		this.value = value;
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());
		return new DefiniteRef<T>(
				this,
				reproducer.distribute(),
				this.valueType,
				this.value);
	}

	@Override
	protected Resolution resolveExpression(Scope scope) {
		return objectResolution(new DefiniteObject<T>(
				this,
				this.distributeIn(scope.getContainer()),
				this.valueType,
				this.value));
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new Op<T>(host, this);
	}

	private static final class Op<T> extends RefOp {

		Op(HostOp host, DefiniteRef<T> ref) {
			super(host, ref);
		}

		@Override
		public void writeLogicalValue(CodeDirs dirs) {
		}

		@Override
		public void writeValue(CodeDirs dirs, ValOp result) {

			@SuppressWarnings("unchecked")
			final DefiniteRef<T> ref = (DefiniteRef<T>) getRef();
			final Code code = dirs.code();

			result.store(
					code,
					ref.valueType.val(getGenerator(), ref.value));
			result.go(code, dirs);
		}

		@Override
		public HostOp target(CodeDirs dirs) {

			@SuppressWarnings("unchecked")
			final DefiniteRef<T> ref = (DefiniteRef<T>) getRef();
			final ObjectIR ir =
				ref.getResolution().toObject().ir(getGenerator());

			return ir.op(getBuilder(), dirs.code());
		}

	}

}
