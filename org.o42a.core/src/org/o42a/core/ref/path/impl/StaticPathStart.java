/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.BoundPath;


public final class StaticPathStart {

	public static StaticPathStart staticPathStart(BoundPath path) {

		final StaticPathStartFinder walker = new StaticPathStartFinder();

		path.walk(pathResolver(path.getOrigin(), dummyUser()), walker);

		return new StaticPathStart(
				walker.startObjectScope(),
				walker.startObject(),
				walker.startIndex());
	}

	private final Scope startObjectScope;
	private final Obj startObject;
	private final int startIndex;

	private StaticPathStart(
			Scope startObjectScope,
			Obj startObject,
			int startIndex) {
		this.startObjectScope = startObjectScope;
		this.startObject = startObject;
		this.startIndex = startIndex;
	}

	public final Scope startObjectScope() {
		return this.startObjectScope;
	}

	public final Obj startObject() {
		return this.startObject;
	}

	public final int startIndex() {
		return this.startIndex;
	}

}
