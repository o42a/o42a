/*
    Compiler Core
    Copyright (C) 2011,2012 Ruslan Lopatin

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
package org.o42a.core.ref.impl.normalizer;

import static org.o42a.core.ref.MultiScope.multiScope;
import static org.o42a.core.ref.impl.normalizer.DerivativesMultiScope.objectMultiScope;
import static org.o42a.core.ref.impl.normalizer.LinkMultiScope.declaredFieldMultiScope;
import static org.o42a.core.ref.impl.normalizer.ReplacementsMultiScope.replacementsMultiScope;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.array.ArrayElement;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.member.local.MemberLocal;
import org.o42a.core.ref.*;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.Step;


public class MultiWalker implements PathWalker {

	private MultiScope multiScope;

	public MultiWalker() {
	}

	public MultiWalker(MultiScope start) {
		this.multiScope = start;
	}

	public final MultiScope getMultiScope() {
		return this.multiScope;
	}

	@Override
	public boolean root(BoundPath path, Scope root) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		assert !path.isStatic() :
			"Path is static";
		assert this.multiScope == null || this.multiScope.getScope() == start :
			"Wrong start of the path: " + start + ", but "
			+ this.multiScope.getScope() + " expected";

		if (this.multiScope == null) {
			this.multiScope = multiScope(start);
		}

		return true;
	}

	@Override
	public boolean module(Step step, Obj module) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean skip(Step step, Scope scope) {
		return true;
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean up(Container enclosed, Step step, Container enclosing) {
		return set(multiScope(enclosing.getScope()));
	}

	@Override
	public boolean member(Container container, Step step, Member member) {

		final MemberField fieldMember = member.toField();

		if (fieldMember == null) {

			final MemberLocal local = member.toLocal();

			assert local != null :
				"Neither field, nor local: " + member;

			this.multiScope = new PropagatedMultiScope(local.local());

			return true;
		}

		final Field<?> field = fieldMember.field(dummyUser());

		if (field.getArtifactKind().isVariable()) {
			// Variables not supported yet.
			return false;
		}
		if (getScopeSet().nothingButDerived()) {
			return set(replacementsMultiScope(field));
		}
		if (!field.isOverride()) {
			return set(declaredFieldMultiScope(field));
		}
		if (!getScopeSet().isInherited()) {
			return set(new InheritedFieldsMultiScope(
					field,
					getMultiScope().ancestors()));
		}
		return set(new MultiOwnerReplacementsMultiScope(
				getMultiScope(),
				field));
	}

	@Override
	public boolean arrayElement(Obj array, Step step, ArrayElement element) {
		// Arrays not supported yet.
		return false;
	}

	@Override
	public boolean refDep(Obj object, Step step, Ref dependency) {
		if (!getScopeSet().nothingButDerived()) {
			return false;
		}

		final LocalScope local =
				object.getScope().getEnclosingScope().toLocal();
		final MultiWalker walker =
				new MultiWalker(new PropagatedMultiScope(local));
		final Resolution resolution =
				dependency.resolve(local.walkingResolver(dummyUser(), walker));

		if (resolution.isError() || !resolution.isResolved()) {
			return false;
		}

		return set(walker.getMultiScope());
	}

	@Override
	public boolean materialize(Artifact<?> artifact, Step step, Obj result) {
		return set(getMultiScope().materialize());
	}

	@Override
	public boolean object(Step step, Obj object) {
		return set(objectMultiScope(object));
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
		this.multiScope = null;
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	private final MultiScopeSet getScopeSet() {
		return getMultiScope().getScopeSet();
	}

	private final boolean set(MultiScope multiScope) {
		this.multiScope = multiScope;
		return multiScope != null;
	}

}
