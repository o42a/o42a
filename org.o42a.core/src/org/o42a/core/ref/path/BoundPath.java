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
package org.o42a.core.ref.path;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.ir.op.PathOp.hostPathOp;
import static org.o42a.core.ref.path.PathNormalizer.pathNormalizer;
import static org.o42a.core.ref.path.PathResolution.noPathResolutionError;
import static org.o42a.core.ref.path.PathResolution.pathResolution;
import static org.o42a.core.ref.path.PathResolution.pathResolutionError;
import static org.o42a.core.ref.path.PathResolver.pathResolver;
import static org.o42a.core.ref.path.PathWalker.DUMMY_PATH_WALKER;
import static org.o42a.core.ref.path.impl.AncestorFragment.ANCESTOR_FRAGMENT;

import org.o42a.analysis.Analyzer;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.CodeBuilder;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.op.CodeDirs;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.*;
import org.o42a.core.ref.impl.cond.RefCondition;
import org.o42a.core.ref.impl.normalizer.UnNormalizedPath;
import org.o42a.core.ref.path.impl.*;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.Statement;
import org.o42a.core.st.sentence.Statements;
import org.o42a.util.ArrayUtil;


public class BoundPath extends RefPath {

	private final Scope origin;
	private final Path rawPath;
	private final PathNormalizationDoubt doubt =
			new PathNormalizationDoubt(this);
	private Path path;

	private Scope startObjectScope;
	private Obj startObject;
	private int startIndex;

	BoundPath(LocationInfo location, Scope origin, Path rawPath) {
		super(location);
		this.origin = origin;
		this.rawPath = rawPath;
	}

	private BoundPath(LocationInfo location, BoundPath prototype) {
		super(location);
		this.origin = prototype.origin;
		this.rawPath = prototype.rawPath;
		this.path = prototype.path;
	}

	public final Scope getOrigin() {
		return this.origin;
	}

	public final PathKind getKind() {
		return getPath().getKind();
	}

	public final boolean isAbsolute() {
		return getPath().isAbsolute();
	}

	public final boolean isStatic() {
		return getPath().isStatic();
	}

	public final boolean isKnownStatic() {
		return getRawPath().isStatic();
	}

	public final boolean isSelf() {
		return getPath().isSelf();
	}

	public final Path getPath() {
		if (this.path != null) {
			return this.path;
		}
		return this.path = rebuildPath();
	}

	public final Path getRawPath() {
		if (this.path != null) {
			return this.path;
		}
		return this.rawPath;
	}

	public final Path getTemplate() {
		return getRawPath().getTemplate();
	}

	public final boolean hasTemplate(PathTemplate template) {
		return getTemplate().hasTemplate(template);
	}

	public final Step[] getSteps() {
		return getPath().getSteps();
	}

	public final int length() {
		return getSteps().length;
	}

	public final int rawLength() {
		return getRawSteps().length;
	}

	public final Step firstStep() {

		final Step[] steps = getSteps();

		if (steps.length == 0) {
			return null;
		}

		return steps[0];
	}

	public final Step lastStep() {
		return getPath().lastStep();
	}

	public final BoundPath setLocation(LocationInfo location) {
		return new BoundPath(location, this);
	}

	public final BoundPath rebuild() {
		getPath();
		return this;
	}

	public final BoundPath append(Step step) {
		return getRawPath().append(step).bind(this, getOrigin());
	}

	public final BoundPath append(MemberKey memberKey) {
		return getRawPath().append(memberKey).bind(this, getOrigin());
	}

	public final BoundPath append(PathFragment fragment) {
		return getRawPath().append(fragment).bind(this, getOrigin());
	}

	public final BoundPath append(BoundFragment fragment) {
		return fragment.appendTo(this);
	}

	public final BoundPath dereference() {
		return getRawPath().dereference().bind(this, getOrigin());
	}

	public final BoundPath newObject(ObjectConstructor constructor) {
		return getRawPath().newObject(constructor).bind(this, getOrigin());
	}

