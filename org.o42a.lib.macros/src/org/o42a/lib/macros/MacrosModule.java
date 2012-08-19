/*
    Standard Macros
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
package org.o42a.lib.macros;

import org.o42a.common.object.AnnotatedModule;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.source.CompilerContext;
import org.o42a.util.string.ID;


@SourcePath("macros.o42a")
public class MacrosModule extends AnnotatedModule {

	public static final ID MACROS_ID = ID.id("macros");

	public static MacrosModule macrosModule(CompilerContext parentContext) {
		return new MacrosModule(
				parentContext,
				moduleSources(MacrosModule.class));
	}

	private MacrosModule(
			CompilerContext parentContext,
			AnnotatedSources sources) {
		super(parentContext, sources);
	}

}