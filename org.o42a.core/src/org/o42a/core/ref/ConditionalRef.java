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
package org.o42a.core.ref;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.Value;


final class ConditionalRef extends Ref {

	private final Ref ref;
	private final Logical condition;

	ConditionalRef(Ref ref, Logical condition) {
		super(ref, ref.distribute());
		this.ref = ref;
		this.condition = condition;
	}

	@Override
	public Resolution resolve(Scope scope) {
		return this.ref.resolve(scope);
	}

	@Override
	public Value<?> value(Scope scope) {
		return this.ref.value(scope).require(
				this.condition.logicalValue(scope));
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Ref reproducedRef = this.ref.reproduce(reproducer);

		if (reproducedRef == null) {
			return null;
		}

		final Logical reproduction = this.condition.reproduce(reproducer);

		if (reproduction == null) {
			return null;
		}

		return new ConditionalRef(reproducedRef, reproduction);
	}

	@Override
	public String toString() {
		return this.condition + " ? " + this.ref;
	}

	@Override
	protected RefOp createOp(HostOp host) {
		return new Op(host, this);
	}

	private static final class Op extends RefOp {

		private Op(HostOp host, ConditionalRef ref) {
			super(host, ref);
		}

		@Override
		public void writeLogicalValue(Code code, CodePos exit) {
			ref().condition.write(code, exit, host());
			super.writeLogicalValue(code, exit);
		}

		@Override
		public void writeValue(Code code, CodePos exit, ValOp result) {
			if (exit != null) {
				ref().condition.write(code, exit, host());
				super.writeValue(code, exit, result);
				return;
			}

			final CodeBlk preconditionFailure =
				code.addBlock("precondition_failure");

			ref().condition.write(code, preconditionFailure.head(), host());
			super.writeValue(code, null, result);

			if (preconditionFailure.exists()) {
				preconditionFailure.go(code.tail());
			}
		}

		@Override
		public HostOp target(Code code, CodePos exit) {
			return ref().ref.op(host()).target(code, exit);
		}

		private final ConditionalRef ref() {
			return (ConditionalRef) getRef();
		}

	}

}
