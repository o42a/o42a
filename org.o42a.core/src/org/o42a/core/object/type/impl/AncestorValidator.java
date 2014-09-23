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
package org.o42a.core.object.type.impl;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.object.Obj;
import org.o42a.core.object.state.Dep;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.ReversePath;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.ref.path.Step;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.st.sentence.Local;
import org.o42a.core.value.link.Link;
import org.o42a.util.log.LogMessage;
import org.o42a.util.log.Severity;


public class AncestorValidator implements PathWalker {

	private final CompilerLogger logger;
	private final TypeRef ancestor;
	private AncestorError ancestorError;
	private final boolean clause;
	private boolean skipValidation;

	public AncestorValidator(
			CompilerLogger logger,
			TypeRef ancestor,
			boolean clause) {
		this.logger = logger;
		this.ancestor = ancestor;
		this.clause = clause;
	}

	@Override
	public boolean root(BoundPath path, Scope root) {
		return validLast();
	}

	@Override
	public boolean start(BoundPath path, Scope start) {
		return invalidLastParent();
	}

	@Override
	public boolean module(Step step, Obj module) {
		return validLast();
	}

	@Override
	public boolean staticScope(Step step, Scope scope) {
		return validLast();
	}

	@Override
	public boolean up(
			Container enclosed,
			Step step,
			Container enclosing,
			ReversePath reversePath) {
		return invalidLastParent();
	}

	@Override
	public boolean member(Container container, Step step, Member member) {
		return validLast();
	}

	@Override
	public boolean dereference(Obj linkObject, Step step, Link link) {
		return invalidLast(AncestorError.DEREF_ANCESTOR);
	}

	@Override
	public boolean local(Step step, Scope scope, Local local) {
		return validateNested(scope, local.ref());
	}

	@Override
	public boolean dep(Obj object, Dep dep) {
		return validateNested(object.getScope().getEnclosingScope(), dep.ref());
	}

	@Override
	public boolean object(Step step, Obj object) {
		// Eager reference can be inherited only when its ancestor
		// can be inherited.
		if (!object.value().getStatefulness().isEager()) {
			return validLast();
		}
		return validateNested(
				object.getScope().getEnclosingScope(),
				object.type().getAncestor().getRef());
	}

	@Override
	public boolean pathTrimmed(BoundPath path, Scope root) {
		return root(path, root);
	}

	@Override
	public void abortedAt(Scope last, Step brokenStep) {
		throw new IllegalStateException(
				"Ancestor validation aborted unexpectedly");
	}

	@Override
	public boolean done(Container result) {
		if (this.ancestorError == null) {
			return true;
		}
		if (!this.skipValidation) {
			this.logger.log(this.ancestorError, this.ancestor);
		}
		return false;
	}

	private boolean validLast() {
		this.ancestorError = null;
		return true;
	}

	private boolean invalidLast(AncestorError ancestorError) {
		this.ancestorError = ancestorError;
		return true;
	}

	private boolean invalidLastParent() {
		if (this.clause) {
			// Clauses may refer to enclosing objects.
			// This will be re-validated in phrase.
			return validLast();
		}
		return invalidLast(AncestorError.PARENT_ANCESTOR);
	}

	private boolean validateNested(Scope start, Ref ref) {

		final boolean prev = this.skipValidation;

		this.skipValidation = true;
		try {
			ref.getPath().walk(
					start.resolver().toPathResolver(),
					this).isResolved();
		} finally {
			this.skipValidation = prev;
		}

		return true;
	}

	private enum AncestorError implements LogMessage {

		DEREF_ANCESTOR(
				"invalid_deref_ancestor",
				"Invalid ancestor. Link target can not be inherited"),
		PARENT_ANCESTOR(
				"invalid_parent_ancestor",
				"Invalid ancestor. Parent reference can not be inherited");

		private final String code;
		private final String defaultMessage;

		AncestorError(String code, String defaultMessage) {
			this.code = code;
			this.defaultMessage = defaultMessage;
		}

		@Override
		public Severity getSeverity() {
			return Severity.ERROR;
		}

		@Override
		public String getCode() {
			return this.code;
		}

		@Override
		public String getText() {
			return this.defaultMessage;
		}

	}

}