	public final BoundPath append(Path path) {
		return getRawPath().append(path).bind(this, getOrigin());
	}

	public final BoundPath append(BoundPath path) {
		return append(path.getRawPath());
	}

	public final BoundPath modifyPath(PathModifier modifier) {

		final BoundPath newPath = modifier.modifyPath(this);

		if (newPath == null) {
			return null;
		}
		return newPath;
	}

	public final BoundPath prefixWith(PrefixPath prefix) {

		final Path oldPath = getRawPath();
		final Path newPath = oldPath.prefixWith(prefix);

		if (oldPath == newPath) {
			if (prefix.getStart().is(getOrigin())) {
				return this;
			}
		}

		return newPath.bind(this, prefix.getStart());
	}

	public final BoundPath upgradeScope(Scope toScope) {
		if (toScope.is(getOrigin())) {
			return this;
		}
		return getRawPath().bind(this, toScope);
	}

	public final BoundPath rebuildIn(Scope scope) {
		return upgradeScope(scope).getPath().removeTemplate().bind(this, scope);
	}

	public final BoundPath cut(int stepsToCut) {
		return getRawPath().cut(stepsToCut).bind(this, this.origin);
	}

	public final PathResolution resolve(PathResolver resolver) {
		return walk(resolver, DUMMY_PATH_WALKER);
	}

	public final PathResolution walk(PathResolver resolver, PathWalker walker) {

		final Path path = getPath();
		final Path template = path.getTemplate();

		return walkPath(
				template != null ? template : path,
				resolver,
				walker,
				template != null /* Only template may contain
				                    unresolved fragments      */);
	}

	public final Ref target(Distributor distributor) {
		return target(this, distributor);
	}

	public final Ref target(LocationInfo location, Distributor distributor) {
		return new Ref(location, distributor, this);
	}

	public final TypeRef typeRef(Distributor distributor) {
		return target(distributor).toTypeRef();
	}

	public final TypeRef typeRef(
			Distributor distributor,
			TypeRefParameters typeParameters) {
		return target(distributor).toTypeRef(typeParameters);
	}

	public final StaticTypeRef staticTypeRef(Distributor distributor) {
		return target(distributor).toStaticTypeRef();
	}

	public final StaticTypeRef staticTypeRef(
			Distributor distributor,
			TypeRefParameters typeParameters) {
		return target(distributor).toStaticTypeRef(typeParameters);
	}

	public final NormalPath normalize(Normalizer normalizer, Scope origin) {
		origin.assertDerivedFrom(getOrigin());

		final PathNormalizer pathNormalizer =
				pathNormalizer(normalizer, origin, this);

		if (pathNormalizer == null) {
			return new UnNormalizedPath(this);
		}

		return pathNormalizer.normalize();
	}

	public final void normalizePath(Analyzer analyzer) {
		for (Step step : getSteps()) {
			step.normalizeStep(analyzer);
		}
	}

	public final PathReproducer reproducer(Reproducer reproducer) {
		return new PathReproducer(reproducer, this);
	}

	public final PrefixPath toPrefix(Scope start) {
		start.assertCompatible(start);
		if (start.is(getOrigin())) {
			return new PrefixPath(start, getRawPath(), this);
		}
		return getRawPath().toPrefix(start);
	}

	public final BoundPath toStatic() {
		if (getRawPath().isStatic()) {
			return this;
		}
		return getRawPath().bindStatically(this, this.origin);
	}

	public final PathOp op(CodeDirs dirs, HostOp start) {
		if (isStatic()) {
			return staticOp(dirs, start);
		}

		final Step[] steps = getSteps();
		PathOp found = hostPathOp(this, start, start);

		for (int i = 0; i < steps.length; ++i) {
			found = steps[i].op(found);
			if (found == null) {
				throw new IllegalStateException(toString(i + 1) + " not found");
			}
		}

		return found;
	}

	@Override
	public String toString() {
		return toString(0);
	}

