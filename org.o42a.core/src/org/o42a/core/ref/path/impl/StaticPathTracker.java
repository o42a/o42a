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
package org.o42a.core.ref.path.impl;

import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathResolver;
import org.o42a.core.ref.path.PathWalker;


public final class StaticPathTracker extends SimplePathTracker {

	private int beforeStart;

	public StaticPathTracker(
			BoundPath path,
			PathResolver resolver,
			PathWalker walker,
			int startIndex) {
		super(path, resolver, walker);
		// Static path resolution starts with object.
		// This object requires resolution.
		// This object's step index is one less than the path start one.
		this.beforeStart = startIndex - 1;
	}

	@Override
	public PathResolver nextResolver() {
		if (this.beforeStart > 0) {
			return this.initialResolver.resolveBy(dummyUser());
		}
		return super.nextResolver();
	}

	@Override
	protected boolean walk(boolean succeed) {
		--this.beforeStart;
		return super.walk(succeed);
	}

}
