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
package org.o42a.core.def;

import static java.lang.System.arraycopy;
import static org.o42a.core.def.DefKind.PROPOSITION;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.o42a.core.Scope;
import org.o42a.core.artifact.object.ObjectValue;
import org.o42a.core.artifact.object.ValuePart;
import org.o42a.core.ref.Resolver;
import org.o42a.util.ArrayUtil;


public abstract class Defs<D extends Def<D>, S extends Defs<D, S>> {

	private final DefKind defKind;
	private final D[] defs;

	Defs(DefKind defKind, D[] defs) {
		this.defKind = defKind;
		this.defs = defs;
	}

	public final DefKind getDefKind() {
		return this.defKind;
	}

	public final D[] get() {
		return this.defs;
	}

	public final boolean isEmpty() {
		return this.defs.length == 0;
	}

	public final int length() {
		return this.defs.length;
	}

	@Override
	public String toString() {
		if (this.defs == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append(this.defKind.displayName());
		out.append("s[");
		defsToString(out, false);
		out.append(']');

		return out.toString();
	}

	@SuppressWarnings("unchecked")
	final S self() {
		return (S) this;
	}

	abstract S create(DefKind defKind, D[] defs);

	final boolean assertValid(Scope scope, DefKind defKind) {
		assert getDefKind() == defKind :
			"Wrong definition kind: " + getDefKind()
			+ ", but " + defKind + " expected";
		for (Def<?> def : this.defs) {
			def.assertScopeIs(scope);
		}
		return true;
	}

	final boolean defsToString(StringBuilder out, boolean comma) {

		boolean hasComma = comma;

		for (Def<?> def : this.defs) {
			if (hasComma) {
				out.append(", ");
			} else {
				hasComma = true;
			}
			out.append(def);
		}

		return hasComma;
	}

	final boolean imply(D def) {
		for (D d : get()) {
			if (d.impliesWhenBefore(def)) {
				return true;
			}
		}
		return false;
	}

	final S removeImpliedBy(S existing) {

		final int len = this.defs.length;
		@SuppressWarnings("unchecked")
		final D[] newItems = (D[]) Array.newInstance(
				this.defs.getClass().getComponentType(),
				len);
		int idx = 0;

		for (D def : this.defs) {
			if (!existing.imply(def)) {
				newItems[idx++] = def;
			}
		}
		if (idx == len) {
			return self();
		}

		return create(getDefKind(), ArrayUtil.clip(newItems, idx));
	}

	final S addClaims(S defs) {

		final D[] claims = get();
		final int len = claims.length;

		if (len == 0) {
			return defs;
		}

		final D[] items = defs.get();
		@SuppressWarnings("unchecked")
		final D[] newClaims = (D[]) Array.newInstance(
				claims.getClass().getComponentType(),
				len + items.length);
		int idx = 0;

		for (D def : items) {
			for (int i = 0; i < len; ++i) {

				final D c1 = claims[i];

				if (c1.impliesWhenBefore(def)) {
					if (items.length == 1) {
						return self();
					}
				} else if (def.impliesWhenAfter(c1)) {
					++i;
					for (; i < len; ++i) {

						final D c2 = claims[i];

						if (def.impliesWhenAfter(c2)) {
							continue;
						}
						newClaims[idx++] = c2;
					}
					newClaims[idx++] = def;
					break;
				}
				newClaims[idx++] = c1;
			}
		}

		return create(getDefKind().unclaim(), ArrayUtil.clip(newClaims, idx));
	}

	final S addPropositions(S claims, S defs) {

		final D[] propositions = get();
		final int len = propositions.length;

		if (len == 0) {
			return defs;
		}

		final D[] items = defs.get();
		final D[] newPropositions =
				Arrays.copyOf(propositions, len + items.length);
		int idx = propositions.length;

		for (D proposition : items) {
			if (imply(proposition)) {
				continue;
			}
			if (claims.imply(proposition)) {
				continue;
			}
			newPropositions[idx++] = proposition;
		}

		return create(getDefKind(), ArrayUtil.clip(newPropositions, idx));
	}

	final S claim(S claims) {
		if (isEmpty()) {
			return claims;
		}

		final D[] propositions = get();
		final D[] oldClaims = claims.get();
		final D[] newClaims = Arrays.copyOf(
				oldClaims,
				oldClaims.length + propositions.length);

		int idx = oldClaims.length;

		for (D proposition : propositions) {
			newClaims[idx++] = proposition.claim();
		}

		return create(getDefKind().claim(), newClaims);
	}

	final S unclaim(S propositions) {
		if (isEmpty()) {
			return propositions;
		}

		final D[] oldPropositions = propositions.get();
		final D[] claims = get();
		@SuppressWarnings("unchecked")
		final D[] newPropositions = (D[]) Array.newInstance(
				propositions.getClass().getComponentType(),
				claims.length + oldPropositions.length);

		int idx = 0;

		for (D claim : claims) {
			newPropositions[idx++] = claim.unclaim();
		}

		arraycopy(
				oldPropositions,
				0,
				newPropositions,
				claims.length,
				oldPropositions.length);

		return create(PROPOSITION, newPropositions);
	}

	final S rescope(Rescoper rescoper) {
		if (isEmpty()) {
			return self();
		}

		final D[] items = get();

		for (int i = 0; i < items.length; ++i) {

			final D def = items[i];
			final D newDef = rescoper.updateDef(def);

			newDef.assertScopeIs(rescoper.getFinalScope());
			if (def == newDef) {
				continue;
			}

			@SuppressWarnings("unchecked")
			final D[] newItems = (D[]) Array.newInstance(
					items.getClass().getComponentType(),
					items.length);

			System.arraycopy(items, 0, newItems, 0, i);
			newItems[i++] = newDef;
			for (;i < items.length; ++i) {
				newItems[i] = rescoper.updateDef(items[i]);
			}

			return create(getDefKind(), newItems);
		}

		return self();
	}

	final void resolveAll(Definitions definitions) {

		final ObjectValue objectValue =
				definitions.getScope().toObject().value();
		final ValuePart part = objectValue.part(getDefKind());
		final Resolver resolver = part.resolver();

		for (Def<?> def : this.defs) {
			def.resolveAll(resolver);
		}
	}

}
