/*
    Compiler Core
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
package org.o42a.core.value.link.impl;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueKnowledge;
import org.o42a.core.value.link.*;


final class TargetLink extends KnownLink {

	static Value<?> linkByValue(Ref ref, TypeParameters<KnownLink> parameters) {

		final LinkValueType linkType = parameters.getValueType().toLinkType();
		final TargetLink link = new TargetLink(
				ref.toTargetRef(linkType.interfaceRef(parameters)),
				ref.distribute(),
				linkType);
		final ValueKnowledge knowledge = link.getKnowledge();

		if (!knowledge.hasCompilerValue()) {
			if (knowledge.isKnownToCompiler()) {
				return parameters.falseValue();
			}
			return parameters.runtimeValue();
		}

		return parameters.compilerValue(link);
	}

	private final LinkValueType linkType;

	private TargetLink(
			TargetRef targetRef,
			Distributor distributor,
			LinkValueType linkType) {
		super(targetRef, distributor, targetRef);
		this.linkType = linkType;
	}

	private TargetLink(TargetLink prototype, TargetRef targetRef) {
		super(prototype, targetRef);
		this.linkType = prototype.linkType;
	}

	@Override
	public LinkValueType getValueType() {
		return this.linkType;
	}

	@Override
	protected Link findLinkIn(Scope enclosing) {

		final TargetRef targetRef = getTargetRef().upgradeScope(enclosing);

		return new TargetLink(this, targetRef);
	}

	@Override
	protected KnownLink prefixWith(PrefixPath prefix) {
		return new TargetLink(this, getTargetRef().prefixWith(prefix));
	}

}
