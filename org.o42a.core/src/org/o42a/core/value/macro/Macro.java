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
package org.o42a.core.value.macro;

import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathTemplate;


/**
 * Macro value representation.
 */
public interface Macro {

	/**
	 * Expands the macro.
	 *
	 * <p>This is called when whe path contains a macro expansion and a macro
	 * expansion {@link Consumer#expandMacro(Ref, PathTemplate, Ref) requested
	 * by consumer}.</p>
	 *
	 * <p>This method should register the {@link MetaDep meta-dependencies}
	 * apart from dependency on the macro itself.<p>
	 *
	 * @param expander macro expander.
	 *
	 * @return expanded path.
	 */
	Path expand(MacroExpander expander);

	/**
	 * Re-expands the macro.
	 *
	 * <p>This is called when whe path contains a macro re-expansion and a macro
	 * expansion {@link Consumer#expandMacro(Ref, PathTemplate, Ref) requested
	 * again} for already expanded macro, but in another scope. The re-expansion
	 * may lead to the different result.</p>
	 *
	 * <p>In contrast to {@link #expand(MacroExpander)} method, this one should
	 * not register any new dependencies, as they should be registered already
	 * by the former.<p>
	 *
	 * @param expander macro expander.
	 *
	 * @return re-expanded path.
	 */
	Path reexpand(MacroExpander expander);

}
