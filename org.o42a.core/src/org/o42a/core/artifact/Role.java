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
package org.o42a.core.artifact;

import org.o42a.core.ScopeInfo;


public enum Role {

	NONE() {
		@Override
		public void reportMisuse(Artifact<?> artifact, ScopeInfo user) {
		}
		@Override
		public void reportMisuse(ScopeInfo target, ScopeInfo user) {
		}
	},
	ANY() {
		@Override
		public void reportMisuse(Artifact<?> artifact, ScopeInfo user) {
			artifact.getLogger().invalidArtifact(artifact);
		}
		@Override
		public void reportMisuse(ScopeInfo target, ScopeInfo user) {
			target.getContext().getLogger().forbiddenAccess(
					user,
					target);
		}
	},
	PROTOTYPE() {
		@Override
		public void reportMisuse(Artifact<?> artifact, ScopeInfo user) {
			artifact.getLogger().cantInherit(user, artifact);
		}
		@Override
		public void reportMisuse(ScopeInfo target, ScopeInfo user) {
			target.getContext().getLogger().cantInherit(user, target);
		}
	},
	INSTANCE() {
		@Override
		public void reportMisuse(Artifact<?> artifact, ScopeInfo user) {
			if (artifact.isAbstract()) {
				artifact.getLogger().abstractValue(user);
			} else {
				artifact.getLogger().indefiniteValue(user);
			}
		}
		@Override
		public void reportMisuse(ScopeInfo target, ScopeInfo user) {
			target.getContext().getLogger().notObject(user, target);
		}
	};

	public abstract void reportMisuse(Artifact<?> artifact, ScopeInfo user);

	public abstract void reportMisuse(ScopeInfo target, ScopeInfo user);

}
