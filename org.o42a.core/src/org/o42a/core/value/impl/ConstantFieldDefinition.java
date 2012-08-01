/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.value.impl;

import static org.o42a.core.member.field.DefinitionTarget.definitionTarget;

import org.o42a.core.Distributor;
import org.o42a.core.member.field.DefinitionTarget;
import org.o42a.core.member.field.ObjectDefiner;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.common.DefaultFieldDefinition;
import org.o42a.core.ref.path.BoundPath;


final class ConstantFieldDefinition extends DefaultFieldDefinition {

	private final Constant<?> constant;

	ConstantFieldDefinition(
			BoundPath path,
			Distributor distributor,
			Constant<?> constant) {
		super(path, distributor);
		this.constant = constant;
	}

	@Override
	public void setImplicitAscendants(Ascendants ascendants) {
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {
		return definitionTarget(this.constant.getValueType().struct());
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		definer.setAncestor(this.constant.getValueType().typeRef(
				path(),
				getScope()));
		pathAsValue(definer);
	}

	@Override
	public String toString() {
		if (this.constant == null) {
			return super.toString();
		}
		return this.constant.toString();
	}

}
