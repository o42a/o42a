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
package org.o42a.core.ref;

import static org.o42a.core.ref.path.Path.ROOT_PATH;
import static org.o42a.core.st.DefinitionTarget.valueDefinition;

import org.o42a.codegen.code.Code;
import org.o42a.core.*;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.Directive;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.Def;
import org.o42a.core.def.Definitions;
import org.o42a.core.def.Rescoper;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.GroupClause;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.path.AbsolutePath;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.RefTypeBase;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.st.action.ReturnValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class Ref extends RefTypeBase {

	public static Ref voidRef(LocationInfo location, Distributor distributor) {

		final Field<Obj> wrapperField =
			location.getContext().getIntrinsics().getVoidField();

		return ROOT_PATH.append(
				wrapperField.getKey()).target(location, distributor);
	}

	public static Ref falseRef(LocationInfo location, Distributor distributor) {

		final Obj falseObject = location.getContext().getFalse();
		final AbsolutePath falsePath = ROOT_PATH.append(
			falseObject.getScope().toField().toMember().getKey());

		return falsePath.target(location, distributor);
	}

	public static Ref errorRef(LocationInfo location, Distributor distributor) {
		return new ErrorRef(location, distributor);
	}

	public static Ref runtimeRef(
			LocationInfo location,
			Distributor distributor,
			ValueType<?> valueType) {
		return new RuntimeRef(location, distributor, valueType);
	}

	private Ref expectedTypeAdapter;
	private RefConditionsWrap conditions;
	private Logical logical;
	private RefOp op;
	private Path resolutionRoot;

	public Ref(LocationInfo location, Distributor distributor) {
		this(location, distributor, null);
	}

	Ref(LocationInfo location, Distributor distributor, Logical logical) {
		super(location, distributor);
		this.logical = logical;
	}

	public Path getPath() {
		return null;
	}

	@Override
	public final ValueType<?> getValueType() {
		return getResolution().materialize().getValueType();
	}

	public final Logical getLogical() {
		if (this.logical == null) {
			this.logical = new RefLogical(this);
		}
		return this.logical;
	}

	public final Resolution getResolution() {
		return resolve(getScope());
	}

	/**
	 * Resolution root path.
	 *
	 * <p>This is either upward part of {@link #getPath() path}
	 * (if present) or resolution root of ancestor.</p>
	 *
	 * <p>This is used by {@link TypeRef} to check the type resolution
	 * compatibility.</p>
	 *
	 * @return resolution root path, never <code>null</code>.
	 */
	public Path getResolutionRoot() {
		if (this.resolutionRoot != null) {
			return this.resolutionRoot;
		}
		if (isKnownStatic()) {
			return this.resolutionRoot = Path.ROOT_PATH;
		}

		final Path path = getPath();

		if (path != null) {
			if (path.isAbsolute()) {
				return this.resolutionRoot = Path.ROOT_PATH;
			}

			final ResolutionRootFinder rootFinder =
				new ResolutionRootFinder(this);

			path.walk(this, getScope(), rootFinder);

			return this.resolutionRoot = rootFinder.getRoot();
		}

		if (getResolution().isFalse()) {
			// False resolution is always absolute.
			return this.resolutionRoot = Path.ROOT_PATH;
		}

		final Obj object = getResolution().toObject();

		assert object != null :
			"Non-object reference expected to have a path";

		if (object == getContext().getIntrinsics().getVoid()) {
			// Explicit VOID resolution is always absolute.
			return this.resolutionRoot = Path.ROOT_PATH;
		}

		return this.resolutionRoot =
			object.getAncestor().getRef().getResolutionRoot();
	}

	@Override
	public Action initialValue(LocalScope scope) {
		return new ReturnValue(this, value(scope));
	}

	@Override
	public Action initialLogicalValue(LocalScope scope) {
		return new ExecuteCommand(this, value(scope).getLogicalValue());
	}

	@Override
	public DefinitionTargets getDefinitionTargets() {
		return valueDefinition();
	}

	@Override
	public Conditions setConditions(Conditions conditions) {
		assert this.conditions == null :
			"Conditions already assigned for: " + conditions;
		return this.conditions = new RefConditionsWrap(this, conditions);
	}

	@Override
	public Definitions define(Scope scope) {

		final Def def= expectedTypeAdapter().toDef();
		final Conditions initialConditions =
			getConditions().getInitialConditions();

		return initialConditions.apply(def).toDefinitions();
	}

	public abstract Resolution resolve(Scope scope);

	public final Value<?> getValue() {
		return value(getScope());
	}

	public Value<?> value(Scope scope) {
		return resolve(scope).materialize().getValue();
	}

	/**
	 * Builds ancestor reference.
	 *
	 * <p>This returns ancestor of object or interface of the link. This
	 * shouldn't be called for e.g. arrays.</p>
	 *
	 * <p>If this reference is an object constructor, the ancestor should be
	 * built before object construction.</p>
	 *
	 * @param location the location of caller.
	 *
	 * @return ancestor reference or <code>null</code> if can not be determined.
	 */
	public TypeRef ancestor(LocationInfo location) {
		return new AncestorRef(location, this).toTypeRef();
	}

	public Ref materialize() {

		final Resolution resolution = getResolution();
		final Path materializationPath = resolution.materializationPath();

		if (materializationPath.isSelf()) {
			return this;
		}

		return materializationPath.target(this, distribute(), this);
	}

	@Override
	public abstract Ref reproduce(Reproducer reproducer);

	public final Resolution noResolution() {
		return new Resolution.Error(this);
	}

	public final Resolution containerResolution(Container resolved) {
		if (resolved == null) {
			return noResolution();
		}

		final LocalScope local = resolved.toLocal();

		if (local != null && local == resolved.getScope().toLocal()) {
			return localResolution(local);
		}

		final Clause clause = resolved.toClause();

		if (clause != null) {
			return clauseResolution(clause);
		}

		return artifactResolution(resolved.toArtifact());
	}

	public final Resolution artifactResolution(Artifact<?> resolved) {
		if (resolved == null) {
			return noResolution();
		}

		final Obj object = resolved.toObject();

		if (object != null) {
			return new Resolution.ObjectResolution(object);
		}

		return new Resolution.ArtifactResolution(resolved);
	}

	public final Resolution objectResolution(Obj resolved) {
		if (resolved == null) {
			return noResolution();
		}
		return new Resolution.ObjectResolution(resolved);
	}

	public final Resolution localResolution(LocalScope resolved) {
		if (resolved == null) {
			return noResolution();
		}
		return new Resolution.LocalResolution(resolved);
	}

	public final Resolution clauseResolution(Clause resolved) {
		if (resolved == null) {
			return noResolution();
		}

		final GroupClause group = resolved.toGroupClause();

		if (group != null) {
			return new Resolution.GroupResolution(group);
		}

		return objectResolution(resolved.toPlainClause().getObject());
	}

	public Ref toStatic() {
		if (isKnownStatic()) {
			return this;
		}
		return new StaticRef(this);
	}

	public final Ref adapt(LocationInfo location, StaticTypeRef adapterType) {
		return new Adapter(location, this, adapterType);
	}

	public final Ref rescope(Scope toScope) {
		if (getScope() == toScope) {
			return this;
		}
		return rescope(getScope().rescoperTo(toScope));
	}

	public Ref rescope(Rescoper rescoper) {
		if (rescoper.isTransparent()) {
			return this;
		}
		return new RescopedRef(this, rescoper);
	}

	@Override
	public Instruction toInstruction(Scope scope, boolean assignment) {
		if (assignment) {
			return null;
		}

		final Directive directive = resolve(scope).toDirective();

		if (directive == null) {
			return null;
		}

		return new ApplyDirective(this, directive);
	}

	public Ref and(Logical logical) {
		if (logical.isTrue()) {
			return this;
		}
		return new ConditionalRef(this, logical);
	}

	public TypeRef toTypeRef() {
		if (isKnownStatic()) {
			return toStaticTypeRef();
		}
		return typeRef(this);
	}

	public StaticTypeRef toStaticTypeRef() {
		return staticTypeRef(this);
	}

	public TargetRef toTargetRef(TypeRef typeRef) {
		return createTargetRef(this, typeRef);
	}

	public Rescoper toRescoper() {
		return new RefRescoper(this);
	}

	public FieldDefinition toFieldDefinition() {
		return new ValueFieldDefinition(this);
	}

	public final RefOp op(HostOp host) {

		final RefOp op = this.op;

		if (op != null && op.host() == host) {
			return op;
		}

		return this.op = createOp(host);
	}

	protected boolean isKnownStatic() {
		return false;
	}

	protected abstract RefOp createOp(HostOp host);

	@Override
	protected final StOp createOp(LocalBuilder builder) {
		return new RefStOp(builder, this, op(builder.host()));
	}

	final Ref expectedTypeAdapter() {
		if (this.expectedTypeAdapter != null) {
			return this.expectedTypeAdapter;
		}

		final ValueType<?> expectedType =
			this.conditions.getInitialConditions().getExpectedType();

		if (expectedType == null) {
			return this.expectedTypeAdapter = this;
		}

		return this.expectedTypeAdapter =
			adapt(this, expectedType.typeRef(this, getScope()));
	}

	final RefConditionsWrap getConditions() {
		return this.conditions;
	}

	private static final class RefStOp extends StOp {

		private final RefOp ref;

		RefStOp(
				LocalBuilder builder,
				Statement statement,
				RefOp ref) {
			super(builder, statement);
			this.ref = ref;
		}

		@Override
		public void allocate(LocalBuilder builder, Code code) {
		}

		@Override
		public void writeAssignment(Control control, ValOp result) {
			this.ref.writeValue(control.code(), control.exit(), result);
			control.returnValue();
		}

		@Override
		public void writeLogicalValue(Control control) {
			this.ref.writeLogicalValue(control.code(), control.exit());
		}

	}

}
