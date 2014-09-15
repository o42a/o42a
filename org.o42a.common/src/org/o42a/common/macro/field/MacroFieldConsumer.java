/*
    Compiler Commons
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.common.macro.field;

import org.o42a.core.object.Meta;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathTemplate;
import org.o42a.core.value.macro.MacroConsumer;
import org.o42a.core.value.macro.MacroDep;


public final class MacroFieldConsumer
		implements MacroDep<MacroFieldMetaDep>, Consumer {

	public static final MacroFieldConsumer INSTANCE = new MacroFieldConsumer();

	private MacroFieldConsumer() {
	}

	@Override
	public MacroConsumer expandMacro(
			Ref macroRef,
			PathTemplate template,
			Ref expansion) {
		return new MacroFieldMacroConsumer(this, macroRef, template);
	}

	@Override
	public MacroFieldMetaDep newDep(
			Meta meta,
			Ref macroRef,
			PathTemplate template) {
		return new MacroFieldMetaDep(meta, macroRef);
	}

	@Override
	public void setParentDep(MacroFieldMetaDep dep, MetaDep parentDep) {
		dep.setParentDep(parentDep);
	}

}
