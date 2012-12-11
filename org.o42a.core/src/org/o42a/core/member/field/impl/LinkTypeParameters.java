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
package org.o42a.core.member.field.impl;

import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.ObjectTypeParameters;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.link.KnownLink;
import org.o42a.core.value.link.LinkValueType;
import org.o42a.util.log.Loggable;


final class LinkTypeParameters
		implements ObjectTypeParameters {

	private final LinkValueType linkType;
	private final TypeRef ancestor;
	private final Ref value;

	LinkTypeParameters(
			LinkValueType linkType,
			TypeRef ancestor,
			Ref value) {
		this.linkType = linkType;
		this.ancestor = ancestor;
		this.value = value;
	}

	@Override
	public CompilerContext getContext() {
		return this.value.getContext();
	}

	@Override
	public Loggable getLoggable() {
		return this.value.getLoggable();
	}

	@Override
	public boolean isDefaultTypeParameters() {
		return false;
	}

	@Override
	public TypeParameters<?> refine(
			Obj object,
			TypeParameters<?> defaultParameters) {

		final TypeRef interfaceRef =
				this.ancestor.setParameters(
						this.value.typeParameters(object.getScope()));
		final TypeParameters<KnownLink> linkParameters =
				this.linkType.typeParameters(interfaceRef);

		return linkParameters.refine(object, defaultParameters);
	}

	@Override
	public ObjectTypeParameters prefixWith(PrefixPath prefix) {

		final TypeRef ancestor = this.ancestor.prefixWith(prefix);
		final Ref value = this.value.prefixWith(prefix);

		if (this.ancestor == ancestor && this.value == value) {
			return this;
		}

		return new LinkTypeParameters(this.linkType, ancestor, value);
	}

	@Override
	public ObjectTypeParameters reproduce(Reproducer reproducer) {

		final TypeRef ancestor = this.ancestor.reproduce(reproducer);

		if (ancestor == null) {
			return null;
		}

		final Ref value = this.value.reproduce(reproducer);

		if (value == null) {
			return null;
		}

		return new LinkTypeParameters(this.linkType, ancestor, value);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return super.toString();
		}
		return "`" + this.value;
	}

}
