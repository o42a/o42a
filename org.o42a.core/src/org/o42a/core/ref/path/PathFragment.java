/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.path.impl.PathFragmentFieldDefinition;
import org.o42a.core.ref.path.impl.PathFragmentStep;


public abstract class PathFragment {

	/**
	 * This is ivoked by {@link BoundPath#consume(Consumer)} to inform the
	 * fragment about the way the path is consumed.
	 *
	 * <p>Such information is not always available, but it's necessary e.g. for
	 * macro expansion. It is a fragment's responsibility to log an error if
	 * this method is never called.</p>
	 *
	 * @param path consumed path.
	 * @param consumer path consumer.
	 */
	public void consume(BoundPath path, Consumer consumer) {
	}

	public abstract Path expand(PathExpander expander, int index, Scope start);

	public FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return new PathFragmentFieldDefinition(path, distributor);
	}

	public final Step toStep() {
		return new PathFragmentStep(this);
	}

	public final Path toPath() {
		return toStep().toPath();
	}

}
