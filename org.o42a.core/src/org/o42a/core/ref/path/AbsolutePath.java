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
package org.o42a.core.ref.path;

import static org.o42a.core.Distributor.declarativeDistributor;
import static org.o42a.core.ref.path.PathReproduction.reproducedPath;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.ir.HostOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.Reproducer;


public final class AbsolutePath extends Path {

	AbsolutePath() {
		super(true, new PathFragment[0]);
	}

	AbsolutePath(PathFragment... fragments) {
		super(true, fragments);
	}

	public AbsolutePath append(CompilerContext context, String... names) {

		AbsolutePath path = this;
		Obj object = resolveArtifact(context).toObject();

		for (String name : names) {

			final Member member = object.member(name);

			assert member != null :
				"Member \"" + name + "\" not found in " + object;

			path = path.append(member.getKey());
			object = member.getSubstance().toObject();
		}

		return path;
	}

	public final Ref target(CompilerContext context) {

		final Artifact<?> target = resolveArtifact(context);

		return target(target, declarativeDistributor(target.getContainer()));
	}

	public final Ref target(Scope scope) {

		final Artifact<?> target = resolveArtifact(scope.getContext());

		return target(target, declarativeDistributor(scope.getContainer()));
	}

	public final Ref target(Container container) {

		final Artifact<?> target = resolveArtifact(container.getContext());

		return target(target, declarativeDistributor(container));
	}

	@Override
	public Ref target(LocationSpec location, Distributor distributor) {
		return new AbsolutePathTarget(location, distributor, this);
	}

	public Container resolve(CompilerContext context) {
		return resolve(context.getRoot(), context.getRoot().getScope());
	}

	public Artifact<?> resolveArtifact(CompilerContext context) {
		return resolveArtifact(context.getRoot(), context.getRoot().getScope());
	}

	@Override
	public AbsolutePath append(PathFragment fragment) {
		return (AbsolutePath) super.append(fragment);
	}

	@Override
	public AbsolutePath append(MemberKey memberKey) {
		return (AbsolutePath) super.append(memberKey);
	}

	@Override
	public AbsolutePath append(Path path) {
		return (AbsolutePath) super.append(path);
	}

	@Override
	public AbsolutePath rebuild() {
		return (AbsolutePath) super.rebuild();
	}

	@Override
	public PathReproduction reproduce(
			LocationSpec location,
			Reproducer reproducer) {
		return reproducedPath(this);
	}

	@Override
	HostOp write(Code code, CodePos exit, HostOp start) {

		final Container target = resolve(start.getContext());

		return target.getScope().ir(start.getGenerator()).op(
				start.getBuilder(),
				code);
	}

	private static final class AbsolutePathTarget extends PathTarget {

		AbsolutePathTarget(
				LocationSpec location,
				Distributor distributor,
				Path path) {
			super(location, distributor, path);
		}

		@Override
		public Ref reproduce(Reproducer reproducer) {
			return new AbsolutePathTarget(
					this,
					reproducer.distribute(),
					getPath());
		}

		@Override
		protected boolean isKnownStatic() {
			return true;
		}

	}

}
