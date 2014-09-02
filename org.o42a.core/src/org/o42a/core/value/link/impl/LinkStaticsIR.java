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

import static org.o42a.core.ir.IRNames.CONST_ID;
import static org.o42a.core.ir.value.Val.VAL_CONDITION;

import org.o42a.core.ir.value.Val;
import org.o42a.core.ir.value.type.CachingStaticsIR;
import org.o42a.core.ir.value.type.ValueTypeIR;
import org.o42a.core.object.Obj;
import org.o42a.core.value.link.KnownLink;
import org.o42a.util.string.ID;


public class LinkStaticsIR extends CachingStaticsIR<KnownLink> {

	private static final ID LINK_PREFIX = CONST_ID.sub("LINK");
	private static final ID VAR_PREFIX = CONST_ID.sub("VAR");

	private int constSeq;

	public LinkStaticsIR(ValueTypeIR<KnownLink> valueTypeIR) {
		super(valueTypeIR);
	}

	@Override
	public Val val(KnownLink value) {

		final Obj target =
				value.getTargetRef()
				.getRef()
				.getResolution()
				.toObject();

		return new Val(
				getValueType(),
				VAL_CONDITION,
				0,
				target.ir(getGenerator()).ptr().toAny());
	}

	@Override
	protected ID constId(KnownLink value) {
		return constIdPrefix().anonymous(++this.constSeq);
	}

	private final ID constIdPrefix() {
		if (getValueType().isVariable()) {
			return VAR_PREFIX;
		}
		return LINK_PREFIX;
	}

}
