package org.o42a.codegen;

import static org.o42a.util.fn.CondInit.condInit;

import java.util.function.BiPredicate;
import java.util.function.Function;

import org.o42a.util.fn.CondInit;


public interface Codegen {

	static <V extends Codegen> BiPredicate<Generator, V> hasGenerator() {
		return (generator, t) -> t.getGenerator() == generator;
	}

	static <V extends Codegen> CondInit<Generator, V> irInit(
			Function<Generator, V> init) {
		return condInit(hasGenerator(), init);
	}

	Generator getGenerator();

}
