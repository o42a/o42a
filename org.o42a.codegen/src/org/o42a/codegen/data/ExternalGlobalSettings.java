/*
    Compiler Code Generator
    Copyright (C) 2012-2014 Ruslan Lopatin

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
package org.o42a.codegen.data;

import org.o42a.codegen.code.op.StructOp;


public class ExternalGlobalSettings
		extends AbstractGlobalSettings<ExternalGlobalSettings> {

	ExternalGlobalSettings(Globals globals) {
		super(globals);
		this.flags = EXPORTED;
	}

	public final ExternalGlobalSettings set(GlobalAttributes properties) {
		this.flags = (properties.getDataFlags() & GLOBAL_FLAGS) | EXPORTED;
		return this;
	}

	public final <S extends StructOp<S>> Ptr<S> link(
			String name,
			Type<S> type) {
		return globals().externalGlobal(name, type, this);
	}

}
