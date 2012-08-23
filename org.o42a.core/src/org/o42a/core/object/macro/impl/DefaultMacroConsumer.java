/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.macro.impl;

import static org.o42a.core.object.macro.MacroExpansionLogger.DEFAULT_MACRO_EXPANSION_LOGGER;

import org.o42a.core.object.macro.MacroConsumer;
import org.o42a.core.object.macro.MacroExpansionLogger;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PathTemplate;


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
	public MacroExpansionLogger getExpansionLogger() {
		return DEFAULT_MACRO_EXPANSION_LOGGER;
	}

	@Override
	public Ref expandMacro(Ref macroExpansion) {
		return macroExpansion;
	}

}