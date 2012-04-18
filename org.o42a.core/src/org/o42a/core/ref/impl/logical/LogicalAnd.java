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


public final class LogicalAnd extends Logical {

	private final Logical[] requirements;

	public LogicalAnd(
			LocationInfo location,
			Scope scope,
			Logical[] requirements) {
		super(location, scope);
		this.requirements = requirements;
	}

	@Override
	public LogicalValue getConstantValue() {

		LogicalValue result = null;

		for (Logical requirement : this.requirements) {

			final LogicalValue value = requirement.getConstantValue();

			if (value.isFalse()) {
				return value;
			}
			result = value.and(result);
		}

		return result;
	}

	@Override
	public LogicalValue logicalValue(Resolver resolver) {
		assertCompatible(resolver.getScope());

		LogicalValue result = null;

		for (Logical requirement : this.requirements) {

			final LogicalValue value = requirement.logicalValue(resolver);

			if (value.isFalse()) {
				return value;
			}
			result = value.and(result);
		}

		return result;
	}

	@Override
	public Logical reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final Logical[] requirements = new Logical[this.requirements.length];

		for (int i = 0; i < requirements.length; ++i) {

			final Logical reproduced =
					this.requirements[i].reproduce(reproducer);

			if (reproduced == null) {
				return null;
			}

			requirements[i] = reproduced;
		}

		return new LogicalAnd(this, reproducer.getScope(), requirements);
	}

	@Override
	public InlineCond inline(Normalizer normalizer, Scope origin) {

		final InlineCond[] inlines = new InlineCond[this.requirements.length];

		for (int i = 0; i < inlines.length; ++i) {
			inlines[i] = this.requirements[i].inline(normalizer, origin);
		}

		return normalizer.isCancelled() ? null : new Inline(inlines);
	}

	@Override
	public void write(CodeDirs dirs, HostOp host) {
		assert assertFullyResolved();

		final CodeDirs subDirs = dirs.begin("and", "Logical AND: " + this);

		for (Logical requirement : this.requirements) {
			requirement.write(subDirs, host);
		}

		subDirs.end();
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append('(').append(this.requirements[0]);
		for (int i = 1; i < this.requirements.length; ++i) {
			out.append(", ").append(this.requirements[i]);
		}
		out.append(')');

		return out.toString();
	}

	@Override
	protected Logical[] expandConjunction() {
		return this.requirements;
	}

	@Override
	protected void fullyResolve(Resolver resolver) {
		for (Logical requirement : expandConjunction()) {
			requirement.resolveAll(resolver);
		}
	}

	private static final class Inline extends InlineCond {

		private final InlineCond[] requirements;

		Inline(InlineCond[] requirements) {
			super(null);
			this.requirements = requirements;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {

			final CodeDirs subDirs =
					dirs.begin("and", "In-line logical AND: " + this);

			for (InlineCond requirement : this.requirements) {
				requirement.writeCond(subDirs, host);
			}

			subDirs.end();
		}

		@Override
		public String toString() {

			final StringBuilder out = new StringBuilder();

			out.append('(').append(this.requirements[0]);
			for (int i = 1; i < this.requirements.length; ++i) {
				out.append(", ").append(this.requirements[i]);
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
