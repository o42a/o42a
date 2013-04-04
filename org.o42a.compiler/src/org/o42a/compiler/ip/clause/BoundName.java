/*
    Compiler
    Copyright (C) 2013 Ruslan Lopatin

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
package org.o42a.compiler.ip.clause;

import static org.o42a.compiler.ip.clause.BoundNameVisitor.BOUND_NAME_VISITOR;
import static org.o42a.compiler.ip.clause.IntervalInterpreter.invalidIntervalBracket;

import org.o42a.ast.phrase.BoundNode;
import org.o42a.ast.phrase.IntervalNode;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.util.string.Name;


final class BoundName implements LocationInfo {

	static BoundName leftBoundName(
			CompilerContext context,
			IntervalNode interval) {

		final BoundNode leftBound = interval.getLeftBound();

		if (leftBound == null) {
			return null;
		}

		final BoundName boundName = boundName(context, leftBound);

		if (!boundName.isBounded() && !interval.isLeftOpen()) {
			invalidIntervalBracket(
					context.getLogger(),
					interval.getLeftBracket());
		}

		return boundName;
	}

	static BoundName rightBoundName(
			CompilerContext context,
			IntervalNode interval) {

		final BoundNode rightBound = interval.getRightBound();

		if (rightBound == null) {
			return null;
		}

		final BoundName boundName = boundName(context, rightBound);

		if (!boundName.isBounded() && !interval.isRightOpen()) {
			invalidIntervalBracket(
					context.getLogger(),
					interval.getRightBracket());
		}

		return boundName;
	}

	private static BoundName boundName(
			CompilerContext context,
			BoundNode node) {
		if (node.toNoBound() != null) {
			return new BoundName(new Location(context, node), null, false);
		}
		return node.toExpression().accept(BOUND_NAME_VISITOR, context);
	}

	private final Location location;
	private final Name name;
	private final boolean bounded;

	BoundName(LocationInfo location, Name name, boolean bounded) {
		this.location = location.getLocation();
		this.name = name;
		this.bounded = bounded;
	}

	@Override
	public final Location getLocation() {
		return this.location;
	}

	public final Name getName() {
		return this.name;
	}

	public final boolean isBounded() {
		return this.bounded;
	}

	@Override
	public String toString() {
		if (this.location == null) {
			return super.toString();
		}
		if (this.name == null) {
			if (this.bounded) {
				return "*";
			}
			return "-";
		}
		if (this.bounded) {
			return this.name.toString();
		}
		return '-' + this.name.toString();
	}

}
