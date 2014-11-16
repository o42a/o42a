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

import static org.o42a.analysis.use.User.dummyUser;

import java.util.function.Function;

import org.o42a.analysis.escape.EscapeAnalyzer;
import org.o42a.analysis.escape.EscapeFlag;
import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.alias.MemberAlias;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.meta.DetectEscapeFlag;
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.Step;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.value.link.Link;


public class EscapeModeDetector implements PathWalker {

	public static EscapeFlag detectEscapeFlag(
			EscapeAnalyzer analyzer,
			Ref ref,
			Scope scope,
			DetectEscapeFlag detect) {

		final EscapeModeDetector detector =
				new EscapeModeDetector(analyzer, detect);

		if (!ref.resolve(scope.walkingResolver(detector)).isResolved()) {
			return analyzer.escapePossible();
		}

		return detector.escapeFlag.apply(analyzer);
	}

	private final EscapeAnalyzer analyzer;
	private final DetectEscapeFlag detect;
	private boolean isStatic;
	private Function<EscapeAnalyzer, EscapeFlag> escapeFlag;

	private EscapeModeDetector(
			EscapeAnalyzer analyzer,
			DetectEscapeFlag detect) {
		this.analyzer = analyzer;
		this.detect = detect;
	}

	@Override
	public boolean root(BoundPath path, Scope root) {
		this.isStatic = true;
		return ownEscapeMode(root.toObject());
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		this.isStatic = false;
		return derivativesEscapeMode(start.toObject());
	}

	@Override
	public boolean module(Step step, Obj module) {
		return ownEscapeMode(module);
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return ownEscapeMode(scope.toObject());
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
		return derivativesEscapeMode(enclosing.toObject());
	}

	@Override
	public boolean member(Container container, Step step, Member member) {

		final MemberField field = member.toField();

		if (field != null) {
			return overridersEscapeMode(field.field(dummyUser()).toObject());
		}

		final MemberAlias alias = member.toAlias();

		if (alias != null) {
			return nested(alias.getRef(), container.getScope());
		}

		return escape();
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {

		final EscapeFlag linkEscapeFlag = this.escapeFlag.apply(this.analyzer);

		if (!linkEscapeFlag.isEscapeImpossible()) {
			return escapeFlag(a -> linkEscapeFlag);
		}

		return escapeFlag(a -> a.escapeImpossible());
	}

	@Override
	public boolean local(Step step, Scope scope, Local local) {
		return nested(local.ref(), scope);
	}

	@Override
	public boolean dep(Obj object, Dep dep) {
		return nested(dep.ref(), object.getScope().getEnclosingScope());
	}

	@Override
	public boolean object(Step step, Obj object) {
		return overridersEscapeMode(object);
	}

	@Override
	public boolean pathTrimmed(BoundPath path, Scope root) {
		return root(path, root);
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
	}

	@Override
	public boolean done(Container result) {
		return true;
	}

	private final boolean escape() {
		this.escapeFlag = null;
		return false;
	}

	private final boolean ownEscapeMode(Obj object) {
		if (object == null) {
			return escape();
		}
		return escapeFlag(a -> object.analysis().ownEscapeFlag(a));
	}

	private final boolean overridersEscapeMode(Obj object) {
		if (object == null) {
			return escape();
		}
		if (this.isStatic) {
			return ownEscapeMode(object);
		}
		return escapeFlag(
				a -> object.analysis().overridersEscapeFlag(a, this.detect));
	}

	private final boolean derivativesEscapeMode(Obj object) {
		if (object == null) {
			return escape();
		}
		if (this.isStatic) {
			return ownEscapeMode(object);
		}
		return escapeFlag(
				a -> object.analysis().derivativesEscapeFlag(a, this.detect));
	}

	private final boolean escapeFlag(
			Function<EscapeAnalyzer, EscapeFlag> escapeFlag) {
		this.escapeFlag = escapeFlag;
		return true;
	}

	private final boolean nested(Ref ref, Scope scope) {
		return ref.resolve(scope.walkingResolver(this)).isResolved();
	}

}
