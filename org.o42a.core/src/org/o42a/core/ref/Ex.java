/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.ref;

import org.o42a.ast.Node;
import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.Scope;


public abstract class Ex extends Ref {

	private Resolution resolved;

	public Ex(LocationSpec location, Distributor distributor) {
		super(location, distributor);
	}

	@Override
	public final Resolution resolve(Scope scope) {
		if (scope == getScope()) {
			if (this.resolved != null) {
				return this.resolved;
			}
			return this.resolved = doResolveExpression(getScope());
		}
		return scope.resolveExpression(this);
	}

	public final Resolution getResolved() {
		return this.resolved;
	}

	@Override
	public String toString() {

		final Resolution resolved = getResolved();

		if (resolved != null) {
			return resolved.toString();
		}

		final Node node = getNode();

		if (node != null) {

			final StringBuilder out = new StringBuilder();

			out.append('[');
			node.printContent(out);
			out.append("]?");

			return out.toString();
		}

		return getClass().getSimpleName() + '?';
	}

	protected abstract Resolution resolveExpression(Scope scope);

	@Override
	protected Ex clone() throws CloneNotSupportedException {

		final Ex clone = (Ex) super.clone();

		clone.resolved = null;

		return clone;
	}

	final Resolution doResolveExpression(Scope scope) {
		assertCompatible(scope);

		final Resolution resolution = resolveExpression(scope);

		if (resolution != null) {
			return resolution;
		}

		return noResolution();
	}

}
