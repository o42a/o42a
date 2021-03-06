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
package org.o42a.core.ir.def;

import static org.o42a.core.ir.def.NoInlineEval.NO_INLINE_EVAL;

import org.o42a.core.ref.Normal;
import org.o42a.core.ref.Normalizer;


public abstract class InlineEval extends Normal implements Eval {

	public static InlineEval noInlineEval() {
		return NO_INLINE_EVAL;
	}

	public static InlineEval falseInlineEval() {
		return FalseEval.INSTANCE;
	}

	public static InlineEval voidInlineEval() {
		return VoidEval.INSTANCE;
	}

	public static InlineEval macroInlineEval() {
		return MacroEval.INSTANCE;
	}

	public InlineEval(Normalizer normalizer) {
		super(normalizer);
	}

}
