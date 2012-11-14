/*
    Intrinsics
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
package org.o42a.intrinsic.link;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.ValueTypeObject;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.LinkValueType;


public abstract class AbstractLinkValueTypeObject extends ValueTypeObject {

	public AbstractLinkValueTypeObject(
			MemberOwner owner,
			AnnotatedSources sources,
			LinkValueType linkType) {
		super(owner, sources);
		setValueStruct(
				linkType.linkStruct(ValueType.VOID.typeRef(this, getScope())));
	}

	@Override
	protected TypeParameters<?> determineTypeParameters() {

		final LinkValueType linkType = value().getValueType().toLinkType();

		return super.determineTypeParameters().add(
				linkType.interfaceKey(getContext().getIntrinsics()),
				ValueType.VOID.typeRef(this, getScope()));
	}

}
