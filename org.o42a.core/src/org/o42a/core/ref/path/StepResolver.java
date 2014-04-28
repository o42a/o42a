/*
    Compiler Core
    Copyright (C) 2012-2014 Ruslan Lopatin

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

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.RefUser;
import org.o42a.core.ref.path.impl.PathTracker;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;


public final class StepResolver implements LocationInfo {

	private final PathTracker tracker;
	private PathResolver pathResolver;
	private int index;
	private Scope start;

	StepResolver(PathTracker tracker) {
		this.tracker = tracker;
	}

	public final BoundPath getPath() {
		return this.tracker.getPath();
	}

	public final PathResolver getPathResolver() {
		return this.pathResolver;
	}

	public final PathWalker getWalker() {
		return this.tracker;
	}

	public final int getIndex() {
		return this.index;
	}

	public final boolean isLastStep() {
		return getIndex() == getPath().length() - 1;
	}

	public final Scope getStart() {
		return this.start;
	}

	@Override
	public final Location getLocation() {
		return getPath().getLocation();
	}

	public final Scope getPathStart() {
		return getPathResolver().getPathStart();
	}

	public final boolean isFullResolution() {
		return getPathResolver().isFullResolution();
	}

	public final RefUser refUser() {
		return getPathResolver().refUser();
	}

	public final RefUsage refUsage() {
		return getPathResolver().refUsage();
	}

	@Override
	public String toString() {
		if (this.pathResolver == null) {
			return super.toString();
		}
		return getPath().toString(getIndex() + 1);
	}

	final Container resolveStep(Step step, Scope start, int index) {
		this.index = index;
		this.start = start;
		this.pathResolver = this.tracker.nextResolver();
		return step.resolve(this);
	}

}
