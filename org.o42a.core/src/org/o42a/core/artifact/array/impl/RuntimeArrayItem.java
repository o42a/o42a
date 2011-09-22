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
package org.o42a.core.artifact.array.impl;

import org.o42a.codegen.Generator;
import org.o42a.core.artifact.array.ArrayItem;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.ir.ScopeIR;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;


public class RuntimeArrayItem extends ArrayItem {

	public RuntimeArrayItem(Ref indexRef) {
		super(indexRef, indexRef.distribute(), indexRef);
	}

	@Override
	public Link getArtifact() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RuntimeArrayItem reproduce(Reproducer reproducer) {

		final Ref indexRef = getIndexRef().reproduce(reproducer);

		if (indexRef == null) {
			return null;
		}

		return new RuntimeArrayItem(indexRef);
	}

	@Override
	protected ScopeIR createIR(Generator generator) {
		// TODO Auto-generated method stub
		return null;
	}

}
