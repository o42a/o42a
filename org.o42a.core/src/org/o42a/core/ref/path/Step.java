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
package org.o42a.core.ref.path;

import static org.o42a.core.ref.path.impl.AncestorFragment.ANCESTOR_FRAGMENT;

import org.o42a.analysis.Analyzer;
import org.o42a.core.Container;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.op.PathOp;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.ref.Consumer;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.RefUsage;
import org.o42a.core.ref.impl.cond.RefCondition;
import org.o42a.core.ref.path.impl.PathFieldDefinition;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Statement;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.ValueAdapter;
import org.o42a.core.value.ValueRequest;


public abstract class Step {

	public abstract PathKind getPathKind();

	public abstract RefUsage getObjectUsage();

	public ValueAdapter valueAdapter(Ref ref, ValueRequest request) {
		return ref.valueStruct(ref.getScope()).valueAdapter(ref, request);
	}

	public Path toPath() {
		return new Path(getPathKind(), false, null, this);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	protected AbstractPathFragment getPathFragment() {
		return null;
	}

	/**
	 * Converts a reference to condition statement.
	 *
	 * <p>This method is called for the last step of the reference path when
	 * the reference is used as an {@link Statements#expression(Ref) expression
	 * statement}.</p>
	 *
	 * <p>By default the logical value of {@code condition} is used as condition.
	 * </p>
	 *
	 * @param condition the reference to convert to condition statement.
	 * @param statements the statements this reference is added to.
	 *
	 * @return the conditional statement.
	 */
	protected Statement condition(Ref condition, Statements<?, ?> statements) {
		return new RefCondition(condition);
	}

	/**
	 * Converts a reference to value statement.
	 *
	 * <p>This method is called for the last step of the reference path when
	 * the reference is used as a {@link Statements#selfAssign(Ref)
	 * self-assignment statement}.</p>
	 *
	 * <p>By default this method returns the reference itself.</p>
	 *
	 * @param location a self-assignment statement location.
	 * @param value the reference to convert to value statement.
	 * @param statements the statements this reference is added to.
	 *
	 * @return the value statement.
	 */
	protected Statement value(
			LocationInfo location,
			Ref value,
			Statements<?, ?> statements) {
		return value;
	}

	protected abstract FieldDefinition fieldDefinition(Ref ref);

	/**
	 * This is ivoked by {@link Ref#consume(Consumer)} for the last step
	 * of the path to optionally {@link Consumer consume} the reference.
	 *
	 * <p>This method's call is optional. But it is required e.g. for macro
	 * expansion. It is a step's responsibility to report the required call
	 * is missing.</p>
	 *
	 * @param ref the reference this step belongs to.
	 * @param consumer the path consumer.
	 *
	 * @return the reference consumption result or the {@code ref} itself if
	 * consumption is not required.
	 */
	protected Ref consume(Ref ref, Consumer consumer) {
		return ref;
	}

	/**
	 * Rebuilds the path.
	 *
	 * <p>This method is called during the bound path rebuild and may replace
	 * the previous step or path fragment.</p>
	 *
	 * @param rebuilder path rebuilder.
	 */
	protected void rebuild(PathRebuilder rebuilder) {
	}

	/**
	 * Combines the step with the next one.
	 *
	 * <p>This method is called during the bound path rebuild after
	 * {@link #rebuild(PathRebuilder) rebuild attempt} of the next step, which
	 * didn't change the path. The combining may replace the current step
	 * or path fragment.</p>
	 *
	 * @param rebuilder path rebuilder.
	 * @param next the next step to combine this one with.
	 */
	protected void combineWith(PathRebuilder rebuilder, Step next) {
	}

	/**
	 * Builds an ancestor of an object pointed by the path.
	 *
	 * <p>This method is called on the last step of the path.</p>
	 *
	 * @param path the bound path to build an ancestor for.
	 * @param location the location of ancestor expression.
	 * @param distributor the constructing ancestor's distributor.
	 *
	 * @return an ancestor type reference.
	 */
	protected TypeRef ancestor(
			BoundPath path,
			LocationInfo location,
			Distributor distributor) {
		return path.append(ANCESTOR_FRAGMENT).typeRef(distributor);
	}

	protected abstract Container resolve(StepResolver resolver);

	protected abstract void normalize(PathNormalizer normalizer);

	protected abstract void normalizeStatic(PathNormalizer normalizer);

	/**
	 * Return the non-normalized path remainder.
	 *
	 * <p>This method is called when the  {@link PathNormalizer#up(Scope, Path,
	 * org.o42a.core.ref.ReversePath)} call leads out of the
	 * {@link PathNormalizer#getOrigin() normalized path origin}.</p>
	 *
	 * @param normalizer path normalizer.
	 *
	 * @return path remainder.
	 */
	protected Path nonNormalizedRemainder(PathNormalizer normalizer) {
		return Path.SELF_PATH;
	}

	/**
	 * Normalize the step.
	 *
	 * <p>This is called during the path normalization when its inlining failed.
	 * </p>
	 *
	 * @param analyzer analyzer.
	 */
	protected void normalizeStep(Analyzer analyzer) {
	}

	protected abstract PathReproduction reproduce(
			LocationInfo location,
			PathReproducer reproducer);

	protected final FieldDefinition defaultFieldDefinition(Ref ref) {
		return new PathFieldDefinition(ref);
	}

	protected abstract PathOp op(PathOp start);

}
