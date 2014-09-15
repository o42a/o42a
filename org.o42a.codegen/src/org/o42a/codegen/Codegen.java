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
package org.o42a.codegen;

import static org.o42a.util.fn.CondInit.condInit;

import java.util.function.BiPredicate;
import java.util.function.Function;

import org.o42a.util.fn.CondInit;


public interface Codegen {

	static <V extends Codegen> BiPredicate<Generator, V> hasGenerator() {
		return (generator, t) -> t.getGenerator().is(generator);
	}

	static <V extends Codegen> CondInit<Generator, V> irInit(
			Function<Generator, V> init) {
		return condInit(hasGenerator(), init);
	}

	Generator getGenerator();

}
