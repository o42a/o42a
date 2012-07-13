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
package org.o42a.core.ref.path.impl;

import org.o42a.core.Scope;
import org.o42a.core.ref.path.*;
import org.o42a.util.log.LogRecord;


public abstract class PathTracker implements PathWalker, PathExpander {

	private final BoundPath path;
	protected final PathResolver initialResolver;
	private final PathWalker walker;
	private LogRecord errorMessage;
	private boolean aborted;
	private boolean error;

	public PathTracker(
			BoundPath path,
			PathResolver resolver,
			PathWalker walker) {
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

	public final boolean isError() {
		return this.error;
	}

	public final LogRecord getErrorMessage() {
		return this.errorMessage;
	}

	@Override
	public boolean replay(PathWalker walker) {
		throw new IllegalStateException();
	}

	@Override
	public final boolean root(BoundPath path, Scope root) {
		return walk(walker().root(path, root));
	}

	@Override
	public final boolean start(BoundPath path, Scope start) {
		return walk(walker().start(path, start));
	}

	@Override
	public void pathTrimmed(BoundPath path, Scope root) {
		walker().pathTrimmed(path, root);
	}

	@Override
	public void error(LogRecord message) {
		walker().error(message);
		this.errorMessage = message;
		this.error = true;
		this.aborted = true;
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
