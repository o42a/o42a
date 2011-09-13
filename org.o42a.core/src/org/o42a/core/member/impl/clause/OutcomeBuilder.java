package org.o42a.core.member.impl.clause;

import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.ScopeInfo;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberContainer;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.ClauseKind;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.ResolutionWalker;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.path.PathWalker;
import org.o42a.core.source.CompilerLogger;
import org.o42a.core.source.LocationInfo;


public class OutcomeBuilder implements ResolutionWalker, PathWalker {

	private final LocationInfo location;
	private Container container;
	private Path outcome;

	public OutcomeBuilder(Ref location) {
		this.location = location;
		this.container = location.getContainer();
	}

	public final Path getOutcome() {
		return this.outcome;
	}

	@Override
	public PathWalker path(LocationInfo location, Path path) {
		return this;
	}

	@Override
	public boolean newObject(ScopeInfo location, Obj object) {
		return invalidOutcome();
	}

	@Override
	public boolean artifactPart(
			LocationInfo location,
			Artifact<?> artifact,
			Artifact<?> part) {
		return invalidOutcome();
	}

	@Override
	public boolean staticArtifact(LocationInfo location, Artifact<?> artifact) {
		return invalidOutcome();
	}

	@Override
	public boolean root(Path path, Scope root) {
		return unexpectedAbsolutePath();
	}

	@Override
	public boolean start(Path path, Scope start) {
		return true;
	}

	@Override
	public boolean module(PathFragment fragment, Obj module) {
		return unexpectedAbsolutePath();
	}

	@Override
	public boolean up(
			Container enclosed,
			PathFragment fragment,
			Container enclosing) {
		if (this.outcome != null) {
			return invalidOutcome();
		}

		final Clause containerClause = this.container.toClause();

		if (containerClause == null) {
			return invalidOutcome();
		}
		if (enclosing.toClause() == null
				&& containerClause.getKind() == ClauseKind.EXPRESSION) {
			return invalidOutcome();
		}

		this.container = enclosing;

		return true;
	}

	@Override
	public boolean member(
			Container container,
			PathFragment fragment,
			Member member) {

		final Clause containerClause = this.container.toClause();

		if (containerClause != null) {

			final MemberKey containerKey = containerClause.toMember().getKey();
			final MemberKey key = member.getKey();

			if (containerKey.startsWith(key)) {
				return up(container, fragment, member.substance(dummyUser()));
			}

			final Scope memberScope = member.getScope();

			if (this.container.getScope() == memberScope) {
				// Clause in the same scope.
				// Enclosing scope as a topmost container.

				final MemberContainer memberContainer =
						memberScope.getContainer();

				if (!up(this.container, fragment, memberContainer)) {
					return false;
				}
			}
		}

		final Field<?> field = member.toField(dummyUser());

		if (field == null) {
			return invalidOutcome();
		}

		if (!initOutcome()) {
			return false;
		}

		this.outcome = this.outcome.append(field.getKey());

		return true;
	}

	@Override
	public boolean fieldDep(
			Obj object,
			PathFragment fragment,
			Field<?> dependency) {
		return invalidOutcome();
	}

	@Override
	public boolean refDep(Obj object, PathFragment fragment, Ref dependency) {
		return invalidOutcome();
	}

	@Override
	public boolean materialize(
			Artifact<?> artifact,
			PathFragment fragment,
			Obj result) {
		if (!initOutcome()) {
			return false;
		}

		this.outcome = this.outcome.materialize();

		return true;
	}

	@Override
	public void abortedAt(Scope last, PathFragment brokenFragment) {
	}

	@Override
	public boolean done(Container result) {
		return initOutcome();
	}

	private CompilerLogger getLogger() {
		return this.location.getContext().getLogger();
	}

	private final boolean initOutcome() {
		if (this.outcome != null) {
			return true;
		}

		this.outcome = pathInObject(this.container);

		return this.outcome != null;
	}

	private Path pathInObject(Container container) {

		final Clause clause = container.toClause();

		if (clause == null) {
			return Path.SELF_PATH;
		}

		return pathInObject(clause).getPath();
	}

	private PathInObject pathInObject(Clause clause) {
		switch (clause.getKind()) {
		case EXPRESSION:
			if (clause.isTopLevel()) {
				return new PathInObject();
			}

			invalidOutcome();

			return null;
		case GROUP:
			return pathInObject(clause.getEnclosingClause());
		case OVERRIDER:

			final MemberKey overridden =
					clause.toPlainClause().getOverridden();
			final PathInObject enclosingPathInObject =
					pathInObject(clause.getEnclosingClause());

			return enclosingPathInObject.append(overridden);
		}

		throw new IllegalStateException(
				"Clause of unsupported kind: " + clause);
	}

	private boolean invalidOutcome() {
		getLogger().error(
				"invalid_clause_outcome",
				this.location,
				"Clause outcome should be a constructed object's field");
		return false;
	}

	private boolean unexpectedAbsolutePath() {
		getLogger().error(
				"unexpected_clause_outcome_absolute_path",
				this.location,
				"Clause outcome should be a relative path");
		return false;
	}

	private static final class PathInObject {

		private final Path path;
		private final MemberKey lastFieldKey;

		PathInObject() {
			this.path = Path.SELF_PATH;
			this.lastFieldKey = null;
		}

		PathInObject(Path path, MemberKey lastFieldKey) {
			this.path = path;
			this.lastFieldKey = lastFieldKey;
		}

		public Path getPath() {
			return this.path;
		}

		public MemberKey getLastFieldKey() {
			return this.lastFieldKey;
		}

		public Path materialize() {
			if (this.lastFieldKey == null) {
				return this.path;
			}

			final MemberContainer origin =
					this.lastFieldKey.getOrigin().getContainer();
			final Member lastMember = origin.member(getLastFieldKey());
			final Field<?> lastField = lastMember.toField(dummyUser());

			if (lastField.getArtifactKind().isObject()) {
				return this.path;
			}

			return this.path.materialize();
		}

		public PathInObject append(MemberKey memberKey) {
			return new PathInObject(materialize().append(memberKey), memberKey);
		}

		@Override
		public String toString() {
			if (this.path == null) {
				return super.toString();
			}
			return this.path.toString();
		}

	}

}
