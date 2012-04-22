/*
    Compiler Core
    Copyright (C) 2012 Ruslan Lopatin

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
package org.o42a.core.object.link.impl;

import static org.o42a.core.value.Value.falseValue;

import org.o42a.core.Scope;
import org.o42a.core.object.Obj;
import org.o42a.core.object.link.*;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.value.Value;


final class LinkCopy extends KnownLink {

	static Value<?> linkValue(
			Ref ref,
			Resolver resolver,
			LinkValueType toLinkType) {

		final Resolution linkResolution = ref.resolve(resolver);

		if (linkResolution.isError()) {
			return falseValue();
		}

		final Obj linkObject = linkResolution.toObject();
		final Value<?> value =
				linkObject.value().explicitUseBy(resolver).getValue();
		final LinkValueStruct sourceStruct =
				value.getValueStruct().toLinkStruct();
		final LinkValueStruct resultStruct =
				sourceStruct.setValueType(toLinkType);

		if (value.getKnowledge().isFalse()) {
			return resultStruct.falseValue();
		}
		if (!value.getKnowledge().isKnownToCompiler()) {
			return resultStruct.runtimeValue();
		}
		if (sourceStruct.getValueType().isRuntimeConstructed()) {
			// Run time constructed link can not be copied at compile time.
			return resultStruct.runtimeValue();
		}

		final PrefixPath prefix =
				ref.getPath().toPrefix(resolver.getScope());
		final KnownLink sourceLink =
				sourceStruct.cast(value).getCompilerValue();
		final TargetRef targetRef =
				sourceLink.getTargetRef().prefixWith(prefix);

		return resultStruct.compilerValue(
				new LinkCopy(sourceLink, toLinkType, targetRef));
	}

	private final LinkValueType toLinkType;
	private final Link copyOf;

	private LinkCopy(
			Link copyOf,
			LinkValueType toLinkType,
			TargetRef targetRef) {
		super(
				copyOf,
				copyOf.distributeIn(targetRef.getScope().getContainer()),
				targetRef);
		this.toLinkType = toLinkType;
		this.copyOf = copyOf;
	}

	private LinkCopy(LinkCopy prototype, TargetRef targetRef) {
		super(prototype, targetRef);
		this.toLinkType = prototype.toLinkType;
		this.copyOf = prototype.copyOf;
	}

	@Override
	public LinkValueType getValueType() {
		return this.toLinkType;
	}

	@Override
	protected TargetRef buildTargetRef() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Link findLinkIn(Scope enclosing) {
		return this.copyOf.findIn(enclosing);
	}

	@Override
	protected KnownLink prefixWith(PrefixPath prefix) {
		return new LinkCopy(
				this,
				getTargetRef().prefixWith(prefix));
	}

}
