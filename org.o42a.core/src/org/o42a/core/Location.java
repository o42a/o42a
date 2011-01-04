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
package org.o42a.core;

import org.o42a.ast.Node;
import org.o42a.util.log.LogInfo;
import org.o42a.util.log.Loggable;
import org.o42a.util.log.LoggableVisitor;


public class Location implements LocationSpec {

	private final CompilerContext context;
	private final Node node;

	public Location(LocationSpec location) {
		assert location != null :
			"Location not specified";
		this.context = location.getContext();
		this.node = location.getNode();
	}

	public Location(CompilerContext context, Node node) {
		assert context != null :
			"Compiler context not specified";
		this.context = context;
		this.node = node;
	}

	@Override
	public final CompilerContext getContext() {
		return this.context;
	}

	@Override
	public Loggable getLoggable() {

		final Node node = getNode();

		return node != null ? node : this;
	}

	@Override
	public Node getNode() {
		return this.node;
	}

	@Override
	public Object getLoggableData() {
		return this;
	}

	@Override
	public LogInfo getPreviousLogInfo() {
		return null;
	}

	@Override
	public <R, P> R accept(LoggableVisitor<R, P> visitor, P p) {

		final Node node = getNode();

		if (node != null) {
			return node.accept(visitor, p);
		}

		return visitor.visitData(this, p);
	}

	public final CompilerLogger getLogger() {
		return getContext().getLogger();
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder();

		out.append(getClass().getSimpleName()).append('[');
		out.append(this.context);

		final Node node = getNode();

		if (node != null) {
			out.append("]:");
			node.printContent(out);
		} else {
			out.append(']');
		}

		return out.toString();
	}

}
