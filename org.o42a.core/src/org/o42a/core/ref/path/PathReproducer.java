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

import java.util.HashMap;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.Reproducer;


public final class PathReproducer {

	private final Reproducer reproducer;
	private final BoundPath reproducingPath;
	private final PathBindings reproducing;
	private final HashMap<PathBinding<?>, PathBinding<?>> reproduced;
	private final HashMap<PathBinding<?>, PathBinding<?>> newBindings;

	PathReproducer(Reproducer reproducer, BoundPath reproducingPath) {
		this.reproducer = reproducer;
		this.reproducingPath = reproducingPath;
		this.reproducing = reproducingPath.getBindings();

		final int size = this.reproducing.size();

		this.reproduced = new HashMap<PathBinding<?>, PathBinding<?>>(size);
		this.newBindings = new HashMap<PathBinding<?>, PathBinding<?>>(size);
		if (!this.reproducing.isEmpty()) {
			this.newBindings.putAll(this.reproducing.bindings());
		}
	}

	PathReproducer(Reproducer reproducer, PathReproducer prototype) {
		this.reproducer = reproducer;
		this.reproducingPath = prototype.reproducingPath;
		this.reproducing = prototype.reproducing;
		this.reproduced = prototype.reproduced;
		this.newBindings = prototype.newBindings;
	}

	public final BoundPath getReproducingPath() {
		return this.reproducingPath;
	}

	public final Reproducer getReproducer() {
		return this.reproducer;
	}

	public final Scope getReproducingScope() {
		return getReproducer().getReproducingScope();
	}

	public final Scope getScope() {
		return getReproducer().getScope();
	}

	public final Container getContainer() {
		return getReproducer().getContainer();
	}

	public final boolean phraseCreatesObject() {
		return getReproducer().phraseCreatesObject();
	}

	public final Distributor distribute() {
		return getReproducer().distribute();
	}

	public final CompilerLogger getLogger() {
		return getReproducer().getLogger();
	}

	public final PathReproduction reproducePath() {
		return getReproducingPath().getKind().reproduce(this);
	}

	public final <B> PathBinding<B> reproduce(PathBinding<B> binding) {

		@SuppressWarnings("unchecked")
		final PathBinding<B> cached =
				(PathBinding<B>) this.reproduced.get(binding);

		if (cached != null) {
			return cached;
		}

		final PathBinding<B> reproduction = binding.reproduce(getReproducer());

		this.reproduced.put(binding, reproduction);
		this.newBindings.remove(binding);

		return reproduction;
	}

	public Path reproduceBindings(Path path) {
		if (this.newBindings.isEmpty()) {
			return path;
		}
		for (PathBinding<?> binding : this.reproduced.values()) {
			this.newBindings.put(binding, binding);
		}

		final PathBindings bindings = path.getBindings();

		if (!bindings.isEmpty()) {
			this.newBindings.putAll(bindings.bindings());
		}

		return new Path(
				path.getKind(),
				new PathBindings(this.newBindings),
				path.isStatic(),
				path.getSteps());

	}

}
