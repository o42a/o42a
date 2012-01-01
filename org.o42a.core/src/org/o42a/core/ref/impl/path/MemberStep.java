/*
    Compiler Core
    Copyright (C) 2010-2012 Ruslan Lopatin

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
package org.o42a.core.ref.impl.path;

import static org.o42a.core.ref.path.PathReproduction.reproducedPath;
import static org.o42a.core.ref.path.PathReproduction.unchangedPath;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.path.*;
import org.o42a.core.source.LocationInfo;


public class MemberStep extends AbstractMemberStep {

	public MemberStep(MemberKey memberKey) {
		super(memberKey);
	}

	@Override
	protected FieldDefinition fieldDefinition(
			BoundPath path,
			Distributor distributor) {
		return defaultFieldDefinition(path, distributor);
	}

	@Override
	protected Container resolve(
			PathResolver resolver,
			BoundPath path,
			int index,
			Scope start,
			PathWalker walker) {

		final Member member = resolveMember(path, index, start);

		if (member == null) {
			return null;
		}

		walker.member(start.getContainer(), this, member);

		return member.substance(resolver);
	}

	@Override
	protected void normalize(PathNormalizer normalizer) {
		if (!normalizer.isStepIgnored()) {
			return;
		}

		// Step will be ignored, so just add it to normalized path.
		final Member member = resolveMember(
				normalizer.getPath(),
				normalizer.getStepIndex(),
				normalizer.getStepStart());

		if (member == null) {
			return;
		}

		normalizer.add(
				member.substance(dummyUser()).getScope(),
				new NormalStep() {
					@Override
					public void cancel() {
					}
					@Override
					public Path appendTo(Path path) {
						return path.append(getMemberKey());
					}
				});
	}

	@Override
	protected PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer,
			Scope origin,
			Scope scope) {

		final Member member = origin.getContainer().member(getMemberKey());

		if (origin.getContainer().toClause() == null
				&& member.toClause() == null) {
			// Neither clause, nor member of clause.
			// Return unchanged.
			return unchangedPath(toPath());
		}

		final MemberKey reproductionKey =
				getMemberKey().getMemberId().reproduceFrom(origin).key(scope);

		return reproducedPath(reproductionKey.toPath());
	}

}
