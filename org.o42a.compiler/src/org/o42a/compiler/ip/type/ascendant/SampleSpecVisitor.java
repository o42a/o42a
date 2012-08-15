/*
    Compiler
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
package org.o42a.compiler.ip.type.ascendant;

import org.o42a.ast.ref.AbstractRefVisitor;
import org.o42a.ast.ref.RefNode;
import org.o42a.compiler.ip.Interpreter;
import org.o42a.core.Distributor;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;


public class SampleSpecVisitor
		extends AbstractRefVisitor<StaticTypeRef, Distributor> {

	private final Interpreter ip;

	public SampleSpecVisitor(Interpreter ip) {
		this.ip = ip;
	}

	@Override
	protected StaticTypeRef visitRef(RefNode ref, Distributor p) {

		final Ref sampleRef = ref.accept(this.ip.bodyRefVisitor(), p);

		if (sampleRef == null) {
			return null;
		}

		return sampleRef.toStaticTypeRef();
	}

}