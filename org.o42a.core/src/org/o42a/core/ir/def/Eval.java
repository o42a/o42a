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
package org.o42a.core.ir.def;

import org.o42a.core.ir.op.HostOp;


public interface Eval {

	Eval FALSE_EVAL = FalseEval.INSTANCE;

	Eval NO_EVAL = NoInlineEval.NO_INLINE_EVAL;

	Eval MACRO_EVAL = MacroEval.INSTANCE;

	void write(DefDirs dirs, HostOp host);

}
