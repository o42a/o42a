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
package org.o42a.core.ref.common;

import static org.o42a.core.st.sentence.BlockBuilder.valueBlock;

import org.o42a.core.Distributor;
import org.o42a.core.member.field.*;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.path.BoundPath;


public abstract class DefaultFieldDefinition extends FieldDefinition {

	private final BoundPath path;

	public DefaultFieldDefinition(
			BoundPath path,
			Distributor distributor) {
		super(path, distributor);
		this.path = path;
	}

	public final BoundPath path() {
		return this.path;
	}

	@Override
	public void setImplicitAscendants(Ascendants ascendants) {
	}

	@Override
	public void overrideObject(ObjectDefiner definer) {
		if (definerLinkDepth(definer) == getLinkDepth()) {
			defineObject(definer);
			return;
		}
		pathAsValue(definer);
	}

	@Override
	public void defineLink(LinkDefiner definer) {
		definer.setTargetRef(path().target(distribute()), null);
	}

	@Override
	public void defineMacro(MacroDefiner definer) {
		definer.setRef(path().target(distribute()));
	}

	@Override
	public String toString() {
		if (this.path == null) {
			return super.toString();
		}
		return this.path.toString();
	}

	protected void pathAsValue(ObjectDefiner definer) {
		definer.define(valueBlock(path().target(
				definer.getField().distributeIn(
						path().getOrigin().getContainer()))));
	}

}
