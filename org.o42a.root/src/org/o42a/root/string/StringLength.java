/*
    Root Object Definition
    Copyright (C) 2011-2013 Ruslan Lopatin

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
package org.o42a.root.string;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.root.string.StringChar.STR_LEN_ID;

import org.o42a.codegen.code.Block;
import org.o42a.codegen.code.op.Int32op;
import org.o42a.common.builtin.AnnotatedBuiltin;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.core.Scope;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.InlineEval;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.fn.Cancelable;


@SourcePath(relativeTo = StringValueTypeObject.class, value = "length.o42a")
final class StringLength extends AnnotatedBuiltin {

	private Ref string;

	public StringLength(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> stringValue = string().value(resolver);

		if (stringValue.getKnowledge().isFalse()) {
			return type().getParameters().falseValue();
		}
		if (!stringValue.getKnowledge().isKnown()) {
			return type().getParameters().runtimeValue();
		}

		final String string =
				ValueType.STRING.cast(stringValue).getCompilerValue();
		final int length = string.length();

		return ValueType.INTEGER.cast(type().getParameters())
				.compilerValue(Long.valueOf(length));
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
		string().resolveAll(resolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue inlineString = string().inline(normalizer, origin);

		if (inlineString == null) {
			return null;
		}

		return new LengthEval(this, inlineString);
	}

	@Override
	public Eval evalBuiltin() {
		return new LengthEval(this, null);
	}

	@Override
	public String toString() {
		if (this.string == null) {
			return super.toString();
		}
		return this.string + ":length";
	}

	private Ref string() {
		if (this.string != null) {
			return this.string;
		}

		final Path path = getScope().getEnclosingScopePath();

		return this.string = path.bind(this, getScope()).target(distribute());
	}

	private void write(DefDirs dirs, HostOp host, InlineValue inlineString) {

		final ValDirs stringDirs = dirs.dirs().nested().value(
				"string_val",
				ValueType.STRING,
				TEMP_VAL_HOLDER);
		final Block code = stringDirs.code();

		final ValOp stringVal;

		if (inlineString != null) {
			stringVal = inlineString.writeValue(stringDirs, host);
		} else {
			stringVal = string().op(host).writeValue(stringDirs);
		}

		final Int32op length = stringVal.loadLength(STR_LEN_ID, code);
		final ValOp result =
				dirs.value().store(code, length.toInt64(null, code));

		dirs.returnValue(code, result);
		stringDirs.done();
	}

	private static final class LengthEval extends InlineEval {

		private final StringLength length;
		private final InlineValue inlineString;

		LengthEval(StringLength length, InlineValue inlineString) {
			super(null);
			this.length = length;
			this.inlineString = inlineString;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {
			this.length.write(dirs, host, this.inlineString);
		}

		@Override
		public String toString() {
			if (this.length == null) {
				return super.toString();
			}
			if (this.inlineString == null) {
				return this.length.toString();
			}
			return this.inlineString + ":length";
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}
