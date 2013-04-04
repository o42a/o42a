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

import static org.o42a.compiler.ip.Interpreter.location;
import static org.o42a.compiler.ip.clause.BoundName.leftBoundName;
import static org.o42a.compiler.ip.clause.BoundName.rightBoundName;
import static org.o42a.compiler.ip.clause.ClauseInterpreter.invalidClauseId;
import static org.o42a.core.member.clause.ClauseDeclaration.clauseDeclaration;
import static org.o42a.core.member.clause.ClauseId.*;

import org.o42a.ast.atom.SignNode;
import org.o42a.ast.phrase.IntervalBracket;
import org.o42a.ast.phrase.IntervalNode;
import org.o42a.core.Distributor;
import org.o42a.core.member.clause.ClauseDeclaration;
import org.o42a.core.member.clause.ClauseId;
import org.o42a.core.source.CompilerLogger;


public class IntervalInterpreter {

	public static void invalidIntervalBracket(
			CompilerLogger logger,
			SignNode<IntervalBracket> bracket) {
		logger.error(
				"invalid_interval_bracket",
				bracket,
				"Unbounded interval endpoint should be opened. "
				+ "Use parenthesis instead of square bracket here");
	}

	static ClauseDeclaration intervalClauseDeclaration(
			IntervalNode interval,
			Distributor p) {

		final BoundName left = leftBoundName(p.getContext(), interval);
		final BoundName right = rightBoundName(p.getContext(), interval);

		if (left == null && right == null) {
			return anoymousUnboundedInterval(interval, p);
		}
		if (right == null) {
			return boundedIntervalStart(interval, p, left);
		}
		if (left == null) {
			return boundedIntervalEnd(interval, p, right);
		}
		if (!left.isBounded() && !right.isBounded()) {
			return unboundedInterval(interval, p, left, right);
		}
		if (!right.isBounded()) {
			return leftBoundedInterval(interval, p, left, right);
		}
		if (!left.isBounded()) {
			return rightBoundedInterval(interval, p, left, right);
		}

		// Both interval bounds specified.
		invalidClauseId(p.getContext(), interval);

		return null;
	}

	private static ClauseDeclaration anoymousUnboundedInterval(
			IntervalNode interval,
			Distributor p) {
		if (!interval.isLeftOpen()) {
			invalidIntervalBracket(
					p.getLogger(),
					interval.getLeftBracket());
		}
		if (!interval.isRightOpen()) {
			invalidIntervalBracket(
					p.getLogger(),
					interval.getRightBracket());
		}
		return clauseDeclaration(
				location(p, interval),
				p,
				null,
				UNBOUNDED_INTERVAL);
	}

	private static ClauseDeclaration unboundedInterval(
			IntervalNode interval,
			Distributor p,
			BoundName left,
			BoundName right) {
		if (right.getName() != null) {
			invalidClauseId(p.getContext(), right.getLocation());
		}
		return clauseDeclaration(
				location(p, interval),
				p,
				left.getName(),
				UNBOUNDED_INTERVAL);
	}

	private static ClauseDeclaration boundedIntervalStart(
			IntervalNode interval,
			Distributor p,
			BoundName left) {
		if (!left.isBounded()) {
			invalidClauseId(p.getContext(), interval);
			return null;
		}

		final ClauseId clauseId;

		if (interval.isLeftOpen()) {
			if (interval.isRightOpen()) {
				clauseId = OPEN_INTERVAL_START;
			} else {
				clauseId = LEFT_OPEN_INTERVAL_START;
			}
		} else if (interval.isRightOpen()) {
			clauseId = RIGHT_OPEN_INTERVAL_START;
		} else {
			clauseId = CLOSED_INTERVAL_START;
		}

		return clauseDeclaration(
				location(p, interval),
				p,
				left.getName(),
				clauseId);
	}

	private static ClauseDeclaration boundedIntervalEnd(
			IntervalNode interval,
			Distributor p,
			BoundName right) {
		if (!right.isBounded()) {
			invalidClauseId(p.getContext(), interval);
			return null;
		}

		final ClauseId clauseId;

		if (interval.isLeftOpen()) {
			if (interval.isRightOpen()) {
				clauseId = OPEN_INTERVAL_END;
			} else {
				clauseId = LEFT_OPEN_INTERVAL_END;
			}
		} else if (interval.isRightOpen()) {
			clauseId = RIGHT_OPEN_INTERVAL_END;
		} else {
			clauseId = CLOSED_INTERVAL_END;
		}

		return clauseDeclaration(
				location(p, interval),
				p,
				right.getName(),
				clauseId);
	}

	private static ClauseDeclaration leftBoundedInterval(
			IntervalNode interval,
			Distributor p,
			BoundName left,
			BoundName right) {
		if (right.getName() != null) {
			invalidClauseId(p.getContext(), right.getLocation());
			return null;
		}
		return clauseDeclaration(
				location(p, interval),
				p,
				left.getName(),
				interval.isLeftOpen()
				? LEFT_BOUNDED_OPEN_INTERVAL : LEFT_BOUNDED_CLOSED_INTERVAL);
	}

	private static ClauseDeclaration rightBoundedInterval(
			IntervalNode interval,
			Distributor p,
			BoundName left,
			BoundName right) {
		if (left.getName() != null) {
			invalidClauseId(p.getContext(), left.getLocation());
			return null;
		}
		return clauseDeclaration(
				location(p, interval),
				p,
				right.getName(),
				interval.isRightOpen()
				? RIGHT_BOUNDED_OPEN_INTERVAL : RIGHT_BOUNDED_CLOSED_INTERVAL);
	}

	private IntervalInterpreter() {
	}

}
