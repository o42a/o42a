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

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.macro.Macro;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathExpander;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueStruct;
import org.o42a.core.value.ValueType;


public class MacroExpansionFragment extends PathFragment {

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {

		final Macro macro = macro(expander, start);

		if (macro == null) {
			return null;
		}

		final MacroExpanderImpl macroExpander = new MacroExpanderImpl(expander);

		return macro.expand(macroExpander);
	}

	private Macro macro(PathExpander expander, Scope start) {

		final Obj object = start.toObject();

		if (object == null) {
			return notMacro(expander);
		}
		if (object.value().getValueType() != ValueType.MACRO) {
			return notMacro(expander);
		}

		final Value<Macro> macroValue = ValueStruct.MACRO.cast(
				object.value().getDefinitions().value(start.resolver()));

		if (!macroValue.getKnowledge().isKnown()
				|| macroValue.getKnowledge().isFalse()) {
			return unresolvedMacro(expander);
		}

		return macroValue.getCompilerValue();
	}

	private Macro notMacro(PathExpander expander) {
		expander.getPath().getLogger().error(
				"not_macro",
				expander.getPath(),
				"Not a macro");
		return null;
	}

	private Macro unresolvedMacro(PathExpander expander) {
		expander.error(expander.getPath().getLogger().errorRecord(
				"unresolved_macro",
				expander.getPath(),
				"Macro can not be resolved"));
		return null;
	}

}
