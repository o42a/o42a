package org.o42a.core.ir.value.struct;

import org.o42a.codegen.code.Code;
import org.o42a.core.ir.value.ValHolder;


public final class NoValHolder extends ValHolder {

	public static final NoValHolder INSTANCE = new NoValHolder();

	private NoValHolder() {
	}

	@Override
	public void set(Code code) {
	}

	@Override
	public void hold(Code code) {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}