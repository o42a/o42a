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
package org.o42a.core.object.array.impl;

import org.o42a.codegen.Generator;
import org.o42a.core.Scope;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.object.ConstructionMode;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.link.Link;
import org.o42a.core.object.type.Ascendants;


final class RtArrayElementObject extends Obj {

	private final RtArrayElement element;

	public RtArrayElementObject(RtArrayElement element) {
		super(element, element.distributeIn(element.getEnclosingContainer()));
		this.element = element;
	}

	@Override
	public ConstructionMode getConstructionMode() {
		return ConstructionMode.RUNTIME_CONSTRUCTION;
	}

	@Override
	public Link getDereferencedLink() {
		return this.element;
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(this.element.getTypeRef());
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return null;
	}

	@Override
	protected Obj findObjectIn(Scope enclosing) {

		final Link link = this.element.findLinkIn(enclosing);

		return link.getTarget();
	}

	@Override
	protected ObjectIR createIR(Generator generator) {
		throw new UnsupportedOperationException();
	}

}
