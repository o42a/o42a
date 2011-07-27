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
package org.o42a.core.member.field;

import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.array.ArrayInitializer;


final class ArrayFieldDefinition extends FieldDefinition {

	private final ArrayInitializer arrayInitializer;

	ArrayFieldDefinition(ArrayInitializer arrayInitializer) {
		super(arrayInitializer, arrayInitializer.distribute());
		this.arrayInitializer = arrayInitializer;
	}

	@Override
	public ArtifactKind<?> determineArtifactKind() {
		return ArtifactKind.ARRAY;
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		getLogger().error(
				"not_object",
				this,
				"Array initializer can not declare object");
	}

	@Override
	public void defineLink(LinkDefiner definer) {
		getLogger().error(
				"not_link",
				this,
				"Array initializer can not declare link");
	}

	@Override
	public void defineArray(ArrayDefiner definer) {
		definer.define(this.arrayInitializer);
	}

	@Override
	public String toString() {
		return this.arrayInitializer.toString();
	}

}
