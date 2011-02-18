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
package org.o42a.core.ref;

import org.o42a.core.artifact.Artifact;
import org.o42a.core.ref.common.Wrap;


final class FixedScopeRef extends Wrap {

	private final Ref ref;

	FixedScopeRef(Ref ref) {
		super(ref, ref.distribute());
		this.ref = ref;
	}

	@Override
	protected Ref resolveWrapped() {

		final Resolution resolution = this.ref.getResolution();

		if (resolution.isError()) {
			return null;
		}

		final Artifact<?> artifact = resolution.toArtifact();

		if (artifact == null) {
			getLogger().notArtifact(this);
			return null;
		}

		return artifact.fixedRef(distribute());
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return "&" + this.ref;
	}

	@Override
	protected boolean isKnownStatic() {
		return true;
	}

}
