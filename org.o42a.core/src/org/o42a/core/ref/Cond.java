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
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.def.CondBase;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ref.common.AbstractConjunction;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.*;
import org.o42a.core.value.Void;


public abstract class Cond extends CondBase {

	public static Cond trueCondition(LocationSpec location, Scope scope) {
		return new True(location, scope);
	}

	public static Cond falseCondition(LocationSpec location, Scope scope) {
		return new False(location, scope);
	}

	public static Cond runtimeCondition(LocationSpec location, Scope scope) {
		return new Runtime(location, scope);
	}

	public static Cond and(Cond cond1, Cond cond2) {
		if (cond1 == null) {
			return cond2;
		}
		return cond1.and(cond2);
	}

	public static Cond or(Cond cond1, Cond cond2) {
		if (cond1 == null) {
			return cond2;
		}
		return cond1.or(cond2);
	}

	public static Cond conjunction(
			LocationSpec location,
			Scope scope,
			Cond... claims) {
		if (claims.length == 0) {
			return falseCondition(location, scope);
		}
		if (claims.length == 1) {
			return claims[0];
		}

		final ArrayList<Cond> newClaims = new ArrayList<Cond>(claims.length);

		for (Cond claim : claims) {
			claim.assertCompatible(scope);

			final LogicalValue value = claim.getConstantValue();

			if (value == LogicalValue.FALSE) {
				return claim;
			}
			if (value.isConstant()) {
				continue;
			}

			final Cond[] expanded = claim.expandConjunction();

			if (expanded != null) {
				for (Cond c : expanded) {
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
				newClaims.toArray(new Cond[size]));
	}

	public static Cond conjunction(
			LocationSpec location,
			Scope scope,
			Collection<? extends Cond> claims) {
		return conjunction(
				location,
				scope,
				claims.toArray(new Cond[claims.size()]));
	}

	public static Cond disjunction(
			LocationSpec location,
			Scope scope,
			Cond... variants) {
		if (variants.length == 0) {
			return falseCondition(location, scope);
		}
		if (variants.length == 1) {
			return variants[0];
		}

		final ArrayList<Cond> newVariants =
			new ArrayList<Cond>(variants.length);

		for (Cond variant : variants) {

			final LogicalValue value = variant.getConstantValue();

			if (value == LogicalValue.TRUE) {
				return variant;
			}
			if (value.isConstant()) {
				continue;
			}

			final Cond[] expanded = variant.expandDisjunction();

			if (expanded != null) {
				for (Cond v : expanded) {
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
				newVariants.toArray(new Cond[size]));
	}

	public static Cond disjunction(
			LocationSpec location,
			Scope scope,
			Collection<? extends Cond> variants) {
		return disjunction(
				location,
				scope,
				variants.toArray(new Cond[variants.size()]));
	}

	private static void and(ArrayList<Cond> claims, Cond claim) {
		if (claims.isEmpty()) {
			claims.add(claim);
			return;
		}

		final Iterator<Cond> i = claims.iterator();

		while (i.hasNext()) {

			final Cond c1 = i.next();

			if (c1.requires(claim)) {
				return;
			}
			if (claim.requires(c1)) {
				i.remove();
				while (i.hasNext()) {

					final Cond c2 = i.next();

					if (claim.requires(c2)) {
						i.remove();
					}
				}

				break;
			}
		}

		claims.add(claim);
	}

	private static void or(ArrayList<Cond> variants, Cond variant) {
		if (variants.isEmpty()) {
			variants.add(variant);
			return;
		}

		final Iterator<Cond> i = variants.iterator();

		while (i.hasNext()) {

			final Cond c1 = i.next();

			if (c1.implies(variant)) {
				return;
			}
			if (variant.requires(c1)) {
				i.remove();
				while (i.hasNext()) {

					final Cond c2 = i.next();

					if (variant.implies(c2)) {
						i.remove();
					}
				}
				break;
			}
		}

		variants.add(variant);
	}

	private Cond negated;

	public Cond(LocationSpec location, Scope scope) {
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

	public abstract Cond reproduce(Reproducer reproducer);

	public final Cond or(Cond other) {
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

		final Cond[] array = new Cond[2];

		array[0] = this;
		array[1] = other;

		return new Or(this, getScope(), array);
	}

	public final Cond and(Cond other) {
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

		final Cond[] array = new Cond[2];

		array[0] = this;
		array[1] = other;

		return new And(this, getScope(), array);
	}

	public final Cond negate() {
		if (this.negated == null) {

			final LogicalValue logicalValue = getConstantValue();

			if (logicalValue.isConstant()) {
				if (logicalValue.isFalse()) {
					this.negated = trueCondition(this, getScope());
				} else {
					this.negated = falseCondition(this, getScope());
				}
			} else {
				this.negated = new Negated(this);
				this.negated.negated = this;
			}
		}

		return this.negated;
	}

	public boolean sameAs(Cond other) {
		if (equals(other)) {
			return true;
		}

		final LogicalValue value = getConstantValue();

		if (value == LogicalValue.RUNTIME) {
			return false;
		}

		return value == other.getConstantValue();
	}

	public final boolean implies(Cond other) {
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

	public final boolean requires(Cond other) {
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

	public Value<Void> toValue() {

		final LogicalValue logicalValue = getConstantValue();

		if (!logicalValue.isConstant()) {
			return ValueType.VOID.runtimeValue();
		}
		if (logicalValue.isTrue()) {
			return Value.voidValue();
		}
		return Value.falseValue();
	}

	public abstract void write(Code code, CodePos exit, HostOp host);

	protected boolean runtimeImplies(Cond other) {

		final Cond[] disjunction = expandDisjunction();

		if (disjunction == null) {
			return false;
		}
		for (Cond variant : disjunction) {
			if (variant.implies(other)) {
				return true;
			}
		}

		return false;
	}

	protected boolean runtimeRequires(Cond other) {

		final Cond[] conjunction = expandConjunction();

		if (conjunction == null) {
			return false;
		}
		for (Cond claim : conjunction) {
			if (claim.requires(other)) {
				return true;
			}
		}

		return false;
	}

	protected Cond[] expandConjunction() {
		return null;
	}

	protected Cond[] expandDisjunction() {
		return null;
	}

	private static final class True extends Cond {

		True(LocationSpec location, Scope scope) {
			super(location, scope);
		}

		@Override
		public LogicalValue getConstantValue() {
			return LogicalValue.TRUE;
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			return LogicalValue.TRUE;
		}

		@Override
		public Cond reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());
			return new True(this, reproducer.getScope());
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			code.debug("Cond: " + this);
		}

		@Override
		public String toString() {
			return "TRUE";
		}

	}

	private static final class False extends Cond {

		False(LocationSpec location, Scope scope) {
			super(location, scope);
		}

		@Override
		public LogicalValue getConstantValue() {
			return LogicalValue.FALSE;
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			return LogicalValue.FALSE;
		}

		@Override
		public Cond reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());
			return new False(this, reproducer.getScope());
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			code.debug("Cond: FALSE");
			code.go(exit);
		}

		@Override
		public String toString() {
			return "FALSE";
		}

	}

	private static final class Runtime extends Cond {

		Runtime(LocationSpec location, Scope scope) {
			super(location, scope);
		}

		@Override
		public LogicalValue getConstantValue() {
			return LogicalValue.RUNTIME;
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			return LogicalValue.RUNTIME;
		}

		@Override
		public Cond reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());
			return new Runtime(this, reproducer.getScope());
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			code.debug("Cond: " + this);
		}

		@Override
		public String toString() {
			return "RUNTIME";
		}

	}

	private static final class Or extends Cond {

		private final Cond[] variants;

		Or(LocationSpec location, Scope scope, Cond[] variants) {
			super(location, scope);
			this.variants = variants;
		}

		@Override
		public LogicalValue getConstantValue() {

			LogicalValue result = null;

			for (Cond variant : this.variants) {

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

			LogicalValue result = null;

			for (Cond variant : this.variants) {

				final LogicalValue value = variant.logicalValue(scope);

				if (value.isTrue()) {
					return value;
				}

				result = value.or(result);
			}

			return result;
		}

		@Override
		public Cond reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());

			final Cond[] variants = new Cond[this.variants.length];

			for (int i = 0; i < variants.length; ++i) {

				final Cond reproduced =
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
			code.debug("Cond: " + this);

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
			for (Cond variant : this.variants) {
				if (out.length() > 1) {
					out.append(" | ");
				}
				out.append(variant);
			}
			out.append(')');

			return out.toString();
		}

		@Override
		protected Cond[] expandDisjunction() {
			return this.variants;
		}

	}

	private static final class And extends AbstractConjunction {

		private final Cond[] claims;

		And(LocationSpec location, Scope scope, Cond[] claims) {
			super(location, scope);
			this.claims = claims;
		}

		@Override
		public Cond reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());

			final Cond[] claims = new Cond[this.claims.length];

			for (int i = 0; i < claims.length; ++i) {

				final Cond reproduced =
					this.claims[i].reproduce(reproducer);

				if (reproduced == null) {
					return null;
				}

				claims[i] = reproduced;
			}

			return new And(this, reproducer.getScope(), claims);
		}

		@Override
		protected Cond[] expandConjunction() {
			return this.claims;
		}

		@Override
		protected int numClaims() {
			return this.claims.length;
		}

		@Override
		protected Cond claim(int index) {
			return this.claims[index];
		}

	}

	private static final class Negated extends Cond {

		Negated(Cond original) {
			super(original, original.getScope());
		}

		@Override
		public LogicalValue getConstantValue() {
			return negate().getConstantValue().negate();
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			return negate().logicalValue(scope).negate();
		}

		@Override
		public Cond reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());

			final Cond reproduced = negate().reproduce(reproducer);

			if (reproduced == null) {
				return null;
			}

			return reproduced.negate();
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			code.debug("Cond: " + this);

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
