/*
    Compiler Code Generator
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.codegen.code;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import org.o42a.util.string.ID;


public final class SimpleSignature<R> extends ExtSignature<R, ExtFunc<R>> {

	SimpleSignature(
			ID id,
			ArrayList<Consumer<SignatureBuilder>> args,
			Function<SignatureBuilder, Return<R>> ret) {
		super(id, args, ret, ExtFunc<R>::new);
	}

}
