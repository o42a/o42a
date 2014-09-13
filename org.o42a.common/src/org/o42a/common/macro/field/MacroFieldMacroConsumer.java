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

import static org.o42a.util.fn.DoOnce.doOnce;

import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathTemplate;
import org.o42a.core.source.ScopedLogger;
import org.o42a.core.value.macro.MacroConsumer;
import org.o42a.util.fn.DoOnce;


class MacroFieldMacroConsumer implements MacroConsumer {

	private final MacroFieldConsumer macroDep;
	private final Ref macroRef;
	private final PathTemplate template;
	private final DoOnce registerDep = doOnce(this::registerDep);

	MacroFieldMacroConsumer(
			MacroFieldConsumer macroDep,
			Ref macroRef,
			PathTemplate template) {
		this.macroDep = macroDep;
		this.macroRef = macroRef;
		this.template = template;
	}

	@Override
	public ScopedLogger getExpansionLogger() {
		return DEFAULT_MACRO_EXPANSION_LOGGER;
	}

	@Override
	public Ref expandMacro(Ref macroExpansion) {
		this.registerDep.run();
		return macroExpansion;
	}

	private void registerDep() {

		final MacroFieldMetaDep dep =
				this.macroDep.buildDep(this.macroRef, this.template);

		if (dep != null) {
			dep.register();
		}
	}

}
