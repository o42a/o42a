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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodeBlk;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.LocationInfo;
import org.o42a.core.Scope;
import org.o42a.core.def.LogicalBase;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.common.AbstractConjunction;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;


public abstract class Logical extends LogicalBase {

	public static Logical logicalTrue(LocationInfo location, Scope scope) {
		return new True(location, scope);
	}

	public static Logical logicalFalse(LocationInfo location, Scope scope) {
		return new False(location, scope);
	}

	public static Logical runtimeTrue(LocationInfo location, Scope scope) {
		return new RuntimeTrue(location, scope);
	}

	public static Logical runtimeLogical(LocationInfo location, Scope scope) {
		return new Runtime(location, scope);
	}

	public static Logical and(Logical cond1, Logical cond2) {
		if (cond1 == null) {
			return cond2;
		}
		return cond1.and(cond2);
	}

	public static Logical or(Logical cond1, Logical cond2) {
		if (cond1 == null) {
			return cond2;
		}
		return cond1.or(cond2);
	}

	public static Logical conjunction(
			LocationInfo location,
			Scope scope,
			Logical... claims) {
		if (claims.length == 0) {
			return logicalFalse(location, scope);
		}
		if (claims.length == 1) {
			return claims[0];
		}

		final ArrayList<Logical> newClaims = new ArrayList<Logical>(claims.length);

		for (Logical claim : claims) {
			claim.assertCompatible(scope);

			final LogicalValue value = claim.getConstantValue();

			if (value == LogicalValue.FALSE) {
				return claim;
			}
			if (value.isConstant()) {
				continue;
			}

			final Logical[] expanded = claim.expandConjunction();

			if (expanded != null) {
				for (Logical c : expanded) {
					and(newClaims, c);
				}
			} else {
				and(newClaims, claim);
			}
		}

		final int size = newClaims.size();

		if (size == 1) {
			return newClaims.get(0);
		}

		return new And(
				location,
				scope,
				newClaims.toArray(new Logical[size]));
	}

	public static Logical conjunction(
			LocationInfo location,
			Scope scope,
			Collection<? extends Logical> claims) {
		return conjunction(
				location,
				scope,
				claims.toArray(new Logical[claims.size()]));
	}

	public static Logical disjunction(
			LocationInfo location,
			Scope scope,
			Logical... variants) {
		if (variants.length == 0) {
			return logicalFalse(location, scope);
		}
		if (variants.length == 1) {
			return variants[0];
		}

		final ArrayList<Logical> newVariants =
			new ArrayList<Logical>(variants.length);

		for (Logical variant : variants) {

			final LogicalValue value = variant.getConstantValue();

			if (value == LogicalValue.TRUE) {
				return variant;
			}
			if (value.isConstant()) {
				continue;
			}

			final Logical[] expanded = variant.expandDisjunction();

			if (expanded != null) {
				for (Logical v : expanded) {
					or(newVariants, v);
				}
			} else {
				or(newVariants, variant);
			}
		}

		final int size = newVariants.size();

		if (size == 1) {
			return newVariants.get(0);
		}

		return new Or(
				location,
				scope,
				newVariants.toArray(new Logical[size]));
	}

	public static Logical disjunction(
			LocationInfo location,
			Scope scope,
			Collection<? extends Logical> variants) {
		return disjunction(
				location,
				scope,
				variants.toArray(new Logical[variants.size()]));
	}

	private static void and(ArrayList<Logical> claims, Logical claim) {
		if (claims.isEmpty()) {
			claims.add(claim);
			return;
		}

		final Iterator<Logical> i = claims.iterator();

		while (i.hasNext()) {

			final Logical c1 = i.next();

			if (c1.requires(claim)) {
				return;
			}
			if (claim.requires(c1)) {
				i.remove();
				while (i.hasNext()) {

					final Logical c2 = i.next();

					if (claim.requires(c2)) {
						i.remove();
					}
				}

				break;
			}
		}

		claims.add(claim);
	}

