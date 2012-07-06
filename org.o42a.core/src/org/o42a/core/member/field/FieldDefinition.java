/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.member.field;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.Scope;
import org.o42a.core.member.field.impl.InvalidFieldDefinition;
import org.o42a.core.member.field.impl.RescopedFieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathResolution;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.ValueStruct;


public abstract class FieldDefinition extends Placed {

	public static FieldDefinition invalidDefinition(
			LocationInfo location,
			Distributor distributor) {
		return new InvalidFieldDefinition(location, distributor);
	}

	public static FieldDefinition impliedDefinition(
			LocationInfo location,
			Distributor scope) {
		return new AscendantsDefinition(location, scope).fieldDefinition(
				location,
				emptyBlock(location));
	}

	public static int definerLinkDepth(ObjectDefiner definer) {

		final Field[] allOverridden = definer.getField().getOverridden();

		for (Field overridden : allOverridden) {

			final ValueStruct<?, ?> valueStruct =
					overridden.toObject().value().getValueStruct();

			if (!valueStruct.isVoid()) {
				return valueStruct.getLinkDepth();
			}
		}

		return 0;
	}

	public static int pathLinkDepth(BoundPath path) {

		final PathResolution resolution = path.resolve(
				pathResolver(path.getOrigin(), dummyUser()));

		if (resolution.isError()) {
			return 0;
		}

		final Obj object = resolution.getObject().toObject();

		if (object == null) {
			return 0;
		}

		return object.value().getValueStruct().getLinkDepth();
	}

	public FieldDefinition(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	public abstract void setImplicitAscendants(Ascendants ascendants);

	public boolean isValid() {
		return true;
	}

	public abstract int getLinkDepth();

	public abstract void defineObject(ObjectDefiner definer);

	public abstract void overrideObject(ObjectDefiner definer);

	public abstract void defineLink(LinkDefiner definer);

	public FieldDefinition prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}
		return new RescopedFieldDefinition(this, prefix);
	}

	public final FieldDefinition upgradeScope(Scope toScope) {
		if (toScope.is(getScope())) {
			return this;
		}
		return prefixWith(upgradePrefix(this, toScope));
	}

}
