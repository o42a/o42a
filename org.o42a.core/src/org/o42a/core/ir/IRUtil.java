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
package org.o42a.core.ir;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ref.path.PathResolver.pathResolver;

import org.o42a.codegen.CodeId;
import org.o42a.codegen.Generator;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.AdapterId;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.object.Obj;
import org.o42a.core.object.array.ArrayElement;
import org.o42a.core.object.link.Link;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.Step;


public class IRUtil {

	public static CodeId encodeMemberId(
			Generator generator,
			MemberId memberId) {

		final String name = memberId.getName();

		if (name != null) {
			return generator.id(name);
		}

		final AdapterId adapterId = memberId.getAdapterId();
		final ScopeIR adapterTypeIR =
				adapterId.getAdapterTypeScope().ir(generator);

		return generator.id().type(adapterTypeIR.getId());
	}

	public static CodeId encodeMemberId(ScopeIR enclosingIR, Member member) {

		final Generator generator = enclosingIR.getGenerator();
		final MemberId memberId = member.getId();

		CodeId localId = addMemberId(generator, null, memberId);

		for (Scope reproducedFrom : memberId.getReproducedFrom()) {
			localId = addDeclaredIn(generator, localId, reproducedFrom);
		}

		final Scope enclosingScope = enclosingIR.getScope();
		final Scope rootScope =
				enclosingScope.getContext().getRoot().getScope();

		CodeId id;

		if (enclosingScope == rootScope) {
			id = generator.topId().setLocal(localId);
		} else {
			id = enclosingIR.getId().setLocal(localId);
		}

		if (member.isOverride()) {
			id = addDeclaredIn(generator, id, member.getKey().getOrigin());
		}

		return id;
	}

	private static CodeId addMemberId(
			Generator generator,
			CodeId prefix,
			MemberId memberId) {

		final String name = memberId.toName();

		if (name != null) {
			if (prefix == null) {
				return generator.id(name);
			}
			return prefix.sub(name);
		}

		final AdapterId adapterId = memberId.toAdapterId();

		if (adapterId != null) {

			final ScopeIR adapterTypeIR =
					adapterId.getAdapterTypeScope().ir(generator);

			if (prefix == null) {
				return generator.id().type(adapterTypeIR.getId());
			}

			return prefix.type(adapterTypeIR.getId());
		}

		final MemberId[] ids = memberId.toIds();

		if (ids != null) {

			CodeId result = prefix;

			for (MemberId id : ids) {
				result = addMemberId(generator, result, id);
			}

			return result;
		}

		throw new IllegalStateException(
				"Can not generate IR identifier for " + memberId);
	}

	private static CodeId addDeclaredIn(
			Generator generator,
			CodeId prefix,
			Scope scope) {

		final Clause clause = scope.getContainer().toClause();

		if (clause == null) {
			return prefix.in(scope.ir(generator).getId());
		}

		final Scope enclosingObjectScope =
				clause.getEnclosingObject().getScope();
		final CodeId id = addDeclaredIn(
				generator,
				prefix,
				enclosingObjectScope);
		final DeclaredInWriter writer = new DeclaredInWriter(generator, id);

		clause.pathInObject().bind(scope, enclosingObjectScope).walk(
				pathResolver(enclosingObjectScope, dummyUser()),
				writer);

		return writer.id;
	}

	private IRUtil() {
	}

	private static final class DeclaredInWriter implements PathWalker {

		private final Generator generator;
		private CodeId id;

		DeclaredInWriter(Generator generator, CodeId id) {
			this.generator = generator;
			this.id = id;
		}

		@Override
		public boolean root(BoundPath path, Scope root) {
			throw new IllegalStateException();
		}

		@Override
		public boolean start(BoundPath path, Scope start) {
			return true;
		}

		@Override
		public boolean module(Step step, Obj module) {
			throw new IllegalStateException();
		}

		@Override
		public boolean skip(Step step, Scope scope) {
			return true;
		}

		@Override
		public boolean staticScope(Step step, Scope scope) {
			return true;
		}

		@Override
		public boolean up(
				Container enclosed,
				Step step,
				Container enclosing,
				ReversePath reversePath) {
			throw new IllegalStateException();
		}

		@Override
		public boolean member(Container container, Step step, Member member) {
			this.id = addMemberId(this.generator, this.id, member.getId());
			return true;
		}

		@Override
		public boolean dereference(Obj linkObject, Step step, Link link) {
			throw new IllegalStateException();
		}

		@Override
		public boolean arrayElement(
				Obj array,
				Step step,
				ArrayElement element) {
			throw new IllegalStateException();
		}

		@Override
		public boolean refDep(Obj object, Step step, Ref dependency) {
			throw new IllegalStateException();
		}

		@Override
		public boolean object(Step step, Obj object) {
			throw new IllegalStateException();
		}

		@Override
		public void pathTrimmed(BoundPath path, Scope root) {
		}

		@Override
		public void abortedAt(Scope last, Step brokenStep) {
		}

		@Override
		public boolean done(Container result) {
			return true;
		}

	}

}
