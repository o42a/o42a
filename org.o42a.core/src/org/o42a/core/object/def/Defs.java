/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.core.object.def;

import static org.o42a.core.ir.def.InlineEval.noInlineEval;
import static org.o42a.core.st.DefValue.TRUE_DEF_VALUE;

import java.lang.reflect.Array;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.ObjectValue;
import org.o42a.core.object.def.impl.InlineDefs;
import org.o42a.core.object.value.ObjectValueDefs;
import org.o42a.core.ref.*;
import org.o42a.core.st.DefValue;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.ArrayUtil;


public final class Defs {

	private final Def[] defs;
	private DefValue constant;

	Defs(Def... defs) {
		this.defs = defs;
	}

	public final Def[] get() {
		return this.defs;
	}

	public final boolean isEmpty() {
		return this.defs.length == 0;
	}

	public final boolean isDefined() {
		for (Def def : this.defs) {
			if (def.isDefined()) {
				return true;
			}
		}
		return false;
	}

	public final boolean hasInherited() {
		for (Def def : this.defs) {
			if (def.isInherited()) {
				return true;
			}
			if (def.isDefined()) {
				// Return unconditionally. Never request an ancestor value.
				return false;
			}
		}
		return true;// Not explicitly defined. Can reuse the inherited value.
	}

	public final int length() {
		return this.defs.length;
	}

	public final DefTarget target() {
		for (Def def : get()) {

			final DefTarget target = def.target();

			if (target != null) {
				return target;
			}
		}
		return null;
	}

	public final DefValue getConstant() {
		if (this.constant != null) {
			return this.constant;
		}

		for (Def def : get()) {

			final DefValue constant = def.getConstantValue();

			if (constant.hasValue()) {
				return this.constant = constant;
			}
			if (!constant.getCondition().isTrue()) {
				return this.constant = constant;
			}
		}

		return this.constant = TRUE_DEF_VALUE;
	}

	public final DefValue value(Resolver resolver) {
		if (resolver.getScope().toObject().getConstructionMode().isRuntime()) {
			return DefValue.RUNTIME_DEF_VALUE;
		}
		for (Def def : get()) {

			final DefValue value = def.value(resolver);

			if (value.hasValue()) {
				return value;
			}
			if (!value.getCondition().isTrue()) {
				return value;
			}
		}

		return TRUE_DEF_VALUE;
	}

	public final boolean updatedSince(Obj ascendant) {

		final ObjectType ascendantType = ascendant.type();

		for (Def def : get()) {
			if (!ascendantType.derivedFrom(def.getSource().type())) {
				return true;
			}
		}

		return false;
	}

	public final boolean presentIn(Obj ascendant) {

		final ObjectType ascendantType = ascendant.type();

		for (Def def : get()) {
			if (ascendantType.derivedFrom(def.getSource().type())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		if (this.defs == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append("Defs[");
		defsToString(out, false);
		out.append(']');

		return out.toString();
	}

	final boolean assertValid(Scope scope) {
		for (Def def : this.defs) {
			def.assertScopeIs(scope);
		}
		return true;
	}

	final boolean defsToString(StringBuilder out, boolean comma) {

		boolean hasComma = comma;

		for (Def def : this.defs) {
			if (hasComma) {
				out.append(", ");
			} else {
				hasComma = true;
			}
			out.append(def);
		}

		return hasComma;
	}

	final Defs upgradeScope(ScopeUpgrade scopeUpgrade) {
		if (isEmpty()) {
			return this;
		}

		final Def[] items = get();

		for (int i = 0; i < items.length; ++i) {

			final Def def = items[i];
			final Def newDef = def.upgradeScope(scopeUpgrade);

			newDef.assertScopeIs(scopeUpgrade.getFinalScope());
			if (def == newDef) {
				continue;
			}

			final Def[] newItems = (Def[]) Array.newInstance(
					items.getClass().getComponentType(),
					items.length);

			System.arraycopy(items, 0, newItems, 0, i);
			newItems[i++] = newDef;
			for (;i < items.length; ++i) {
				newItems[i] = items[i].upgradeScope(scopeUpgrade);
			}

			return new Defs(newItems);
		}

		return this;
	}

	final Defs add(Def def) {
		return new Defs(ArrayUtil.append(this.defs, def));
	}

	final Defs override(Defs overriders) {
		if (!overriders.isDefined()) {
			return this;
		}
		return overriders;
	}

	InlineEval inline(Normalizer normalizer) {
		if (!isDefined()) {
			return noInlineEval();
		}

		final Def[] defs = get();
		final InlineEval[] inlines = new InlineEval[defs.length];

		for (int i = 0; i < defs.length; ++i) {
			inlines[i] = inlineDef(normalizer, defs[i]);
		}

		if (normalizer.isCancelled()) {
			return null;
		}

		return new InlineDefs(inlines);
	}

	boolean upgradeTypeParameters(
			Definitions definitions,
			TypeParameters<?> typeParameters) {

		boolean ok = true;

		for (Def def : get()) {

			final TypeParameters<?> defTypeParameters = def.getTypeParameters();

			if (defTypeParameters == null) {
				continue;
			}
			if (typeParameters.getValueType().isVoid()) {
				continue;
			}
			if (!typeParameters.assignableFrom(defTypeParameters)) {
				definitions.getLogger().incompatible(
						def.getLocation(),
						typeParameters);
			}
		}

		return ok;
	}

	final Defs toVoid() {

		final Def[] oldDefs = get();

		if (oldDefs.length == 0) {
			return this;
		}

		final Def[] newDefs = new Def[oldDefs.length];

		for (int i = 0; i < newDefs.length; ++i) {
			newDefs[i] = oldDefs[i].toVoid();
		}

		return new Defs(newDefs);
	}

	final void resolveTargets(TargetResolver wrapper) {
		for (Def def : get()) {
			def.resolveTarget(wrapper);
		}
	}

	void resolveAll(Definitions definitions) {
		validate(definitions.getTypeParameters());
		if (isEmpty()) {
			return;
		}

		final ObjectValue objectValue =
				definitions.getScope().toObject().value();
		final ObjectValueDefs part = objectValue.valueDefs();
		final FullResolver resolver = part.fullResolver();

		for (Def def : get()) {
			def.resolveAll(resolver);
		}
	}

	final void normalize(RootNormalizer normalizer) {
		for (Def def : get()) {
			def.normalize(normalizer);
		}
	}

	private void validate(TypeParameters<?> typeParameters) {
		for (Def def : get()) {

			final TypeParameters<?> defTypeParameters = def.getTypeParameters();

			if (defTypeParameters == null) {
				continue;
			}
			if (!typeParameters.assignableFrom(defTypeParameters)) {
				def.getContext().getLogger().incompatible(
						def.getLocation(),
						typeParameters);
			}
		}
	}

	private InlineEval inlineDef(Normalizer normalizer, Def def) {

		final InlineEval inline = def.inline(normalizer);

		if (inline == null) {
			normalizer.cancelAll();
		}

		return inline;
	}

}
