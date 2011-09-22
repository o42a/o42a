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
package org.o42a.core.ref.impl.path;

import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.LinkDefiner;
import org.o42a.core.member.field.ObjectDefiner;
import org.o42a.core.ref.Ref;


final class PathTargetDefinition extends FieldDefinition {

	private final Ref target;

	PathTargetDefinition(Ref target) {
		super(target, target.distribute());
		this.target = target;
	}

	@Override
	public ArtifactKind<?> determineArtifactKind() {
		return artifactKind(this.target);
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		definer.setAncestor(this.target.toTypeRef());
	}

	@Override
	public void defineLink(LinkDefiner definer) {
		definer.setTargetRef(
				this.target.materialize(),
				this.target.toTypeRef());
	}

	@Override
	public String toString() {
		if (this.target == null) {
			return super.toString();
		}
		return "FieldDefinition[" + this.target.toString() + ']';
	}

}
