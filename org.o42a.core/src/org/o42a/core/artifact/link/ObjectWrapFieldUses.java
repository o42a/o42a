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
package org.o42a.core.artifact.link;

import static java.util.Arrays.asList;

import java.util.Iterator;
import java.util.List;

import org.o42a.util.use.UseInfo;
import org.o42a.util.use.Uses;


final class ObjectWrapFieldUses extends Uses {

	private final ObjectWrap objectWrap;
	private List<UseInfo> uses;

	ObjectWrapFieldUses(ObjectWrap objectWrap) {
		this.objectWrap = objectWrap;
	}

	@Override
	public String toString() {
		if (this.objectWrap == null) {
			return super.toString();
		}
		return "FieldUses[" + this.objectWrap + ']';
	}

	@Override
	protected Iterator<? extends UseInfo> usedBy() {
		if (this.uses != null) {
			return this.uses.iterator();
		}

		this.uses = asList(
				this.objectWrap.getWrapped().fieldUses(),
				this.objectWrap.superFieldUses());

		return this.uses.iterator();
	}

}
