/*
    Compiler Commons
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
package org.o42a.common.builtin;

import org.o42a.core.Scope;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.EscapeAnalyzer;
import org.o42a.core.object.meta.EscapeFlag;
import org.o42a.core.ref.FullResolver;
import org.o42a.core.ref.Normalizer;
import org.o42a.core.ref.Resolver;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;


public interface Builtin extends LocationInfo {

	boolean isConstantBuiltin();

	TypeParameters<?> getBuiltinTypeParameters();

	Obj toObject();

	EscapeFlag escapeFlag(EscapeAnalyzer analyzer, Scope scope);

	Value<?> calculateBuiltin(Resolver resolver);

	void resolveBuiltin(FullResolver resolver);

	InlineEval inlineBuiltin(Normalizer normalizer, Scope origin);

	Eval evalBuiltin();

}
