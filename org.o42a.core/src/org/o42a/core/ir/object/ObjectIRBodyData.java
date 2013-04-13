/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.ir.object;

import org.o42a.codegen.Generator;
import org.o42a.codegen.data.SubData;
import org.o42a.core.ir.field.Fld;


public final class ObjectIRBodyData {

	private final ObjectIRBody bodyIR;
	private final SubData<?> data;

	ObjectIRBodyData(ObjectIRBody bodyIR, SubData<?> data) {
		this.bodyIR = bodyIR;
		this.data = data;
	}

	public final Generator getGenerator() {
		return getBodyIR().getGenerator();
	}

	public final ObjectIRBody getBodyIR() {
		return this.bodyIR;
	}

	public final SubData<?> getData() {
		return this.data;
	}

	public final void declareFld(Fld<?> fld) {
		getBodyIR().declareFld(fld);
	}

	@Override
	public String toString() {
		if (this.data == null) {
			return super.toString();
		}
		return this.data.toString();
	}

}
