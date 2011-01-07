/*
    Compiler Core
    Copyright (C) 2010,2011 Ruslan Lopatin

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
package org.o42a.core.member.field;

import java.util.HashMap;

import org.o42a.core.artifact.Artifact;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.ExpressionScope;


public abstract class FieldScope extends ExpressionScope {

	private HashMap<MemberKey, Field<?>> propagatedFields;

	@SuppressWarnings("unchecked")
	public <A extends Artifact<A>> Field<A> propagateField(Field<A> sample) {

		final MemberKey key = sample.getKey();
		Field<A> propagated;

		if (this.propagatedFields == null) {
			this.propagatedFields = new HashMap<MemberKey, Field<?>>();
			propagated = null;
		} else {
			propagated = (Field<A>) this.propagatedFields.get(key);
		}
		if (propagated == null) {
			propagated = sample.propagate(getScope());
			if (propagated == sample) {
				return sample;
			}
			this.propagatedFields.put(key, propagated);
		}

		return propagated;
	}

}