	private static void or(ArrayList<Logical> variants, Logical variant) {
		if (variants.isEmpty()) {
			variants.add(variant);
			return;
		}

		final Iterator<Logical> i = variants.iterator();

		while (i.hasNext()) {

			final Logical c1 = i.next();

			if (c1.implies(variant)) {
				return;
			}
			if (variant.requires(c1)) {
				i.remove();
				while (i.hasNext()) {

					final Logical c2 = i.next();

					if (variant.implies(c2)) {
						i.remove();
					}
				}
				break;
			}
		}

		variants.add(variant);
	}

	private Logical negated;

	public Logical(LocationInfo location, Scope scope) {
		super(location, scope);
	}

	public final boolean isConstant() {
		return getConstantValue().isConstant();
	}

	public final boolean isTrue() {
		return getConstantValue().isTrue();
	}

	public final boolean isFalse() {
		return getConstantValue().isFalse();
	}

	public abstract LogicalValue getConstantValue();

	public abstract LogicalValue logicalValue(Scope scope);

	public abstract Logical reproduce(Reproducer reproducer);

	public final Logical or(Logical other) {
		if (other == null) {
			return this;
		}
		if (implies(other)) {
			return this;
		}
		if (requires(other)) {
			return other;
		}
		if (other.implies(this)) {
			return other;
		}
		if (other.requires(this)) {
			return this;
		}

		final Logical[] array = new Logical[2];

		array[0] = this;
		array[1] = other;

		return new Or(this, getScope(), array);
	}

	public final Logical and(Logical other) {
		if (other == null) {
			return this;
		}
		if (requires(other)) {
			return this;
		}
		if (implies(other)) {
			return other;
		}
		if (other.requires(this)) {
			return other;
		}
		if (other.implies(this)) {
			return this;
		}

		final Logical[] array = new Logical[2];

		array[0] = this;
		array[1] = other;

		return new And(this, getScope(), array);
	}

	public final Logical negate() {
		if (this.negated == null) {

			final LogicalValue logicalValue = getConstantValue();

			if (logicalValue.isConstant()) {
				if (logicalValue.isFalse()) {
					this.negated = logicalTrue(this, getScope());
				} else {
					this.negated = logicalFalse(this, getScope());
				}
			} else {
				this.negated = new Negated(this);
				this.negated.negated = this;
			}
		}

		return this.negated;
	}

	public boolean sameAs(Logical other) {
		if (equals(other)) {
			return true;
		}

		final LogicalValue value = getConstantValue();

		if (value == LogicalValue.RUNTIME) {
			return false;
		}

		return value == other.getConstantValue();
	}

	public final boolean implies(Logical other) {
		if (equals(other)) {
			return true;
		}

		final LogicalValue value = getConstantValue();

		switch (value) {
		case TRUE:
			return true;
		case RUNTIME:
			if (runtimeImplies(other)) {
				return true;
			}
			return other.runtimeRequires(this);
		case FALSE:
		}
		return other.isFalse();
	}

	public final boolean requires(Logical other) {
		if (equals(other)) {
			return true;
		}

		final LogicalValue value = getConstantValue();

		switch (value) {
		case FALSE:
			return true;
		case RUNTIME:
			if (runtimeRequires(other)) {
				return true;
			}
			return other.runtimeImplies(this);
		case TRUE:
		}
		return other.isTrue();
	}

	public Logical rescope(Rescoper rescoper) {
		return new RescopedLogical(this, rescoper);
	}

	public abstract void write(Code code, CodePos exit, HostOp host);

	protected boolean runtimeImplies(Logical other) {

		final Logical[] disjunction = expandDisjunction();

		if (disjunction == null) {
			return false;
		}
		for (Logical variant : disjunction) {
			if (variant.implies(other)) {
				return true;
			}
		}

		return false;
	}

	protected boolean runtimeRequires(Logical other) {

		final Logical[] conjunction = expandConjunction();

		if (conjunction == null) {
			return false;
		}
		for (Logical claim : conjunction) {
			if (claim.requires(other)) {
				return true;
			}
		}

		return false;
	}

	protected Logical[] expandConjunction() {
		return null;
	}

	protected Logical[] expandDisjunction() {
		return null;
	}

	private static final class True extends Logical {

		True(LocationInfo location, Scope scope) {
			super(location, scope);
		}

