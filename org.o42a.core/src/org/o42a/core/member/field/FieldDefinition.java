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

import static org.o42a.core.ref.path.PrefixPath.upgradePrefix;
import static org.o42a.core.st.sentence.BlockBuilder.emptyBlock;

import org.o42a.core.Distributor;
import org.o42a.core.Placed;
import org.o42a.core.Scope;
import org.o42a.core.member.impl.field.DefaultFieldDefinition;
import org.o42a.core.member.impl.field.InvalidFieldDefinition;
import org.o42a.core.member.impl.field.RescopedFieldDefinition;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.BlockBuilder;


public abstract class FieldDefinition extends Placed {

	public static FieldDefinition invalidDefinition(
			LocationInfo location,
			Distributor distributor) {
		return new InvalidFieldDefinition(location, distributor);
	}

	public static FieldDefinition fieldDefinition(
			LocationInfo location,
			AscendantsDefinition ascendants,
			BlockBuilder definition) {
		return new DefaultFieldDefinition(
				location,
				ascendants.distribute(),
				ascendants,
				definition != null
				? definition : emptyBlock(location));
	}

	public static FieldDefinition impliedDefinition(
			LocationInfo location,
			Distributor scope) {
		return new DefaultFieldDefinition(
				location,
				scope,
				new AscendantsDefinition(location, scope),
				emptyBlock(location));
	}

	public FieldDefinition(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	public boolean isValid() {
		return true;
	}

	public abstract void defineObject(ObjectDefiner definer);

	public abstract void defineLink(LinkDefiner definer);

	public FieldDefinition prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}
		return new RescopedFieldDefinition(this, prefix);
	}

	public final FieldDefinition upgradeScope(Scope toScope) {
		if (toScope == getScope()) {
			return this;
		}
		return prefixWith(upgradePrefix(this, toScope));
	}

}
