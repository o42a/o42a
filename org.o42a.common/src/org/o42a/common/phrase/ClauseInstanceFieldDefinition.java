/*
    Compiler Commons
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.common.phrase;

import org.o42a.core.member.field.DefinitionTarget;
import org.o42a.core.member.field.ObjectDefiner;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.DefaultFieldDefinition;


final class ClauseInstanceFieldDefinition extends DefaultFieldDefinition {

	private final ClauseInstanceConstructor constructor;

	ClauseInstanceFieldDefinition(
			Ref ref,
			ClauseInstanceConstructor constructor) {
		super(ref);
		this.constructor = constructor;
	}

	@Override
	public DefinitionTarget getDefinitionTarget() {
		return refDefinitionTarget(getRef());
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		this.constructor.getAscendants().updateAscendants(definer);
		definer.define(
				this.constructor.instance().getDefinition()::definitions);
	}

	@Override
	public String toString() {
		if (this.constructor == null) {
			return super.toString();
		}
		return this.constructor.toString();
	}

}
