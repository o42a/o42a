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
package org.o42a.core.value;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ref.*;
import org.o42a.core.value.impl.RawValueAdapter;
import org.o42a.core.value.link.TargetResolver;


public abstract class ValueAdapter {

	public static ValueAdapter rawValueAdapter(Ref adaptedRef) {
		return new RawValueAdapter(adaptedRef);
	}

	private final Ref adaptedRef;

	public ValueAdapter(Ref adaptedRef) {
		this.adaptedRef = adaptedRef;
	}

	public final Ref getAdaptedRef() {
		return this.adaptedRef;
	}

	public abstract boolean isConstant();

	public abstract Ref toTarget();

	/**
	 * Determines an adapted type parameters.
	 *
	 * @param scope the scope to determine the type parameters in.
	 *
	 * @return type parameters.
	 */
	public abstract TypeParameters<?> typeParameters(Scope scope);

	public abstract Value<?> value(Resolver resolver);

	public final void resolveAll(FullResolver resolver) {
		resolver.getContext().fullResolution().start();
		try {
			fullyResolve(resolver);
		} finally {
			resolver.getContext().fullResolution().end();
		}
	}

	public abstract void resolveTargets(TargetResolver resolver);

	public abstract InlineValue inline(Normalizer normalizer, Scope origin);

	public abstract Eval eval();

	@Override
	public String toString() {
		if (this.adaptedRef == null) {
			return super.toString();
		}
		return this.adaptedRef.toString();
	}

	protected abstract void fullyResolve(FullResolver resolver);

}
