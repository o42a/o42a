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
package org.o42a.core.ref.path;

import static org.o42a.core.ref.path.impl.RebuiltInterface.rebuiltInterface;

import org.o42a.core.Scope;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.impl.PathFragmentFieldDefinition;
import org.o42a.core.ref.path.impl.PathFragmentStep;
import org.o42a.core.ref.type.TypeRef;


public abstract class AbstractPathFragment {

	AbstractPathFragment() {
	}

	public abstract Path expand(PathExpander expander, int index, Scope start);

	/**
	 * Whether this fragment is a template.
	 *
	 * <p>The template fragment may be expanded multiple times in different
	 * scopes. The expansions may differ, unlike the non-template fragment.
	 * This is particularly useful for macro expansion.</p>
	 *
	 * @return <code>true</code> to form a {@link Path#isTemplate() template
	 * path}, or <code>false</code> otherwise.
	 */
	public abstract boolean isTemplate();

	public abstract FieldDefinition fieldDefinition(Ref ref);

	public abstract TypeRef iface(Ref ref);

	public final Step toStep() {
		return new PathFragmentStep(this);
	}

	public final Path toPath() {
		return toStep().toPath();
	}

	protected final FieldDefinition defaultFieldDefinition(Ref ref) {
		return new PathFragmentFieldDefinition(ref);
	}

	protected final TypeRef defaultInterface(Ref ref) {
		return rebuiltInterface(ref);
	}

}
