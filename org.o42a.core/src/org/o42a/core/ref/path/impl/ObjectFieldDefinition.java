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
package org.o42a.core.ref.path.impl;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.st.sentence.BlockBuilder.valueBlock;

import org.o42a.core.Distributor;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.LinkDefiner;
import org.o42a.core.member.field.ObjectDefiner;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathResolution;


public final class ObjectFieldDefinition extends FieldDefinition {

	static boolean pathToLink(BoundPath path) {

		final PathResolution resolution = path.resolve(
				pathResolver(path.getOrigin(), dummyUser()));

		if (resolution.isError()) {
			return false;
		}

		final Obj object = resolution.getObject().toObject();

		if (object == null) {
			return false;
		}

		return object.value().getValueType().isLink();
	}

	private final BoundPath path;

	public ObjectFieldDefinition(BoundPath path, Distributor distributor) {
		super(path, distributor);
		this.path = path;
	}

	@Override
	public void setImplicitAscendants(Ascendants ascendants) {
	}

	@Override
	public boolean isLink() {

		final PathResolution resolution =
				this.path.resolve(
						pathResolver(this.path.getOrigin(), dummyUser()));

		if (resolution.isError()) {
			return false;
		}

		final Obj object = resolution.getResult().toObject();

		if (object == null) {
			return false;
		}

		return object.value().getValueType().isLink();
	}

	@Override
	public void defineObject(ObjectDefiner definer) {
		definer.addImplicitSample(this.path.staticTypeRef(distribute()));
	}

	@Override
	public void overrideObject(ObjectDefiner definer) {
		if (!linkDefiner(definer) || pathToLink(this.path)) {
			defineObject(definer);
			return;
		}
		definer.define(valueBlock(this.path.target(
				definer.getField().distributeIn(
						this.path.getOrigin().getContainer()))));
	}

	@Override
	public void defineLink(LinkDefiner definer) {
		definer.setTargetRef(this.path.target(distribute()), null);
	}

	@Override
	public String toString() {
		return this.path.toString();
	}

}
