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
package org.o42a.core.ref.path.impl;

import org.o42a.core.Scope;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.Location;
import org.o42a.core.st.Reproducer;
import org.o42a.core.value.TypeParameters;


public class RebuiltInterface extends PathFragment {

	public static TypeRef rebuiltInterface(Ref ref) {
		return new RebuiltInterface(ref)
				.toPath()
				.bind(ref, ref.getScope())
				.typeRef(ref.distribute())
				.setParameters(new RebuiltTypeParameters(ref));
	}

	private final Ref ref;

	private RebuiltInterface(Ref ref) {
		this.ref = ref;
	}

	@Override
	public Path expand(PathExpander expander, int index, Scope start) {

		final TypeRef iface = this.ref.rebuiltInterface();

		return iface.getPath().getPath();
	}

	@Override
	public FieldDefinition fieldDefinition(Ref ref) {
		return this.ref.rebuiltFieldDefinition();
	}

	@Override
	public TypeRef iface(Ref ref) {
		return ref.toTypeRef();
	}

	@Override
	public String toString() {
		if (this.ref == null) {
			return super.toString();
		}
		return "Interface[" + this.ref + ']';
	}

	private static final class RebuiltTypeParameters extends TypeRefParameters {

		private final Ref ref;

		RebuiltTypeParameters(Ref ref) {
			this.ref = ref;
		}

		@Override
		public Scope getScope() {
			return this.ref.getScope();
		}

		@Override
		public Location getLocation() {
			return this.ref.getLocation();
		}

		@Override
		public TypeParameters<?> refine(TypeParameters<?> defaultParameters) {
			return getParameters().refine(defaultParameters);
		}

		@Override
		public TypeRefParameters prefixWith(PrefixPath prefix) {

			final Ref ref = this.ref.prefixWith(prefix);

			if (ref == this.ref) {
				return this;
			}

			return new RebuiltTypeParameters(ref);
		}

		@Override
		public TypeRefParameters reproduce(Reproducer reproducer) {
			return getParameters().reproduce(reproducer);
		}

		private TypeParameters<?> getParameters() {
			return this.ref.rebuiltInterface().getParameters();
		}

	}

}
