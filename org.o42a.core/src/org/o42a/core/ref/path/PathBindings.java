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
package org.o42a.core.ref.path;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public final class PathBindings {

	public static final PathBindings NO_PATH_BINDINGS = new PathBindings();

	private Map<PathBinding<?>, PathBinding<?>> bindings;

	PathBindings() {
	}

	PathBindings(Map<PathBinding<?>, PathBinding<?>> bindings) {
		this.bindings = bindings;
	}

	public final boolean isEmpty() {
		return this.bindings == null;
	}

	public final int size() {
		return isEmpty() ? 0 : this.bindings.size();
	}

	public final <B> B boundOf(PathBinding<B> binding) {
		assert !isEmpty() :
			"No path bindings";

		@SuppressWarnings("unchecked")
		final PathBinding<B> found =
				(PathBinding<B>) this.bindings.get(binding);

		assert found != null :
			"Path binding not found: " + binding;

		return found.getBound();
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "PathBindings{}";
		}
		return "PathBindings" + this.bindings;
	}

	PathBindings addBinding(PathBinding<?> binding) {

		final Map<PathBinding<?>, PathBinding<?>> bindings;

		if (this.bindings == null) {
			bindings = Collections.<PathBinding<?>, PathBinding<?>>singletonMap(
					binding,
					binding);
		} else {

			final HashMap<PathBinding<?>, PathBinding<?>> newBindings =
					new HashMap<PathBinding<?>, PathBinding<?>>(
							this.bindings.size() + 1);

			newBindings.putAll(this.bindings);
			newBindings.put(binding, binding);

			bindings = Collections.unmodifiableMap(newBindings);
		}

		return new PathBindings(bindings);
	}

	PathBindings modifyPaths(PathModifier modifier) {
		if (isEmpty()) {
			return this;
		}

		final HashMap<PathBinding<?>, PathBinding<?>> newBindings =
				new HashMap<PathBinding<?>, PathBinding<?>>(
						this.bindings.size());

		for (Map.Entry<PathBinding<?>, PathBinding<?>> e
				: this.bindings.entrySet()) {

			final PathBinding<?> newBinding = e.getValue().modifyPath(modifier);

			newBindings.put(e.getKey(), newBinding);
		}


		return new PathBindings(newBindings);
	}

	PathBindings prefixWith(PrefixPath prefix) {

		final PathBindings prefixBindings = prefix.getBindings();

		if (isEmpty()) {
			return prefixBindings;
		}

		final HashMap<PathBinding<?>, PathBinding<?>> newBindings;

		if (!prefixBindings.isEmpty()) {
			newBindings = new HashMap<PathBinding<?>, PathBinding<?>>(
					this.bindings.size() + prefixBindings.size());
			newBindings.putAll(prefixBindings.bindings);
		} else {
			newBindings = new HashMap<PathBinding<?>, PathBinding<?>>(
					this.bindings.size());
		}

		for (Map.Entry<PathBinding<?>, PathBinding<?>> e
				: this.bindings.entrySet()) {

			final PathBinding<?> newBinding = e.getValue().prefixWith(prefix);

			newBindings.put(e.getKey(), newBinding);
		}

		return new PathBindings(newBindings);
	}

	final Map<? extends PathBinding<?>, ? extends PathBinding<?>> bindings() {
		return this.bindings;
	}

}
