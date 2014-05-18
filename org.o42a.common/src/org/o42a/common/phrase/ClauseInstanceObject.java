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

import org.o42a.core.object.common.DefinedObject;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.value.Statefulness;


final class ClauseInstanceObject extends DefinedObject {

	private final ClauseInstanceConstructor constructor;

	ClauseInstanceObject(ClauseInstanceConstructor constructor) {
		super(
				constructor.instance().getLocation(),
				constructor.distribute());
		this.constructor = constructor;
	}

	@Override
	public String toString() {
		if (this.constructor == null) {
			return super.toString();
		}
		return this.constructor.toString();
	}

	@Override
	protected Nesting createNesting() {
		return this.constructor.getNesting();
	}

	@Override
	protected Ascendants buildAscendants() {
		return this.constructor.getAscendants().updateAscendants(
				new Ascendants(this));
	}

	@Override
	protected Statefulness determineStatefulness() {
		return super.determineStatefulness().setStateful(
				this.constructor.getAscendants().isStateful());
	}

	@Override
	protected DefinitionsBuilder createDefinitionsBuilder() {
		return blockDefinitions(this.constructor.instance().getDefinition());
	}

}
