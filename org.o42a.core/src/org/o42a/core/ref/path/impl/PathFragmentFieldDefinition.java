/*
    Compiler Core
    Copyright (C) 2011-2014 Ruslan Lopatin

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

import org.o42a.core.member.field.*;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;


public final class PathFragmentFieldDefinition extends FieldDefinition {

	private final Ref ref;
	private FieldDefinition definition;

	public PathFragmentFieldDefinition(Ref ref) {
		super(ref);
		this.ref = ref;
	}

	@Override
	public boolean isValid() {
		return getDefinition().isValid();
	}

	@Override
	public void init(Field field, Ascendants implicitAscendants) {
		getDefinition().init(field, implicitAscendants);
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {
		return getDefinition().getDefinitionTarget();
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		getDefinition().defineObject(definer);
	}

	@Override
	public void overrideObject(ObjectDefiner definer) {
		getDefinition().overrideObject(definer);
	}

	@Override
	public void defineMacro(MacroDefiner definer) {
		getDefinition().defineMacro(definer);
	}

	@Override
	public String toString() {
		if (this.definition == null) {
			return super.toString();
		}
		return this.definition.toString();
	}

	private final FieldDefinition getDefinition() {
		if (this.definition != null) {
			return this.definition;
		}
		return this.definition = this.ref.rebuiltFieldDefinition();
	}

}
