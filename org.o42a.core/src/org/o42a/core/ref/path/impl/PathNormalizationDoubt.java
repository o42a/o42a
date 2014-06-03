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
package org.o42a.core.ref.path.impl;

import static org.o42a.analysis.use.SimpleUsage.ALL_SIMPLE_USAGES;
import static org.o42a.analysis.use.SimpleUsage.SIMPLE_USAGE;
import static org.o42a.analysis.use.User.dummyUser;

import org.o42a.analysis.Doubt;
import org.o42a.analysis.use.*;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathResolver;


public final class PathNormalizationDoubt extends Doubt {

	private final BoundPath path;
	private final Usable<SimpleUsage> uses;
	private final ProxyUser<SimpleUsage> user;
	private ProxyUser<SimpleUsage> normalUser;
	private boolean normalizationAborted;

	public PathNormalizationDoubt(BoundPath path) {
		this.path = path;
		this.uses = ALL_SIMPLE_USAGES.usable(path);
		this.user = new ProxyUser<>(this.uses.toUser());
	}

	public final PathResolver wrapResolutionUser(
			PathResolver originalResolver) {

		final User<?> originalUser = originalResolver.toUser();

		if (originalUser.isDummyUser()) {
			return originalResolver;
		}

		this.uses.useBy(originalUser, SIMPLE_USAGE);

		return originalResolver.resolveBy(this.user);
	}

	public final UserInfo pathNormalized() {
		if (this.normalUser != null) {
			return this.normalUser;
		}
		return this.normalUser = new ProxyUser<>(dummyUser());
	}

	public final void abortNormalization() {
		this.normalizationAborted = true;
	}

	@Override
	public void resolveDoubt() {
		if (this.normalUser == null) {
			// Path was never normalized.
			return;
		}
		// Mark the normalized path steps used.
		this.normalUser.setProxied(this.uses.toUser());
		if (!this.normalizationAborted) {
			// The path normalization never aborted.
			// Replace the user a path resolved against with a dummy one.
			// This marks the original path steps unused.
			this.user.setProxied(dummyUser());
		}
	}

	@Override
	public String toString() {
		if (this.path == null) {
			return super.toString();
		}
		return this.path.toString();
	}

	@Override
	protected void reused() {
		if (this.normalUser != null) {
			this.normalUser.setProxied(dummyUser());
			this.normalUser = null;
		}
		if (this.user.getProxied().isDummyUser()) {
			this.user.setProxied(this.uses.toUser());
		}
		this.normalizationAborted = false;
	}

}
