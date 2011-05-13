/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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

import static org.o42a.core.ir.op.CodeDirs.falseWhenUnknown;

import org.o42a.codegen.code.Code;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;


final class LogicalOr extends Logical {

	private final Logical[] variants;

	LogicalOr(LocationInfo location, Scope scope, Logical[] variants) {
		super(location, scope);
		this.variants = variants;
	}

	@Override
	public LogicalValue getConstantValue() {

		LogicalValue result = null;

		for (Logical variant : this.variants) {

			final LogicalValue value = variant.getConstantValue();

			if (value.isTrue()) {
				return value;
			}

			result = value.or(result);
		}

		return result;
	}

	@Override
	public LogicalValue logicalValue(Resolver resolver) {
		assertCompatible(resolver.getScope());

		LogicalValue result = null;

		for (Logical variant : this.variants) {

			final LogicalValue value = variant.logicalValue(resolver);

			if (value.isTrue()) {
				return value;
			}

			result = value.or(result);
		}

		return result;
	}

	@Override
	public Logical reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Logical[] variants = new Logical[this.variants.length];

		for (int i = 0; i < variants.length; ++i) {

			final Logical reproduced =
				this.variants[i].reproduce(reproducer);

			if (reproduced == null) {
				return null;
			}

			variants[i] = reproduced;
		}

		return new LogicalOr(this, reproducer.getScope(), variants);
	}

	@Override
	public void write(CodeDirs dirs, HostOp host) {
		assert assertFullyResolved();
		dirs = dirs.begin("or", "Logical OR: " + this);

		final Code code = dirs.code();

		Code block = code.addBlock("0_disj");

		code.go(block.head());

		for (int i = 0; i < this.variants.length; ++i) {

			final Code next;
			final CodeDirs blockDirs;

			if (i + 1 < this.variants.length) {
				next = code.addBlock((i + 1) + "_disj");
			} else {
				next = code.addBlock("all_false");
				dirs.goWhenFalse(next);
			}

			blockDirs = falseWhenUnknown(block, next.head());
			this.variants[i].write(blockDirs, host);
			block.go(code.tail());

			block = next;
		}

		dirs.end();
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append('(');
		for (Logical variant : this.variants) {
			if (out.length() > 1) {
				out.append(" | ");
			}
			out.append(variant);
		}
		out.append(')');

		return out.toString();
	}

	@Override
	protected Logical[] expandDisjunction() {
		return this.variants;
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		for (Logical variant : this.variants) {
			variant.resolveAll(resolver);
		}
	}

}