	@Override
	protected TypeRef ancestor(LocationInfo location, Ref ref) {

		final Step[] steps = getRawSteps();

		if (steps.length == 0) {
		}

		final Step lastStep = lastRawStep();

		if (lastStep == null) {
			return ANCESTOR_FRAGMENT.toPath()
					.bind(location, ref.getScope())
					.typeRef(ref.distribute());
		}

		return lastStep.ancestor(location, ref);
	}

	@Override
	protected TypeRef iface(Ref ref, boolean rebuilt) {

		final Step lastStep;

		if (rebuilt) {
			lastStep = lastStep();
		} else {
			// It is essential to use the last unresolved step,
			// as it can be an unexpanded path fragment,
			// which field definition can perform an additional tasks.
			// One example is phrase.
			lastStep = lastRawStep();
		}

		if (lastStep == null) {
			return ref.toTypeRef();
		}

		return lastStep.iface(ref);
	}

	public String toString(int length) {
		if (this.rawPath == null) {
			return super.toString();
		}
		return getRawPath().toString(this.origin, length);
	}

	@Override
	protected Statement toCondition(Ref condition, Statements<?, ?> statements) {

		final Step lastStep = lastRawStep();

		if (lastStep == null) {
			return new RefCondition(condition);
		}

		return lastStep.condition(condition, statements);
	}

	@Override
	protected Statement toValue(
			LocationInfo location,
			Ref value,
			Statements<?, ?> statements) {

		final Step lastStep = lastRawStep();

		if (lastStep == null) {
			return value;
		}

		return lastStep.value(location, value, statements);
	}

	@Override
	protected FieldDefinition toFieldDefinition(Ref ref, boolean rebuilt) {

		final Step lastStep;

		if (rebuilt) {
			lastStep = lastStep();
		} else {
			// It is essential to use the last unresolved step,
			// as it can be an unexpanded path fragment,
			// which field definition can perform additional tasks.
			// One example is phrase.
			lastStep = lastRawStep();
		}

		if (lastStep == null) {
			return new PathFieldDefinition(ref);
		}

		return lastStep.fieldDefinition(ref);
	}

	@Override
	protected Ref consume(Ref ref, Consumer consumer) {

		final Step lastStep = lastRawStep();

		if (lastStep == null) {
			return ref;
		}

		final Ref consumption = lastStep.consume(ref, consumer);

		if (consumption != null) {
			consumption.assertSameScope(ref);
		}

		return consumption;
	}

	final PathNormalizationDoubt doubt(Analyzer analyzer) {
		this.doubt.addTo(analyzer);
		return this.doubt;
	}

	final Scope root() {
		return getOrigin().getContext().getRoot().getScope();
	}

	final int startIndex() {
		if (this.startObject == null) {
			findStart();
		}
		return this.startIndex;
	}

	final Scope startObjectScope() {
		if (this.startObjectScope == null) {
			findStart();
		}
		return this.startObjectScope;
	}

	final Obj startObject() {
		if (this.startObject == null) {
			findStart();
		}
		return this.startObject;
	}

	private final Step[] getRawSteps() {
		return getRawPath().getSteps();
	}

	private final Step lastRawStep() {
		return getRawPath().lastStep();
	}

	private PathResolution walkPath(
			Path path,
			PathResolver originalResolver,
			PathWalker walker,
			boolean expand) {
		assert !expand || path.isTemplate() || path.getTemplate() == null :
			"The template should be expanded instead of templated path";
		this.path = path;

		final PathResolver resolver =
				this.doubt.wrapResolutionUser(originalResolver);
		final Scope start;
		final PathTracker tracker;

		if (isAbsolute()) {
			start = root();
			if (expand) {
				tracker = new PathRecorder(
						this,
						resolver,
						walker,
						start,
						true);
			} else if (resolver.toUser().isDummy()) {
				tracker = new SimplePathTracker(this, resolver, walker);
			} else {
				tracker = new StaticPathTracker(
						this,
						resolver,
						walker,
						startIndex());
			}
			if (!tracker.root(this, start)) {
				return noResolution(tracker, null, null);
			}
		} else {
			start = resolver.getPathStart();
			start.assertDerivedFrom(getOrigin());
			if (expand) {
				tracker = new PathRecorder(
						this,
						resolver,
						walker,
						start,
						false);
			} else if (!isStatic() || resolver.toUser().isDummy()) {
				tracker = new SimplePathTracker(this, resolver, walker);
			} else {
				tracker = new StaticPathTracker(
						this,
						resolver,
						walker,
						startIndex());
			}
			if (!tracker.start(this, start)) {
				return noResolution(tracker, null, null);
			}
		}

		return walkFrom(start, tracker);
	}

