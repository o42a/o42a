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

import org.o42a.common.macro.AnnotatedMacro;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.core.member.MemberOwner;
import org.o42a.core.object.Meta;
import org.o42a.core.object.macro.MacroExpander;
import org.o42a.core.object.meta.MetaDep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;


abstract class AbstractLinkCast extends AnnotatedMacro {

	private EnclosingLinkMetaDep linkDep;

	AbstractLinkCast(MemberOwner owner, AnnotatedSources sources) {
		super(owner, sources);
	}

	@Override
	public Path expand(MacroExpander expander) {
		registerLinkDep();
		return getScope().getEnclosingScopePath();
	}

	@Override
	public Path reexpand(MacroExpander expander) {
		registerLinkDep();
		return getScope().getEnclosingScopePath();
	}

	private void registerLinkDep() {
		if (this.linkDep != null) {
			return;
		}
		this.linkDep = new EnclosingLinkMetaDep(this);
		this.linkDep.register();
	}

	private static final class EnclosingLinkMetaDep extends MetaDep {

		private final Ref path;

		EnclosingLinkMetaDep(AbstractLinkCast cast) {
			super(cast.meta());
			this.path =
					cast.getScope()
					.getEnclosingScopePath()
					.bind(cast, cast.getScope())
					.target(cast.distribute());
		}

		@Override
		public MetaDep parentDep() {
			return null;
		}

		@Override
		public MetaDep nestedDep() {
			return null;
		}

		@Override
		protected boolean triggered(Meta meta) {

			final Resolver resolver = meta.getObject().getScope().resolver();
			final Meta linkMeta =
					this.path.resolve(resolver).toObject().meta();

			return linkMeta.isUpdated();
		}

		@Override
		protected boolean changed(Meta meta) {
			return true;
		}

	}

}
