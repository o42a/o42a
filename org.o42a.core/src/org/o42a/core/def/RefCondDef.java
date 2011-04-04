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
package org.o42a.core.def;

import static org.o42a.core.def.Rescoper.transparentRescoper;
import static org.o42a.core.ref.Logical.logicalTrue;

import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.Logical;
import org.o42a.core.ref.Ref;


abstract class RefCondDef extends CondDef {

	static RefCondDef refCondDef(Ref ref) {
		return refCondDef(sourceOf(ref), ref);
	}

	static RefCondDef refCondDef(Obj source, Ref ref) {
		return new SimpleDef(source, ref);
	}

	private final Ref ref;
	private Ref rescopedRef;

	RefCondDef(
			Obj source,
			Ref ref,
			Logical prerequisite,
			Rescoper rescoper) {
		super(source, ref, prerequisite, rescoper);
		this.ref = ref;
	}

	RefCondDef(
			RefCondDef prototype,
			Logical prerequisite,
			Rescoper rescoper) {
		super(prototype, prerequisite, rescoper);
		this.ref = prototype.ref;
	}

	public final Ref getRescopedRef() {
		if (this.rescopedRef != null) {
			return this.rescopedRef;
		}
		return this.rescopedRef = this.ref.rescope(getRescoper());
	}

	@Override
	public DefKind getKind() {
		return DefKind.PROPOSITION;
	}

	@Override
	public RefCondDef and(Logical logical) {

		final Ref newRef = this.ref.and(logical);

		if (this.ref == newRef) {
			return this;
		}

		return new ConditionalDef(this, newRef);
	}

	@Override
	protected Logical getLogical() {
		return this.ref.getLogical();
	}

	@Override
	protected abstract RefCondDef create(
			Rescoper rescoper,
			Rescoper additionalRescoper);

	private static final class SimpleDef extends RefCondDef {

		SimpleDef(Obj source, Ref ref) {
			super(
					source,
					ref,
					logicalTrue(ref, ref.getScope()),
					transparentRescoper(ref.getScope()));
		}

		SimpleDef(
				RefCondDef prototype,
				Logical prerequisite,
				Rescoper rescoper) {
			super(prototype, prerequisite, rescoper);
		}

		@Override
		public boolean hasPrerequisite() {
			return false;
		}

		@Override
		protected Logical buildPrerequisite() {
			return logicalTrue(this, getScope());
		}

		@Override
		protected SimpleDef create(
				Rescoper rescoper,
				Rescoper additionalRescoper) {
			return new SimpleDef(this, prerequisite(), rescoper);
		}

	}

	private static class ConditionalDef extends RefCondDef {

		private final RefCondDef def;

		ConditionalDef(RefCondDef def, Ref ref) {
			super(
					def.getSource(),
					ref,
					null,
					def.getRescoper());
			this.def = def;
		}

		ConditionalDef(
				ConditionalDef prototype,
				Logical prerequisite,
				Rescoper rescoper) {
			super(prototype, prerequisite, rescoper);
			this.def = prototype.def;
		}

		@Override
		public DefKind getKind() {
			return this.def.getKind();
		}

		@Override
		public boolean hasPrerequisite() {
			return this.def.hasPrerequisite();
		}

		@Override
		protected Logical buildPrerequisite() {
			return this.def.getPrerequisite();
		}

		@Override
		protected ConditionalDef create(
				Rescoper rescoper,
				Rescoper additionalRescoper) {
			return new ConditionalDef(this, prerequisite(), rescoper);
		}

	}

}
