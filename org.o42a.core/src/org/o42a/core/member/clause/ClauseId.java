/*
    Compiler Core
    Copyright (C) 2010 Ruslan Lopatin

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
package org.o42a.core.member.clause;

import static org.o42a.core.member.AdapterId.adapterTypeScope;
import static org.o42a.core.ref.path.Path.absolutePath;

import org.o42a.core.CompilerContext;
import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.artifact.StaticTypeRef;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ref.path.AbsolutePath;


public enum ClauseId {

	NAME() {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
			return null;
		}

	},

	ARGUMENT() {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
			return absolutePath(context, "clauses", "argument");
		}

	},

	IMPERATIVE() {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
			return absolutePath(context, "clauses", "imperative");
		}

	},

	STRING() {

		@Override
		public AbsolutePath adapterPath(CompilerContext context) {
			return absolutePath(context, "clauses", "string");
		}

	};

	public final boolean hasAdapterType() {
		return this != NAME;
	}

	public final StaticTypeRef adapterType(
			LocationSpec location,
			Distributor distributor) {
		if (!hasAdapterType()) {
			return null;
		}

		final AbsolutePath adapterPath = adapterPath(location.getContext());

		return adapterPath.target(location, distributor).toStaticTypeRef();
	}

	public abstract AbsolutePath adapterPath(CompilerContext context);

	public static ClauseId byAdapterType(StaticTypeRef adapterType) {

		final Obj type =
			adapterTypeScope(adapterType.getType()).getContainer().toObject();

		for (ClauseId clauseId : values()) {
			if (clauseId.hasAdapterType()) {
				continue;
			}

			final AbsolutePath adapterPath =
				clauseId.adapterPath(adapterType.getContext());

			if (type == adapterPath.resolveArtifact(
					adapterType.getContext()).toObject()) {
				return clauseId;
			}
		}

		return NAME;
	}

}
