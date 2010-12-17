/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.core.def;

import static org.o42a.core.def.CondDef.trueCondDef;

import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.st.St;
import org.o42a.core.st.sentence.ImperativeBlock;


public abstract class BlockBase extends St {

	protected static Def localDef(ImperativeBlock block, Scope scope) {

		final Obj actualOwner = scope.getContainer().toObject();
		final Obj explicitOwner = block.getScope().getOwner();
		final boolean explicit = actualOwner == explicitOwner;

		final Rescoper rescoper = block.getScope().rescoperTo(scope);
		final LocalDef localDef = new LocalDef(
				block,
				trueCondDef(block, block.getScope()).rescope(rescoper),
				rescoper,
				explicit);

		// rescope to explicit owner scope
		final Def def = localDef.rescope(explicitOwner.getScope());

		if (explicit) {
			return def;
		}

		// upgrade scope to actual owner's one
		return def.upgradeScope(actualOwner.getScope());
	}

	public BlockBase(LocationSpec location, Distributor distributor) {
		super(location, distributor);
	}

}
