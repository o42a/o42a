/*
    Compiler Core
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.core.ref.impl;

import static org.o42a.core.ref.RefPurity.IMPURE_REF;
import static org.o42a.core.ref.RefPurity.PURE_REF;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.alias.MemberAlias;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefPurity;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.Step;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.value.link.Link;


public class RefPurityDetector implements PathWalker {

	public static RefPurity detectPurity(Ref ref, Scope scope) {

		final RefPurityDetector detector = new RefPurityDetector();

		ref.resolve(scope.walkingResolver(detector)).resolve();

		return detector.purity;
	}

	private RefPurity purity = PURE_REF;

	private RefPurityDetector() {
	}

	@Override
	public boolean root(BoundPath path, Scope root) {
		return pure();
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		return pure();
	}

	@Override
	public boolean module(Step step, Obj module) {
		return pure();
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return pure();
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
		return pure();
	}

	@Override
	public boolean member(Container container, Step step, Member member) {

		final MemberField field = member.toField();

		if (field != null) {
			return pure();
		}

		final MemberAlias alias = member.toAlias();

		if (alias != null) {
			return setPurity(alias.getRef().purity(container.getScope()));
		}

		return impure();
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		return impure();
	}

	@Override
	public boolean local(Step step, Scope scope, Local local) {
		return setPurity(local.ref().purity(scope));
	}

	@Override
	public boolean dep(Obj object, Dep dep) {
		return setPurity(
				dep.ref().purity(object.getScope().getEnclosingScope()));
	}

	@Override
	public boolean object(Step step, Obj object) {

		final TypeRef ancestor = object.type().getAncestor();

		if (ancestor == null) {
			return pure();
		}

		return setPurity(
				ancestor.getRef().purity(object.getScope().getEnclosingScope()));
	}

	@Override
	public boolean pathTrimmed(BoundPath path, Scope root) {
		return true;
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	private final boolean setPurity(RefPurity purity) {
		this.purity = purity;
		return purity.isPure();
	}

	private final boolean pure() {
		return true;
	}

	private final boolean impure() {
		this.purity = IMPURE_REF;
		return false;
	}

}