	private PathResolution walkFrom(Scope start, PathTracker tracker) {

		final StepResolver resolver = new StepResolver(tracker);
		Step[] steps = this.path.getSteps();
		Container result = start.getContainer();
		Scope prev = start;
		int i = 0;
		boolean templateReached = false;

		while (i < steps.length) {

			final Step step = steps[i];
			final AbstractPathFragment fragment = step.getPathFragment();

			if (fragment != null) {
				// Build path fragment and replace current step with it.

				final Path replacement = fragment.expand(tracker, i, prev);

				if (replacement == null) {
					// Error occurred.
					steps = ArrayUtil.replace(
							steps,
							i,
							steps.length,
							new Step[] {ErrorStep.ERROR_STEP});
					this.path = new Path(
							this.path.getKind(),
							this.path.isStatic(),
							this.path.getTemplate(),
							steps);
					tracker.abortedAt(prev, step);
					return noResolution(tracker, null, null);
				}

				final Path template;

				if (fragment.isTemplate()) {
					templateReached = true;
					// Do not expand the template fragment
					// for the template path.
					template = this.path.getTemplate();
				} else if (templateReached) {
					// Template expansion resulted to another path fragment.
					// The template should remain intact.
					template = this.path.getTemplate();
				} else {
					template = replacementTemplate(steps, i, replacement);
				}

				final Step[] replacementSteps = replacement.getSteps();

				if (replacement.isAbsolute()) {
					// Replacement is an absolute path.
					// Replace all steps from the very first to the current one.
					steps = ArrayUtil.replace(
							steps,
							0,
							i + 1,
							replacementSteps);
					this.path = new Path(
							PathKind.ABSOLUTE_PATH,
							true,
							template,
							steps);
					// Continue from the ROOT.
					prev = root();
					i = 0;
					if (!tracker.pathTrimmed(this, prev)) {
						return noResolution(tracker, prev, step);
					}
				} else {
					// Replacement is a relative path.
					// Replace the current step.
					steps = ArrayUtil.replace(
							steps,
							i,
							i + 1,
							replacementSteps);
					this.path = new Path(
							this.path.getKind(),
							this.path.isStatic() || replacement.isStatic(),
							template,
							steps);
				}
				// Do not change the current index.
				continue;
			}
			result = resolver.resolveStep(step, prev, i);
			if (tracker.isAborted()) {
				return noResolution(tracker, prev, step);
			}
			if (result == null) {
				tracker.abortedAt(prev, step);
				return pathResolutionError(this);
			}
			++i;
			prev = result.getScope();
		}

		if (!tracker.done(result)) {
			return noResolution(tracker, null, null);
		}
		if (result != null) {
			return pathResolution(this, result);
		}

		return pathResolutionError(this);
	}

	private Path replacementTemplate(
			Step[] steps,
			int stepIndex,
			Path replacement) {
		if (this.path.isTemplate()) {
			// The path is template by itself.
			// No additional template needed.
			return null;
		}

		assert this.path.getTemplate() == null :
			"Template lost";

		final Path template = replacement.getTemplate();

		if (template == null) {
			return null;
		}
		if (replacement.isTemplate()) {
			// The replacement is template by itself,
			// so the expanded path will be a template too.
			return null;
		}

		assert stepIndex == steps.length - 1 :
			"Only the last step may expand to template";

		final Step[] replacementSteps = template.getSteps();

		if (template.isAbsolute()) {
			return new Path(
					PathKind.ABSOLUTE_PATH,
					true,
					null,
					replacementSteps);
		}

		final Step[] templateSteps = ArrayUtil.replace(
				steps,
				stepIndex,
				stepIndex + 1,
				replacementSteps);

		return new Path(
				this.path.getKind(),
				this.path.isStatic() || template.isStatic(),
				null,
				templateSteps);
	}

