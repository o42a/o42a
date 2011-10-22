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
package org.o42a.core.ref.impl.path;

import org.o42a.core.Scope;
import org.o42a.core.ref.path.*;


public abstract class PathTracker implements PathWalker, PathExpander {

	private final BoundPath path;
	protected final PathResolver initialResolver;
	private final PathWalker walker;
	private boolean aborted;

	public PathTracker(BoundPath path, PathResolver resolver, PathWalker walker) {
		this.path = path;
		this.initialResolver = resolver;
		this.walker = walker;
	}

	@Override
	public BoundPath getPath() {
		return this.path;
	}

	public PathResolver nextResolver() {
		return this.initialResolver;
	}

	public final boolean isAborted() {
		return this.aborted;
	}

	public void setAbsolute(Scope root) {
	}

	@Override
	public boolean replay(PathWalker walker) {
		throw new IllegalStateException();
	}

	@Override
	public final boolean root(BoundPath path, Scope root) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean start(BoundPath path, Scope start) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
		walker().abortedAt(last, brokenStep);
	}

	protected final PathWalker walker() {
		return this.walker;
	}

	protected boolean walk(boolean succeed) {
		this.aborted = !succeed;
		return succeed;
	}

}
