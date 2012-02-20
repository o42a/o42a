/*
    Compiler Core
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
package org.o42a.core.ref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.o42a.core.Scope;
import org.o42a.core.Scoped;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.object.def.CondDef;
import org.o42a.core.object.def.impl.LogicalCondDef;
import org.o42a.core.ref.impl.logical.*;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.LogicalValue;


public abstract class Logical extends Scoped {

	public static Logical logicalTrue(LocationInfo location, Scope scope) {
		return new LogicalTrue(location, scope);
	}

	public static Logical logicalFalse(LocationInfo location, Scope scope) {
		return new LogicalFalse(location, scope);
	}

	public static Logical runtimeTrue(LocationInfo location, Scope scope) {
		return new RuntimeLogicalTrue(location, scope);
	}

	public static Logical runtimeLogical(LocationInfo location, Scope scope) {
		return new RuntimeLogical(location, scope);
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
			Logical... requirements) {
		if (requirements.length == 0) {
			return logicalFalse(location, scope);
		}
		if (requirements.length == 1) {
			return requirements[0];
		}

		final ArrayList<Logical> newRequirements =
				new ArrayList<Logical>(requirements.length);

		for (Logical requirement : requirements) {
			requirement.assertCompatible(scope);

			final LogicalValue value = requirement.getConstantValue();

			if (value == LogicalValue.FALSE) {
				return requirement;
			}
			if (value.isConstant()) {
				continue;
			}

			final Logical[] expanded = requirement.expandConjunction();

			if (expanded != null) {
				for (Logical c : expanded) {
					and(newRequirements, c);
				}
			} else {
				and(newRequirements, requirement);
			}
		}

		final int size = newRequirements.size();

		if (size == 1) {
			return newRequirements.get(0);
		}

		return new LogicalAnd(
				location,
				scope,
				newRequirements.toArray(new Logical[size]));
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

		return new LogicalOr(
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
	private boolean fullyResolved;

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

	public abstract LogicalValue logicalValue(Resolver resolver);

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

		return new LogicalOr(this, getScope(), array);
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

		return new LogicalAnd(this, getScope(), array);
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
				this.negated = new NegatedLogical(this);
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

	public final CondDef toCondDef() {
		return new LogicalCondDef(this);
	}

	public Logical upgradeScope(ScopeUpgrade scopeUpgrade) {
		return new RescopedLogical(this, scopeUpgrade);
	}

	public final void resolveAll(Resolver resolver) {
		this.fullyResolved = true;
		getContext().fullResolution().start();
		try {
			fullyResolve(resolver);
		} finally {
			getContext().fullResolution().end();
		}
	}

	public abstract Logical reproduce(Reproducer reproducer);

	public abstract InlineCond inline(Normalizer normalizer, Scope origin);

	public abstract void write(CodeDirs dirs, HostOp host);

	public final boolean assertFullyResolved() {
		assert this.fullyResolved :
			this + " is not fully resolved";
		return true;
	}

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

	protected abstract void fullyResolve(Resolver resolver);

}
