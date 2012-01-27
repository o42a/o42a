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
package org.o42a.core.artifact.object.impl;

import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactScope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.local.Dep;
import org.o42a.core.ref.Ref;


public abstract class ObjectArtifact extends Artifact<Obj> {

	public ObjectArtifact(Scope scope) {
		super(scope);
	}

	public ObjectArtifact(ArtifactScope<Obj> scope) {
		super(scope);
	}

	protected ObjectArtifact(Scope scope, Obj sample) {
		super(scope, sample);
	}

	protected ObjectArtifact(ArtifactScope<Obj> scope, Obj sample) {
		super(scope, sample);
	}

	protected abstract Dep addEnclosingOwnerDep(Obj owner);

	protected abstract Dep addRefDep(Ref ref);

}
