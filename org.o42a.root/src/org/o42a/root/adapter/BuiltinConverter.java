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
package org.o42a.root.adapter;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;

import org.o42a.analysis.escape.EscapeAnalyzer;
import org.o42a.analysis.escape.EscapeFlag;
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
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.util.fn.Cancelable;


public abstract class BuiltinConverter<F, T> extends AnnotatedBuiltin {

	private Ref object;

	public BuiltinConverter(Obj owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	@SuppressWarnings("unchecked")
	public TypeParameters<T> getBuiltinTypeParameters() {
		return (TypeParameters<T>) type().getParameters();
	}

	@Override
	public EscapeFlag escapeFlag(EscapeAnalyzer analyzer, Scope scope) {
		return object().escapeFlag(analyzer, scope);
	}

	@Override
	public Value<?> calculateBuiltin(Resolver resolver) {

		final Value<?> objectValue = object().value(resolver);

		if (objectValue.getKnowledge().isFalse()) {
			return type().getParameters().falseValue();
		}
		if (!objectValue.getKnowledge().isKnown()) {
			return type().getParameters().runtimeValue();
		}

		@SuppressWarnings("unchecked")
		final TypeParameters<F> typeParameters =
				(TypeParameters<F>) objectValue.getTypeParameters();
		final F value =
				typeParameters.cast(objectValue).getCompilerValue();
		final T converted = convert(resolver, resolver, value);

		if (converted == null) {
			return type().getParameters().falseValue();
		}

		return getBuiltinTypeParameters().compilerValue(converted);
	}

	@Override
	public void resolveBuiltin(FullResolver resolver) {
		object().resolveAll(resolver);
	}

	@Override
	public InlineEval inlineBuiltin(Normalizer normalizer, Scope origin) {

		final InlineValue value = object().inline(normalizer, origin);

		if (value == null) {
			return null;
		}

		return new InlineConverter(this, value);
	}

	@Override
	public Eval evalBuiltin() {
		return new ConverterEval(this);
	}

	protected abstract T convert(
			LocationInfo location,
			Resolver resolver,
			F value);

	protected abstract ValOp convert(ValDirs targetDirs, ValOp value);

	protected final Ref object() {
		if (this.object != null) {
			return this.object;
		}

		final Path path = getScope().getEnclosingScopePath();

		return this.object = path.bind(this, getScope()).target(distribute());
	}

	private static final class InlineConverter extends InlineEval {

		private final BuiltinConverter<?, ?> converter;
		private InlineValue value;

		InlineConverter(BuiltinConverter<?, ?> converter, InlineValue value) {
			super(null);
			this.converter = converter;
			this.value = value;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final Ref object = this.converter.object();
			final ValDirs valueDirs = dirs.dirs().nested().value(
					"value",
					object.getValueType(),
					TEMP_VAL_HOLDER);
			final ValOp value = this.value.writeValue(valueDirs, host);

			final ValDirs targetDirs =
					valueDirs.dirs().nested().value(dirs.valDirs());

			dirs.returnValue(
					targetDirs.code(),
					this.converter.convert(targetDirs, value));

			targetDirs.done();
			valueDirs.done();
		}

		@Override
		public String toString() {
			if (this.converter == null) {
				return super.toString();
			}
			return this.converter.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class ConverterEval implements Eval {

		private final BuiltinConverter<?, ?> converter;

		ConverterEval(BuiltinConverter<?, ?> converter) {
			this.converter = converter;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final Ref object = this.converter.object();
			final ValDirs valueDirs = dirs.dirs().nested().value(
					"value",
					object.getValueType(),
					TEMP_VAL_HOLDER);
			final ValOp value = object.op(host).writeValue(valueDirs);

			final ValDirs targetDirs =
					valueDirs.dirs().nested().value(dirs.valDirs());

			dirs.returnValue(
					targetDirs.code(),
					this.converter.convert(targetDirs, value));

			targetDirs.done();
			valueDirs.done();
		}

		@Override
		public String toString() {
			if (this.converter == null) {
				return super.toString();
			}
			return this.converter.toString();
		}

	}

}
