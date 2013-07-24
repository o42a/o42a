/*
    Root Object Definition
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.root;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.common.object.ValueTypeObject;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.macro.Macro;
import org.o42a.core.value.macro.MacroExpander;


@SourcePath(relativeTo = Root.class, value = "macro.o42a")
public class MacroValueTypeObject extends ValueTypeObject implements Macro {

	public MacroValueTypeObject(Obj owner, AnnotatedSources sources) {
		super(owner, sources, ValueType.MACRO);
	}

	@Override
	public Definitions overrideDefinitions(Definitions ascendantDefinitions) {

		final Definitions explicitDefinitions =
				value()
				.getExplicitDefinitions()
				.upgradeScope(ascendantDefinitions.getScope());

		return ascendantDefinitions.override(explicitDefinitions);
	}

	@Override
	public Path expand(MacroExpander expander) {
		return cantExpand(expander);
	}

	@Override
	public Path reexpand(MacroExpander expander) {
		return cantExpand(expander);
	}

	@Override
	protected Definitions explicitDefinitions() {

		final Ref ref =
				ValueType.MACRO.constantRef(this, distribute(), this);

		return ref.toDefinitions(definitionEnv());
	}

	private Path cantExpand(MacroExpander expander) {
		expander.getLogger().error(
				"cant_expand_macro",
				expander,
				"Macro can not be expanded");
		return null;
	}

}
