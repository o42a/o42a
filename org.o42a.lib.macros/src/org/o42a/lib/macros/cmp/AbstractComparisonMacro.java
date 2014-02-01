/*
    Standard Macros
    Copyright (C) 2013,2014 Ruslan Lopatin

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
package org.o42a.lib.macros.cmp;

import static org.o42a.common.macro.Macros.expandMacroField;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.common.macro.AnnotatedMacro;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.phrase.part.BinaryPhraseOperator;
import org.o42a.common.ref.cmp.ComparisonExpression;
import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.Location;
import org.o42a.core.value.macro.MacroExpander;
import org.o42a.util.string.Name;


public abstract class AbstractComparisonMacro extends AnnotatedMacro {

	public static final Name WHAT = CASE_INSENSITIVE.canonicalName("what");
	public static final Name TO = CASE_INSENSITIVE.canonicalName("to");
	public static final Name THAN = CASE_INSENSITIVE.canonicalName("than");

	private final BinaryPhraseOperator operator;
	private Ref left;
	private Ref right;

	public AbstractComparisonMacro(
			Obj owner,
			AnnotatedSources sources,
			BinaryPhraseOperator operator) {
		super(owner, sources);
		this.operator = operator;
	}

	@Override
	public Path expand(MacroExpander expander) {
		return compare(expander);
	}

	@Override
	public Path reexpand(MacroExpander expander) {
		return compare(expander);
	}

	protected abstract Name leftName();

	protected abstract Name rightName();

	private Path compare(MacroExpander expander) {

		final Scope macroScope = expander.getMacroObject().getScope();
		final CompilerContext context = expander.getLocation().getContext();
		final Ref left = left();
		final Ref right = right();
		final ComparisonExpression comparison = new ComparisonExpression(
				expander,
				this.operator,
				left.rebuildIn(macroScope)
				.setLocation(new Location(context, left.getLocation())),
				right.rebuildIn(macroScope)
				.setLocation(new Location(context, right.getLocation())),
				false);

		return comparison.setResolutionLogger(expander.getLogger()).toPath();
	}

	private Ref left() {
		if (this.left != null) {
			return this.left;
		}
		return this.left = expandMacroField(
				fieldName(leftName()).key(getScope()),
				distribute());
	}

	private Ref right() {
		if (this.right != null) {
			return this.right;
		}
		return this.right = expandMacroField(
				fieldName(rightName()).key(getScope()),
				distribute());
	}

}
