/*
    Root Object Definition
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
package org.o42a.root.array;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.object.def.EscapeMode.ESCAPE_POSSIBLE;

import org.o42a.codegen.code.Block;
import org.o42a.common.builtin.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.EscapeMode;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.array.Array;
import org.o42a.core.value.array.ArrayItem;
import org.o42a.core.value.array.ArrayValueType;
import org.o42a.util.fn.Cancelable;


@SourcePath(relativeTo = ArrayValueTypeObject.class, value = "as_row.o42a")
final class ArrayAsRow extends AnnotatedBuiltin {

	private Ref array;

	ArrayAsRow(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public EscapeMode escapeMode(Scope scope) {
		return ESCAPE_POSSIBLE;
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> arrayValue = array().value(resolver);
		final TypeParameters<Array> arrayParams =
				arrayValue.getTypeParameters().toArrayParameters();
		final ArrayValueType arrayType =
				arrayParams.getValueType().toArrayType();
		final TypeParameters<Array> rowParams =
				arrayParams.convertTo(arrayType.setVariable(false));

		if (arrayValue.getKnowledge().isFalse()) {
			return rowParams.falseValue();
		}
		if (!arrayValue.getKnowledge().isKnownToCompiler()) {
			return rowParams.runtimeValue();
		}

		final Array array = arrayParams.cast(arrayValue).getCompilerValue();

		if (array.length() == 0) {

			final Array row = new Array(
					array,
					array.distribute(),
					rowParams,
					new ArrayItem[0]);

			return row.toValue();
		}

		return rowParams.runtimeValue();
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

		return new ArrayAsRowEval(this, inlineArray);
	}

	@Override
	public Eval evalBuiltin() {
		return new ArrayAsRowEval(this, null);
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

		dirs.returnValue(
				code,
				dirs.value().store(
						code,
						arrayVal.value(null, code)
						.toRec(null, code)
						.load(null, code),
						arrayVal.loadLength(null, code)));

		arrayDirs.done();
	}

	private static final class ArrayAsRowEval extends InlineEval {

		private final ArrayAsRow arrayAsRow;
		private final InlineValue inlineArray;

		ArrayAsRowEval(ArrayAsRow arrayAsRow, InlineValue inlineArray) {
			super(null);
			this.arrayAsRow = arrayAsRow;
			this.inlineArray = inlineArray;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			this.arrayAsRow.write(dirs, host, this.inlineArray);
		}

		@Override
		public String toString() {
			if (this.arrayAsRow == null) {
				return super.toString();
			}
			if (this.inlineArray == null) {
				return this.arrayAsRow.toString();
			}
			return "(" + this.inlineArray + "):as row";
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}
