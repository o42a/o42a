/*
    Compiler Core
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.core.ref;

import org.o42a.core.Container;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.clause.Clause;
import org.o42a.util.use.AllUsages;
import org.o42a.util.use.Usage;


public abstract class RefUsage extends Usage<RefUsage> {

	public static final AllUsages<RefUsage> ALL_REF_USAGES =
			new AllUsages<RefUsage>(RefUsage.class);

	public static final RefUsage LOGICAL_REF_USAGE =
			new ValueUsage("RefLogical");
	public static final RefUsage VALUE_REF_USAGE =
			new ValueUsage("RefValue");
	public static final RefUsage TYPE_REF_USAGE =
			new TypeUsage("RefType");
	public static final RefUsage TARGET_REF_USAGE =
			new ResolutionUsage("RefTarget");
	public static final RefUsage ASSIGNEE_REF_USAGE =
			new ResolutionUsage("RefAssignee");
	public static final RefUsage RESOLUTION_REF_USAGE =
			new ResolutionUsage("RefResolution");

	protected abstract void fullyResolve(
			Resolution resolution,
			Container resolved);

	private RefUsage(String name) {
		super(ALL_REF_USAGES, name);
	}

	private static final class ValueUsage extends RefUsage {

		ValueUsage(String name) {
			super(name);
		}

		@Override
		protected void fullyResolve(Resolution resolution, Container resolved) {

			final Obj materialized = resolved.toArtifact().materialize();

			if (materialized != null) {
				materialized.value().resolveAll(resolution.getResolver());
			}
		}

	}

	private static final class TypeUsage extends RefUsage {

		TypeUsage(String name) {
			super(name);
		}

		@Override
		protected void fullyResolve(Resolution resolution, Container resolved) {

			final Obj materialized = resolved.toArtifact().materialize();

			if (materialized != null) {
				materialized.type().useBy(resolution.getResolver());
			}
		}

	}

	private static final class ResolutionUsage extends RefUsage {

		ResolutionUsage(String name) {
			super(name);
		}

		@Override
		protected void fullyResolve(Resolution resolution, Container resolved) {

			final Clause clause = resolved.toClause();

			if (clause != null) {
				clause.resolveAll();
			} else {
				resolved.toArtifact().resolveAll();
			}
		}

	}
}
