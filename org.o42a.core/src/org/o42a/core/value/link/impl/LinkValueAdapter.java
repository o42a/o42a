/*
    Compiler Core
    Copyright (C) 2012,2013 Ruslan Lopatin

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
package org.o42a.core.value.link.impl;

import static org.o42a.core.ir.value.ValHolderFactory.TEMP_VAL_HOLDER;
import static org.o42a.core.ref.RefUsage.VALUE_REF_USAGE;
import static org.o42a.core.value.link.impl.LinkCopy.linkValue;

import org.o42a.codegen.code.Block;
import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.RefOpEval;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.ref.*;
import org.o42a.core.value.*;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.core.value.link.TargetResolver;
import org.o42a.util.fn.Cancelable;


public class LinkValueAdapter extends ValueAdapter {

	private final TypeParameters<KnownLink> expectedParameters;
	private Ref target;

	public LinkValueAdapter(
			Ref adaptedRef,
			TypeParameters<KnownLink> expectedParameters) {
		super(adaptedRef);
		assert expectedParameters != null :
			"Link value structure not specified";
		this.expectedParameters = expectedParameters;
	}

	public final TypeParameters<KnownLink> getExpectedParameters() {
		return this.expectedParameters;
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public final Ref toTarget() {
		if (this.target != null) {
			return this.target;
		}
		return this.target = getAdaptedRef().dereference();
	}

	@Override
	public TypeParameters<KnownLink> typeParameters(Scope scope) {
		return getAdaptedRef()
				.typeParameters(scope)
				.toLinkParameters()
				.convertTo(this.expectedParameters.getValueType());
	}

	@Override
	public Value<?> value(Resolver resolver) {
		return linkValue(
				getAdaptedRef(),
				resolver,
				typeParameters(resolver.getScope()));
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {
		resolver.resolveTarget(toTarget().getResolution().toObject());
	}

	@Override
	public InlineValue inline(Normalizer normalizer, Scope origin) {

		final InlineValue inline = getAdaptedRef().inline(normalizer, origin);

		if (inline == null) {
			return null;
		}

		final ValueType<?> fromType = getAdaptedRef().getValueType();

		if (getExpectedParameters().getValueType().is(fromType)) {
			return inline;
		}

		return new InlineLinkEval(inline, fromType.toLinkType());
	}

	@Override
	public Eval eval() {

		final ValueType<?> fromType = getAdaptedRef().getValueType();

		if (getExpectedParameters().getValueType().is(fromType)) {
			return new RefOpEval(getAdaptedRef());
		}

		return new LinkEval(getAdaptedRef(), fromType.toLinkType());
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getAdaptedRef().resolveAll(resolver.setRefUsage(VALUE_REF_USAGE));
	}

	private static final class InlineLinkEval extends InlineValue {

		private final InlineValue inline;
		private final LinkValueType fromValueType;

		InlineLinkEval(InlineValue inline, LinkValueType fromValueType) {
			super(null);
			this.inline = inline;
			this.fromValueType = fromValueType;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			this.inline.writeCond(dirs, host);
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {

			final ValDirs fromDirs = dirs.dirs().nested().value(
					this.fromValueType,
					TEMP_VAL_HOLDER);

			final ValOp from = this.inline.writeValue(fromDirs, host);
			final Block code = fromDirs.code();
			final ValOp converted = dirs.value().store(
					code,
					from.value(null, code).toRec(null, code).load(null, code));

			fromDirs.done();

			return converted;
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

	private static final class LinkEval implements Eval {

		private final LinkValueType fromValueType;
		private final Ref ref;

		LinkEval(Ref ref, LinkValueType fromValueType) {
			this.ref = ref;
			this.fromValueType = fromValueType;
		}

		public final Ref getRef() {
			return this.ref;
		}

		@Override
		public void write(DefDirs dirs, HostOp host) {

			final ValDirs fromDirs = dirs.dirs().nested().value(
					this.fromValueType,
					TEMP_VAL_HOLDER);

			final ValOp from = getRef().op(host).writeValue(fromDirs);
			final Block code = fromDirs.code();
			final ValOp converted = dirs.value().store(
					code,
					from.value(null, code).toRec(null, code).load(null, code));

			dirs.returnValue(code, converted);

			fromDirs.done();
		}

		@Override
		public String toString() {
			if (this.ref == null) {
				return super.toString();
			}
			return this.ref.toString();
		}

	}

}
