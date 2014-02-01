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
package org.o42a.core.ref.path;

import static org.o42a.core.ref.path.impl.RebuiltInterface.rebuiltInterface;

import org.o42a.core.Scope;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.impl.PathFragmentFieldDefinition;
import org.o42a.core.ref.type.TypeRef;


public abstract class BoundFragment {

	private final BoundPathFragment fragment = new BoundPathFragment(this);

	public abstract Path expand(PathExpander expander, int index, Scope start);

	public abstract FieldDefinition fieldDefinition(Ref ref);

	public abstract TypeRef iface(Ref ref);

	protected final FieldDefinition defaultFieldDefinition(Ref ref) {
		return new PathFragmentFieldDefinition(ref);
	}

	protected final TypeRef defaultInterface(Ref ref) {
		return rebuiltInterface(ref);
	}

	final BoundPath appendTo(BoundPath path) {

		final BoundPath origin = path.append(this.fragment);

		this.fragment.origin = origin;

		return origin;
	}

	private static final class BoundPathFragment extends PathFragment {

		private final BoundFragment fragment;
		private BoundPath origin;
		private Path expansion;
		private boolean expanded;

		BoundPathFragment(BoundFragment fragment) {
			this.fragment = fragment;
		}

		@Override
		public Path expand(PathExpander expander, int index, Scope start) {
			if (this.expanded) {
				return this.expansion;
			}
			if (!expander.getPath().getOrigin().is(this.origin.getOrigin())) {
				this.origin.rebuild();
				this.expanded = true;
				return this.expansion;
			}

			this.expanded = true;

			return this.expansion =
					this.fragment.expand(expander, index, start);
		}

		@Override
		public FieldDefinition fieldDefinition(Ref ref) {
			return this.fragment.fieldDefinition(ref);
		}

		@Override
		public TypeRef iface(Ref ref) {
			return this.fragment.iface(ref);
		}

		@Override
		public int hashCode() {
			return this.fragment.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}

			final BoundPathFragment other = (BoundPathFragment) obj;

			return this.fragment.equals(other.fragment);
		}

		@Override
		public String toString() {
			if (this.fragment == null) {
				return super.toString();
			}
			return this.fragment.toString();
		}

	}

}
