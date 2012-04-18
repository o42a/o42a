/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ref.impl.logical;

import org.o42a.codegen.code.Block;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.InlineCond;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;
import org.o42a.util.fn.Cancelable;


public final class LogicalOr extends Logical {

	private final Logical[] options;

	public LogicalOr(LocationInfo location, Scope scope, Logical[] options) {
		super(location, scope);
		this.options = options;
	}

	@Override
	public LogicalValue getConstantValue() {

		LogicalValue result = null;

		for (Logical option : this.options) {

			final LogicalValue value = option.getConstantValue();

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

		for (Logical option : this.options) {

			final LogicalValue value = option.logicalValue(resolver);

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

		final Logical[] options = new Logical[this.options.length];

		for (int i = 0; i < options.length; ++i) {

			final Logical reproduced =
					this.options[i].reproduce(reproducer);

			if (reproduced == null) {
				return null;
			}

			options[i] = reproduced;
		}

		return new LogicalOr(this, reproducer.getScope(), options);
	}

	@Override
	public InlineCond inline(Normalizer normalizer, Scope origin) {

		final InlineCond[] inlines = new InlineCond[this.options.length];

		for (int i = 0; i < inlines.length; ++i) {
			inlines[i] = this.options[i].inline(normalizer, origin);
		}

		return normalizer.isCancelled() ? null : new Inline(inlines);
	}

	@Override
	public void write(CodeDirs dirs, HostOp host) {
		assert assertFullyResolved();

		final CodeDirs subDirs = dirs.begin("or", "Logical OR: " + this);
		final Block code = subDirs.code();

		Block block = code.addBlock("0_disj");

		code.go(block.head());

		for (int i = 0; i < this.options.length; ++i) {

			final Block next;
			final CodeDirs blockDirs;

			if (i + 1 < this.options.length) {
				next = code.addBlock((i + 1) + "_disj");
			} else {
				next = code.addBlock("all_false");
				next.go(subDirs.falseDir());
			}

			blockDirs = dirs.getBuilder().falseWhenUnknown(block, next.head());
			this.options[i].write(blockDirs, host);
			if (block.exists()) {
				block.go(code.tail());
			}

			block = next;
		}

		subDirs.end();
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append('(').append(this.options[0]);
		for (int i = 1; i < this.options.length; ++i) {
			out.append("; ").append(this.options[i]);
		}
		out.append(')');

		return out.toString();
	}

	@Override
	protected Logical[] expandDisjunction() {
		return this.options;
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		for (Logical variant : this.options) {
			variant.resolveAll(resolver);
		}
	}

	private static final class Inline extends InlineCond {

		private final InlineCond[] options;

		Inline(InlineCond[] options) {
			super(null);
			this.options = options;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {

			final CodeDirs subDirs =
					dirs.begin("or", "In-line logical OR: " + this);
			final Block code = subDirs.code();

			Block block = code.addBlock("0_disj");

			code.go(block.head());

			for (int i = 0; i < this.options.length; ++i) {

				final Block next;
				final CodeDirs blockDirs;

				if (i + 1 < this.options.length) {
					next = code.addBlock((i + 1) + "_disj");
				} else {
					next = code.addBlock("all_false");
					next.go(subDirs.falseDir());
				}

				blockDirs =
						dirs.getBuilder().falseWhenUnknown(block, next.head());
				this.options[i].writeCond(blockDirs, host);
				if (block.exists()) {
					block.go(code.tail());
				}

				block = next;
			}

			subDirs.end();
		}

		@Override
		public String toString() {

			final StringBuilder out = new StringBuilder();

			out.append('(').append(this.options[0]);
			for (int i = 1; i < this.options.length; ++i) {
				out.append("; ").append(this.options[i]);
			}
			out.append(')');

			return out.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}
