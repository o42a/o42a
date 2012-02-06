package org.o42a.backend.llvm.code;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.code.Code;


final class LLCodeInset extends LLInset {

	LLCodeInset(LLCode enclosing, LLInset prevInset, Code code, CodeId id) {
		super(enclosing, prevInset, code, id);
	}

}
