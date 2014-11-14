/*
    Root Object Definition
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
package org.o42a.root.array;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.common.builtin.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.EscapeAnalyzer;
import org.o42a.core.object.meta.EscapeFlag;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.array.Array;
import org.o42a.util.fn.Cancelable;
import org.o42a.util.string.ID;


abstract class IndexedLength extends AnnotatedBuiltin {

	private static final ID ARRAY_LEN_ID = ID.id("array_len");

	private Ref array;

	IndexedLength(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public EscapeFlag escapeFlag(EscapeAnalyzer analyzer, Scope scope) {
		return analyzer.escapePossible();
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> arrayValue = array().value(resolver);

		if (arrayValue.getKnowledge().isFalse()) {
			return type().getParameters().falseValue();
		}
		if (!arrayValue.getKnowledge().isKnownToCompiler()) {
			return type().getParameters().runtimeValue();
		}

		final Array array =
				typeParameters(resolver.getScope())
				.cast(arrayValue)
				.getCompilerValue();
		final int length = array.length();

		return ValueType.INTEGER
				.cast(type().getParameters())
				.compilerValue(Long.valueOf(length));
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
		array().resolveAll(resolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue inlineArray = array().inline(normalizer, origin);

		if (inlineArray == null) {
			return null;
		}

		return new ArrayLengthEval(this, inlineArray);
	}

	@Override
	public Eval evalBuiltin() {
		return new ArrayLengthEval(this, null);
	}

	@Override
	public String toString() {
		if (this.array == null) {
			return super.toString();
		}
		return "(" + this.array + "):length";
	}

	private TypeParameters<Array> typeParameters(Scope scope) {
		return array().typeParameters(scope).toArrayParameters();
	}

	private Ref array() {
		if (this.array != null) {
			return this.array;
		}

		final Path path = getScope().getEnclosingScopePath();

		return this.array = path.bind(this, getScope()).target(distribute());
	}

	private void write(DefDirs dirs, HostOp host, InlineValue inlineArray) {

		final ValDirs arrayDirs =
				dirs.dirs()
				.nested()
				.value("array_val", array().getValueType(), TEMP_VAL_HOLDER);
		final Block code = arrayDirs.code();

		final ValOp arrayVal;

		if (inlineArray != null) {
			arrayVal = inlineArray.writeValue(arrayDirs, host);
		} else {
			arrayVal = array().op(host).writeValue(arrayDirs);
		}

		final Int32op length = arrayVal.loadLength(ARRAY_LEN_ID, code);
		final ValOp result =
				dirs.value().store(code, length.toInt64(null, code));

		dirs.returnValue(code, result);
		arrayDirs.done();
	}

	private static final class ArrayLengthEval extends InlineEval {

		private final IndexedLength length;
		private final InlineValue inlineArray;

		ArrayLengthEval(IndexedLength length, InlineValue inlineArray) {
			super(null);
			this.length = length;
			this.inlineArray = inlineArray;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			this.length.write(dirs, host, this.inlineArray);
		}

		@Override
		public String toString() {
			if (this.length == null) {
				return super.toString();
			}
			if (this.inlineArray == null) {
				return this.length.toString();
			}
			return "(" + this.inlineArray + "):length";
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}
