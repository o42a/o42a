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
package org.o42a.core.artifact.common;

import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.FieldUses;


public abstract class MaterializableArtifact<
		A extends MaterializableArtifact<A>>
		extends Artifact<A> {

	private Obj materialization;

	public MaterializableArtifact(Scope scope) {
		super(scope);
	}

	protected MaterializableArtifact(MaterializableArtifactScope<A> scope) {
		super(scope);
	}

	protected MaterializableArtifact(Scope scope, A sample) {
		super(scope, sample);
	}

	@Override
	public final Obj materialize() {
		if (this.materialization != null) {
			return this.materialization;
		}

		this.materialization = createMaterialization();

		content().useBy(this.materialization.content());

		return this.materialization;
	}

	@Override
	public FieldUses fieldUses() {
		return materialize().fieldUses();
	}

	protected abstract Obj createMaterialization();

	@Override
	protected final void fullyResolve() {
		fullyResolveArtifact();
		materialize().resolveAll();
	}

	protected abstract void fullyResolveArtifact();

}
