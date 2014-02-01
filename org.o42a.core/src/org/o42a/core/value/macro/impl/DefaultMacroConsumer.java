/*
    Compiler Core
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
package org.o42a.core.value.macro.impl;

import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathTemplate;
import org.o42a.core.source.ScopedLogger;
import org.o42a.core.value.macro.MacroConsumer;


public class DefaultMacroConsumer implements Consumer, MacroConsumer {

	public static final DefaultMacroConsumer INSTANCE =
			new DefaultMacroConsumer();

	private DefaultMacroConsumer() {
	}

	@Override
	public MacroConsumer expandMacro(
			Ref macroRef,
			PathTemplate template,
			Ref expansion) {
		return this;
	}

	@Override
	public ScopedLogger getExpansionLogger() {
		return DEFAULT_MACRO_EXPANSION_LOGGER;
	}

	@Override
	public Ref expandMacro(Ref macroExpansion) {
		return macroExpansion;
	}

}
