/*
    Compiler Core
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.core.member.field.impl;

import static org.o42a.core.member.field.DefinitionTarget.objectDefinition;

import org.o42a.core.Distributor;
import org.o42a.core.member.field.*;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.source.LocationInfo;


public final class InvalidFieldDefinition extends FieldDefinition {

	public InvalidFieldDefinition(
			LocationInfo location,
			Distributor distributor) {
		super(location, distributor);
	}

	@Override
	public void init(Field field, Ascendants implicitAscendants) {
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {
		return objectDefinition();
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
	}

	@Override
	public void overrideObject(ObjectDefiner definer) {
	}

	@Override
	public void defineMacro(MacroDefiner definer) {
	}

	@Override
	public String toString() {
		return "INVALID DEFINITION";
	}

}
