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

import org.o42a.core.Scope;
import org.o42a.core.ir.def.DefDirs;
import org.o42a.core.ir.def.Eval;
import org.o42a.core.ir.def.RefOpEval;
import org.o42a.core.ir.op.HostOp;
import org.o42a.core.ir.op.InlineValue;
import org.o42a.core.ir.op.ValDirs;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.core.value.link.TargetResolver;


public class LinkValueAdapter extends ValueAdapter {

	private final TypeParameters<KnownLink> expectedParameters;

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
		return getAdaptedRef().dereference();
	}

	@Override
	public TypeParameters<?> typeParameters(Scope scope) {
		return getAdaptedRef().typeParameters(scope);
	}

	@Override
	public Value<?> value(Resolver resolver) {
		return linkValue(
				getAdaptedRef(),
				resolver,
				getExpectedParameters());
	}

	@Override
	public void resolveTargets(TargetResolver resolver) {

		final Obj object = getAdaptedRef().getResolution().toObject();

		object.value().getDefinitions().resolveTargets(resolver);
	}

	@Override
	public InlineValue inline(Normalizer normalizer, Scope origin) {
		return null;
	}

	@Override
	public Eval eval() {

		final TypeParameters<?> fromParameters =
				getAdaptedRef()
				.typeParameters(getAdaptedRef().getScope());

		if (getExpectedParameters().assignableFrom(fromParameters)) {
			return new RefOpEval(getAdaptedRef());
		}

		final LinkValueType linkType =
				fromParameters.getValueType().toLinkType();

		return new LinkEval(getAdaptedRef(), linkType);
	}

	@Override
	protected void fullyResolve(FullResolver resolver) {
		getAdaptedRef().resolveAll(resolver.setRefUsage(VALUE_REF_USAGE));
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

			dirs.returnValue(from);
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
