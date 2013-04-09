/*
    Compiler Core
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.core.st;

import org.o42a.core.Contained;
import org.o42a.core.Distributor;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.ImperativeBlock;


public abstract class Statement extends Contained {

	private boolean fullyResolved;

	public Statement(LocationInfo location, Distributor distributor) {
		super(location, distributor);
	}

	public abstract boolean isValid();

	public DeclarativeBlock toDeclarativeBlock() {
		return null;
	}

	public ImperativeBlock toImperativeBlock() {
		return null;
	}

	public abstract Command command(CommandEnv env);

	public abstract Statement reproduce(Reproducer reproducer);

	public final boolean assertFullyResolved() {
		assert this.fullyResolved :
			this + " is not fully resolved";
		return true;
	}

	protected final void fullyResolved() {
		this.fullyResolved = true;
	}

}
