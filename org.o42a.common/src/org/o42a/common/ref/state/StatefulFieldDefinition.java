/*
    Compiler Commons
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.common.ref.state;

import org.o42a.core.member.field.*;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.Ref;


final class StatefulFieldDefinition extends FieldDefinition {

	private final Ref ref;
	private final Ref value;
	private FieldDefinition valueDefinition;

	StatefulFieldDefinition(Ref ref, Ref value) {
		super(ref);
		this.ref = ref;
		this.value = value;
	}

	@Override
	public void init(Field field, Ascendants implicitAscendants) {
		getValueDefinition().init(field, implicitAscendants);
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {
		return getValueDefinition().getDefinitionTarget();
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		definer.makeStateful();
		getValueDefinition().defineObject(definer);
	}

	@Override
	public void overrideObject(ObjectDefiner definer) {

		final FieldDefinition valueDefinition = getValueDefinition();
		final DefinitionTarget target = valueDefinition.getDefinitionTarget();
		final DefinitionTarget definerTarget = definerTarget(definer);

		if (target.isDefault() || target.is(definerTarget)) {
			definer.makeStateful();
			valueDefinition.overrideObject(definer);
			return;
		}

		this.ref.rebuiltFieldDefinition().overrideObject(definer);
	}

	@Override
	public void defineMacro(MacroDefiner definer) {
		definer.makeStateful();
		getValueDefinition().defineMacro(definer);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return "\\\\" + this.value;
	}

	private final FieldDefinition getValueDefinition() {
		if (this.valueDefinition != null) {
			return this.valueDefinition;
		}
		return this.valueDefinition = this.value.toFieldDefinition();
	}

}