	private PathResolution noResolution(
			PathTracker tracker,
			Scope last,
			Step brokenStep) {
		if (tracker.isError()) {
			if (brokenStep != null) {
				tracker.abortedAt(last, brokenStep);
			}
			return pathResolutionError(this);
		}
		return noPathResolutionError(this);
	}

	private void findStart() {

		final StaticPathStartFinder walker = new StaticPathStartFinder();

		walk(pathResolver(getOrigin(), dummyUser()), walker);

		this.startObjectScope = walker.getStartObjectScope();
		this.startObject = walker.getStartObject();
		this.startIndex = walker.getStartIndex();
	}

	private Path rebuildPath() {

		final Path path = getRawPath();
		final Path template = getTemplate();

		removeOddFragments(template != null ? template : path);

		return this.path = combineSteps();
	}

	private void removeOddFragments(Path path) {
		if (path.getSteps().length == 0) {
			this.path = path;
			return;
		}

		final OddPathFragmentRemover remover =
				new OddPathFragmentRemover(path.getSteps().length);

		walkPath(
				path,
				pathResolver(getOrigin(), dummyUser()),
				remover,
				true);

		final Step[] oldSteps = getSteps();
		final Step[] newSteps = remover.removeOddFragments(oldSteps);

		if (oldSteps == newSteps) {
			return;
		}

		this.path = new Path(
				getKind(),
				isStatic(),
				removeOddFragmentsFromTemplate(remover),
				newSteps);
	}

	private Path removeOddFragmentsFromTemplate(
			OddPathFragmentRemover remover) {

		final Path oldTemplate = getPath().getTemplate();

		if (oldTemplate == null) {
			return null;
		}
		// Up to this point the path and its template has the same prefix.
		final Step[] oldTemplateSteps = oldTemplate.getSteps();
		final Step[] newTemplateSteps =
				remover.removeOddFragments(oldTemplateSteps);

		if (oldTemplateSteps == newTemplateSteps) {
			return oldTemplate;
		}

		return new Path(
				oldTemplate.getKind(),
				oldTemplate.isStatic(),
				null,
				newTemplateSteps);
	}

	private Path combineSteps() {

		final Path path = getPath();
		final Step[] steps = path.getSteps();

		if (steps.length <= 1) {
			return path;
		}

		final Step[] combined = new PathRebuilder(this, steps).rebuild();

		if (steps == combined) {
			return path;
		}

		return new Path(
				getKind(),
				isStatic(),
				combineTemplateSteps(path, combined),
				combined);
	}

	private static Path combineTemplateSteps(Path path, Step[] combined) {
		if (combined.length == 0) {
			// Rebuilt path is empty.
			// Leave the template intact.
			return path.getTemplate();
		}

		final AbstractPathFragment lastFragment =
				combined[combined.length - 1].getPathFragment();

		if (lastFragment != null && lastFragment.isTemplate()) {
			// Rebuilt path is template.
			return null;
		}

		// Otherwise, leave the template intact.
		return path.getTemplate();
	}

	private final PathOp staticOp(CodeDirs dirs, HostOp start) {

		final CodeBuilder builder = dirs.getBuilder();
		final ObjectIR firstObject =
				startObject().ir(dirs.getGenerator());
		PathOp found =
				hostPathOp(this, start, firstObject.op(builder, dirs.code()));
		final Step[] steps = getSteps();

		for (int i = startIndex(); i < steps.length; ++i) {
			found = steps[i].op(found);
			if (found == null) {
				throw new IllegalStateException(toString(i + 1) + " not found");
			}
		}

		return found;
	}

}