		@Override
		public LogicalValue getConstantValue() {
			return LogicalValue.TRUE;
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			assertCompatible(scope);
			return LogicalValue.TRUE;
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());
			return new True(this, reproducer.getScope());
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			code.debug("Logical: TRUE");
		}

		@Override
		public String toString() {
			return "TRUE";
		}

	}

	private static final class False extends Logical {

		False(LocationInfo location, Scope scope) {
			super(location, scope);
		}

		@Override
		public LogicalValue getConstantValue() {
			return LogicalValue.FALSE;
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			assertCompatible(scope);
			return LogicalValue.FALSE;
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());
			return new False(this, reproducer.getScope());
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			code.debug("Logical: FALSE");
			code.go(exit);
		}

		@Override
		public String toString() {
			return "FALSE";
		}

	}

	private static final class Runtime extends Logical {

		Runtime(LocationInfo location, Scope scope) {
			super(location, scope);
		}

		@Override
		public LogicalValue getConstantValue() {
			return LogicalValue.RUNTIME;
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			assertCompatible(scope);
			return LogicalValue.RUNTIME;
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());
			return new Runtime(this, reproducer.getScope());
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			throw new UnsupportedOperationException(
					"Abstract run-time logical should not generate any code");
		}

		@Override
		public String toString() {
			return "RUN-TIME";
		}

	}

	private static final class RuntimeTrue extends Logical {

		RuntimeTrue(LocationInfo location, Scope scope) {
			super(location, scope);
		}

		@Override
		public LogicalValue getConstantValue() {
			return LogicalValue.RUNTIME;
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			assertCompatible(scope);
			return LogicalValue.RUNTIME;
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());
			return new Runtime(this, reproducer.getScope());
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			code.debug("Logical: " + this);
		}

		@Override
		public String toString() {
			return "RUN-TIME TRUE";
		}

	}

	private static final class Or extends Logical {

		private final Logical[] variants;

		Or(LocationInfo location, Scope scope, Logical[] variants) {
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
		public LogicalValue logicalValue(Scope scope) {
			assertCompatible(scope);

			LogicalValue result = null;

			for (Logical variant : this.variants) {

				final LogicalValue value = variant.logicalValue(scope);

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

			return new Or(this, reproducer.getScope(), variants);
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			code.debug("Logical: " + this);

			Code block = code.addBlock("0_or");

			code.go(block.head());

			for (int i = 0; i < this.variants.length; ++i) {

				final Code next;
				final CodePos nextPos;

				if (i + 1 < this.variants.length) {
					next = code.addBlock((i + 1) + "_or");
					nextPos = next.head();
				} else {
					next = null;
					nextPos = exit;
				}

				this.variants[i].write(block, nextPos, host);
				block.go(code.tail());

				block = next;
			}
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

	}

	private static final class And extends AbstractConjunction {

		private final Logical[] claims;

		And(LocationInfo location, Scope scope, Logical[] claims) {
			super(location, scope);
			this.claims = claims;
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());

			final Logical[] claims = new Logical[this.claims.length];

			for (int i = 0; i < claims.length; ++i) {

				final Logical reproduced =
					this.claims[i].reproduce(reproducer);

				if (reproduced == null) {
					return null;
				}

				claims[i] = reproduced;
			}

			return new And(this, reproducer.getScope(), claims);
		}

		@Override
		protected Logical[] expandConjunction() {
			return this.claims;
		}

		@Override
		protected int numClaims() {
			return this.claims.length;
		}

		@Override
		protected Logical claim(int index) {
			return this.claims[index];
		}

	}

	private static final class Negated extends Logical {

		Negated(Logical original) {
			super(original, original.getScope());
		}

		@Override
		public LogicalValue getConstantValue() {
			return negate().getConstantValue().negate();
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			assertCompatible(scope);
			return negate().logicalValue(scope).negate();
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());

			final Logical reproduced = negate().reproduce(reproducer);

			if (reproduced == null) {
				return null;
			}

			return reproduced.negate();
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			code.debug("Logical: " + this);

			final CodeBlk isTrue = code.addBlock("is_true");

			negate().write(code, isTrue.head(), host);
			code.go(exit);

			if (isTrue.exists()) {
				isTrue.go(code.tail());
			}
		}

		@Override
		public String toString() {
			return "--" + negate();
		}

	}

}
