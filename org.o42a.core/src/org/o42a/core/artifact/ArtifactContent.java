/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.artifact;

import static org.o42a.util.use.Usable.simpleUsable;

import org.o42a.util.use.*;


public class ArtifactContent implements UserInfo {

	private final Artifact<?> artifact;
	private Usable usable;
	private final boolean clonesContent;

	ArtifactContent(Artifact<?> artifact, boolean clonesContent) {
		this.artifact = artifact;
		this.clonesContent = clonesContent;
	}

	public final Artifact<?> getArtifact() {
		return this.artifact;
	}

	@Override
	public final User toUser() {
		return usable().toUser();
	}

	@Override
	public final boolean isUsedBy(UseCaseInfo useCase) {
		return getUseBy(useCase).isUsed();
	}

	@Override
	public final UseFlag getUseBy(UseCaseInfo useCase) {
		if (this.usable == null) {
			return useCase.toUseCase().unusedFlag();
		}
		return this.usable.getUseBy(useCase);
	}

	public final void useBy(UserInfo user) {
		if (user.toUser().isDummy()) {
			return;
		}
		usable().useBy(user);
	}

	@Override
	public String toString() {
		if (this.artifact == null) {
			return super.toString();
		}

		final StringBuilder out = new StringBuilder();

		out.append(this.clonesContent ? "ClonesContent[" : "Content[");
		out.append(this.artifact).append(']');

		return out.toString();
	}

	private final Usable usable() {
		if (this.usable != null) {
			return this.usable;
		}

		this.usable = simpleUsable(this);

		return this.usable;
	}

}
