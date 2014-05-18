/*
    Compiler
    Copyright (C) 2011-2014 Ruslan Lopatin

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
package org.o42a.compiler.ip.type.ancestor;

import org.o42a.common.phrase.Phrase;
import org.o42a.compiler.ip.type.ParamTypeRef;
import org.o42a.core.member.field.AscendantsDefinition;


public final class AncestorTypeRef {

	public static AncestorTypeRef ancestorTypeRef(ParamTypeRef ancestor) {
		if (ancestor == null) {
			return null;
		}
		return new AncestorTypeRef(ancestor, false);
	}

	public static AncestorTypeRef macroAncestorTypeRef(ParamTypeRef ancestor) {
		if (ancestor == null) {
			return null;
		}
		return new AncestorTypeRef(ancestor, true);
	}

	public static AncestorTypeRef impliedAncestorTypeRef() {
		return new AncestorTypeRef(null, false);
	}

	private final ParamTypeRef ancestor;
	private final boolean mactoExpanding;

	private AncestorTypeRef(
			ParamTypeRef ancestor,
			boolean macroExpanding) {
		this.ancestor = ancestor;
		this.mactoExpanding = macroExpanding;
	}

	public final boolean isImplied() {
		return this.ancestor == null;
	}

	public final boolean isMacroExpanding() {
		return this.mactoExpanding;
	}

	public final ParamTypeRef getAncestor() {
		return this.ancestor;
	}

	public final AscendantsDefinition applyTo(
			AscendantsDefinition ascendants) {
		return getAncestor().updateAncestor(ascendants);
	}

	public final Phrase applyTo(Phrase phrase) {
		return getAncestor().updateAncestor(phrase);
	}

	@Override
	public String toString() {
		return this.ancestor != null ? this.ancestor.toString() : "*";
	}

}
