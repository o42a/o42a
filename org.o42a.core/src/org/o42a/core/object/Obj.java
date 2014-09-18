/*
    Compiler Core
    Copyright (C) 2010-2014 Ruslan Lopatin

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
package org.o42a.core.object;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.codegen.Codegen.irInit;
import static org.o42a.core.AbstractContainer.matchingPathOf;
import static org.o42a.core.member.MemberId.OWNER_FIELD_ID;
import static org.o42a.core.member.clause.Clause.validateImplicitSubClauses;
import static org.o42a.core.object.ObjectContent.objectContent;
import static org.o42a.core.object.impl.ObjectResolution.MEMBERS_RESOLVED;
import static org.o42a.core.object.impl.ObjectResolution.RESOLVING_MEMBERS;
import static org.o42a.core.object.impl.OverrideRequirement.abstractsAllowedIn;
import static org.o42a.core.object.impl.OwnerField.ownerFieldFor;
import static org.o42a.core.object.impl.OwnerField.reusedOwnerPath;
import static org.o42a.core.object.value.Statefulness.DERIVED_EAGER;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.Path.staticPath;
import static org.o42a.core.value.TypeParameters.typeParameters;
import static org.o42a.util.fn.Init.init;
import static org.o42a.util.fn.NullableInit.nullableInit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.ClauseContainer;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldUses;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.type.impl.DeclaredMemberTypeParameter;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.impl.*;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.type.Sample;
import org.o42a.core.object.value.Statefulness;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.impl.StaticObjectStep;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.CommandEnv;
import org.o42a.core.st.impl.ObjectEnv;
import org.o42a.core.value.TypeParameter;
import org.o42a.core.value.TypeParameters;
import org.o42a.core.value.ValueType;
import org.o42a.core.value.link.Link;
import org.o42a.util.ArrayUtil;
import org.o42a.util.fn.CondInit;
import org.o42a.util.fn.Init;
import org.o42a.util.fn.NullableInit;


public abstract class Obj
		extends Contained
		implements MemberContainer, ClauseContainer {

	private final Obj propagatedFrom;

	private final Init<Meta> meta = init(() -> new Meta(this));
	private final Init<ObjectType> type = init(() -> new ObjectType(this));
	private final Init<ObjectValue> value = init(() -> new ObjectValue(this));
	private final Init<Deps> deps = init(() -> new Deps(this));

	private final Init<ObjectContent> content =
			init(() -> objectContent(this, false));
	private final Init<ObjectContent> clonesContent =
			init(() -> objectContent(this, true));
	private final Init<Ref> selfRef = init(
			() -> SELF_PATH
			.bindStatically(this, getScope())
			.target(distribute()));
	private final NullableInit<Obj> cloneOf = nullableInit(this::findCloneOf);
	private byte fullResolution;

	private final Init<OwnerPath> ownerPath = init(this::createOwnerPath);
	private final Init<Obj> wrapped = init(this::findWrapped);

	private ObjectMembers objectMembers;
	private final HashMap<MemberKey, Member> members = new HashMap<>();
	private final HashMap<MemberId, Symbol> symbols = new HashMap<>();

	private final Init<MemberClause[]> explicitClauses =
			init(this::findExplicitClauses);
	private final Init<MemberClause[]> implicitClauses =
			init(this::findImplicitClauses);

	private final Init<FieldUses> fieldUses = init(() -> new FieldUses(this));

	private final CondInit<Generator, ObjectIR> ir = irInit(this::createIR);

	public Obj(Scope scope) {
		super(scope, new ObjectDistributor(scope, scope));
		this.propagatedFrom = null;
	}

	protected Obj(ObjectScope scope) {
		super(scope, new ObjectDistributor(scope, scope));
		this.propagatedFrom = null;
		scope.setScopeObject(this);
	}

	protected Obj(Scope scope, Obj sample) {
		super(scope, new ObjectDistributor(scope, sample));
		this.propagatedFrom = sample;
	}

	public Obj(LocationInfo location, Distributor enclosing) {
		this(new ObjScope(location, enclosing));
	}

	protected Obj(ObjectScope scope, Obj sample) {
		super(scope, new ObjectDistributor(scope, sample));
		this.propagatedFrom = sample;
		scope.setScopeObject(this);
	}

	protected Obj(Distributor enclosing, Obj sample) {
		this(new ObjScope(sample, enclosing), sample);
	}

	public final boolean isNone() {
		return is(getContext().getNone());
	}

	@Override
	public Obj getContainer() {
		return this;
	}

	public final boolean isPrototype() {

		final Field field = getScope().toField();

		return field != null && field.isPrototype();
	}

	public final boolean isStatic() {

		final Field field = getScope().toField();

		if (field != null) {
			return field.isStatic();
		}

		final Scope enclosingScope = getScope().getEnclosingScope();

		return enclosingScope == null || enclosingScope.isTopScope();
	}

	public final boolean isAbstract() {

		final Field field = getScope().toField();

		return field != null && field.isAbstract();
	}

	public boolean isValid() {
		return true;
	}

	public Link getDereferencedLink() {
		return null;
	}

	/**
	 * Returns an interface for this object.
	 *
	 * <p>The object interface is a closest object, which can be constructed by
	 * compiler. Usually, it is an object itself, even if it is
	 * {@link ConstructionMode#RUNTIME_CONSTRUCTION run time constructed}.
	 * The only exception is a run time constructed link target. In this case
	 * a link interface is returned.<p>
	 *
	 * @return object interface.
	 */
	public final Obj getInterface() {
		if (!getConstructionMode().isRuntime()
				|| getConstructionMode().isPredefined()) {
			return this;
		}

		final Link link = getDereferencedLink();

		if (link != null) {
			return link.getInterfaceRef().getType();
		}

		return this;
	}

	public Obj getPropagatedFrom() {
		return this.propagatedFrom;
	}

	public final boolean isClone() {
		return getCloneOf() != null;
	}

	public boolean isPropagated() {
		return getPropagatedFrom() != null;
	}

	public final Obj getCloneOf() {
		return this.cloneOf.get();
	}

	public ConstructionMode getConstructionMode() {
		return type().getAscendants().getConstructionMode();
	}

	@Override
	public Member toMember() {

		final Field field = getScope().toField();

		return field != null ? field.toMember() : null;
	}

	@Override
	public Obj toObject() {
		return this;
	}

	@Override
	public Clause toClause() {
		return null;
	}

	@Override
	public final Namespace toNamespace() {
		return null;
	}

	public final boolean isWrapper() {
		return getWrapped() != this;
	}

	public final Obj getWrapped() {
		return this.wrapped.get();
	}

	public final Obj mostWrapped() {

		final Obj wrapped = getWrapped();

		if (wrapped == this) {
			return this;
		}

		return wrapped.mostWrapped();
	}

	public final Meta meta() {
		return this.meta.get();
	}

	public final ObjectType type() {
		return this.type.get();
	}

	public final ObjectValue value() {
		return this.value.get();
	}

	public final boolean hasDeps(Analyzer analyzer) {
		return this.deps.isInitialized() && deps().hasDeps(analyzer);
	}

	public final Deps deps() {
		return this.deps.get();
	}

	public final boolean membersResolved() {
		return type().getResolution().membersResolved();
	}

	@Override
	public Collection<? extends Member> getMembers() {
		resolveMembers(true);
		return this.members.values();
	}

	public final MemberClause[] getExplicitClauses() {
		return this.explicitClauses.get();
	}

	@Override
	public boolean hasSubClauses() {
		if (getExplicitClauses().length != 0) {
			return true;
		}

		final TypeRef ancestor = type().getAncestor();

		if (ancestor != null) {
			return ancestor.getType().hasSubClauses();
		}

		final Sample sample = type().getSample();

		if (sample != null) {
			if (sample.getObject().hasSubClauses()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public MemberClause[] getImplicitClauses() {
		return this.implicitClauses.get();
	}

	@Override
	public Container getEnclosingContainer() {
		return getScope().getEnclosingContainer();
	}

	@Override
	public final Member member(MemberKey memberKey) {
		resolveMembers(memberKey.getMemberId().containsAdapterId());
		return members().get(memberKey);
	}

	public final Member member(MemberId memberId) {
		return objectMember(Accessor.PUBLIC, memberId, null);
	}

	public final Member member(MemberId memberId, Accessor accessor) {
		return objectMember(accessor, memberId, null);
	}

	public final Member objectMember(
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {
		resolveMembers(memberId.containsAdapterId());

		final Symbol found = this.symbols.get(memberId);

		if (found == null) {
			return null;
		}

		return found.member(declaredIn, accessor);
	}

	public final boolean is(Obj object) {
		return getScope().is(object.getScope());
	}

	public final boolean cloneOf(Obj other) {
		if (is(other)) {
			return true;
		}

		final Obj cloneOf = getCloneOf();

		if (cloneOf == null) {
			return false;
		}

		return cloneOf.cloneOf(other);
	}

	public void resolveMembers(boolean resolveAdapters) {
		if (this.objectMembers != null) {
			// Register members incrementally.
			updateMembers();
			this.objectMembers.registerMembers(resolveAdapters);
			return;
		}

		final ObjectType objectType = type();

		if (!objectType.getResolution().membersResolved()) {
			if (!resolveIfNotResolving()) {
				return;
			}

			final ObjectResolution resolution = objectType.getResolution();

			if (resolution.typeResolved() && !resolution.membersResolved()) {
				// resolution is not in progress - resolve members
				// otherwise members are empty
				objectType.setResolution(RESOLVING_MEMBERS);
				try {
					declareMembers();
					this.objectMembers.registerMembers(resolveAdapters);
				} finally {
					objectType.setResolution(resolution);
				}
				objectType.setResolution(MEMBERS_RESOLVED);
				updateMembers();
				this.objectMembers.registerMembers(resolveAdapters);
			}
		}
	}

	@Override
	public final MemberPath member(
			Access access,
			MemberId memberId,
			Obj declaredIn) {

		final Member found =
				objectMember(access.getAccessor(), memberId, declaredIn);

		return found != null ? found.getMemberPath() : null;
	}

	@Override
	public MemberClause clause(MemberId memberId, Obj declaredIn) {

		// Clauses are always accessible to public.
		final Member found =
				objectMember(Accessor.PUBLIC, memberId, declaredIn);

		return found != null ? found.toClause() : null;
	}

	@Override
	public MemberPath matchingPath(MemberId memberId, Obj declaredIn) {
		return matchingPathOf(this, memberId, declaredIn);
	}

	@Override
	public final MemberPath findMember(
			Access access,
			MemberId memberId,
			Obj declaredIn) {
		return member(access, memberId, declaredIn);
	}

	public Definitions overrideDefinitions(Definitions ascendantDefinitions) {

		final Definitions explicitDefinitions =
				value()
				.getExplicitDefinitions()
				.upgradeScope(ascendantDefinitions.getScope());

		return ascendantDefinitions.override(explicitDefinitions);
	}

	public final OwnerPath ownerPath() {
		return this.ownerPath.get();
	}

	public final Ref staticRef(Scope scope) {

		final BoundPath path =
				new StaticObjectStep(this).toPath().bindStatically(this, scope);

		return path.target(distributeIn(scope.getContainer()));
	}

	public final FieldUses fieldUses() {
		return this.fieldUses.get();
	}

	public final Ref selfRef() {
		return this.selfRef.get();
	}

	public final ObjectContent content() {
		return this.content.get();
	}

	public final ObjectContent clonesContent() {
		return this.clonesContent.get();
	}

	public final CommandEnv definitionEnv() {
		return new ObjectEnv(this);
	}

	public final void resolveAll() {
		if (this.fullResolution != 0) {
			return;
		}
		this.fullResolution = 1;
		getContext().fullResolution().start();
		try {
			fullyResolve();
		} finally {
			getContext().fullResolution().end();
		}
	}

	public final void normalize(Analyzer analyzer) {
		if (this.fullResolution >= 2) {
			return;
		}
		this.fullResolution = 2;
		normalizeObject(analyzer);
	}

	public final ObjectIR ir(Generator generator) {
		return this.ir.get(generator);
	}

	public final boolean assertFullyResolved() {
		assert this.fullResolution > 0
			|| (!meta().isUpdated() && getCloneOf().fullResolution > 0):
				this + " is not fully resolved";
		return true;
	}

	public final boolean assertDerivedFrom(Obj type) {
		assert is(type) || type().derivedFrom(type.type()) :
			this + " is not derived from " + type;
		return true;
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return scope.toString();
	}

	protected OwnerPath createOwnerPath() {
		if (isStatic()) {
			return new OwnerPath() {
				@Override
				public MemberKey ownerFieldKey() {
					return null;
				}
				@Override
				public Path toPath() {
					return staticPath(
							getScope(),
							getScope().getEnclosingScope());
				}
			};
		}

		final OwnerPath reused = reusedOwnerPath(this);

		if (reused != null) {
			return reused;
		}

		// New owner field is to be created.
		return new OwnerStep(this, OWNER_FIELD_ID.key(getScope()));
	}

	protected abstract Nesting createNesting();

	protected Obj findWrapped() {

		final Field field = getScope().toField();

		if (field == null) {
			return this;
		}

		final Obj enclosingObject = getScope().getEnclosingScope().toObject();

		if (enclosingObject == null) {
			return this;
		}

		final Obj enclosingWrapped = enclosingObject.getWrapped();

		if (enclosingWrapped.is(enclosingObject)) {
			return this;
		}

		return enclosingWrapped.member(field.getKey())
				.toField()
				.object(dummyUser());
	}

	protected final void resolve() {
		type().resolve(false);
	}

	protected final boolean resolveIfNotResolving() {
		return type().resolve(true);
	}

	protected void postResolve() {
	}

	protected abstract Ascendants buildAscendants();

	protected final void setValueType(ValueType<?> valueType) {
		type().setKnownValueType(valueType);
	}

	protected abstract void declareMembers(ObjectMembers members);

	protected void updateMembers() {
	}

	protected TypeParameters<?> determineTypeParameters() {

		final TypeRef ancestor = type().getAncestor();

		if (ancestor == null) {
			return typeParameters(this, ValueType.VOID);
		}

		final TypeParameters<?> derivedTypeParameters =
				type().derivedParameters();
		final ValueType<?> knownValueType = type().getKnownValueType();
		final TypeParameters<?> typeParameters;

		if (knownValueType != null) {
			typeParameters =
					typeParameters(this, knownValueType)
					.refine(derivedTypeParameters);
		} else {
			typeParameters = derivedTypeParameters;
		}

		return typeParameters;
	}

	protected Statefulness determineStatefulness() {

		final Sample sample = type().getSample();

		if (sample != null) {

			final Statefulness sampleStatefulness =
					sample.getObject().value().getStatefulness();

			if (sampleStatefulness.isEager()) {
				checkEagerOverride();
				return isPropagated() ? sampleStatefulness : DERIVED_EAGER;
			}
		} else {

			final TypeRef ancestor = type().getAncestor();

			if (ancestor != null) {

				final Statefulness ancestorStatefulness =
						ancestor.getType().value().getStatefulness();

				if (ancestorStatefulness.isEager()) {
					checkEagerOverride();
					return DERIVED_EAGER;
				}
			}
		}

		return type().getValueType().getDefaultStatefulness();
	}

	protected abstract Definitions explicitDefinitions();

	protected Obj findCloneOf() {
		if (!isPropagated()) {
			assert !getScope().isClone() :
				this + " is not propagated, but scope "
				+ getScope() + " is clone";
			return null;
		}

		final Sample sample = type().getSample();

		assert sample != null :
			"Propagated object has no sample: " + this;

		final Obj sampleObject = sample.getObject();

		if (sampleObject == null) {
			return null;
		}

		final Obj cloneOf;
		final Obj sampleCloneOf = sampleObject.getCloneOf();

		if (sampleCloneOf != null) {
			cloneOf = sampleCloneOf;
		} else {
			cloneOf = sampleObject;
		}

		return cloneOf;
	}

	protected void fullyResolve() {

		final Obj wrapped = getWrapped();

		if (!wrapped.is(this)) {
			wrapped.type().wrapBy(type());
			wrapped.resolveAll();
		}
		type().resolveAll();
		if (isClone()) {
			resolveUpdatedFields();
			return;
		}
		resolveAllMembers();
		validateImplicitSubClauses(getExplicitClauses());
		value().resolveAll(dummyUser());
	}

	protected void fullyResolveDefinitions() {

		final Obj wrapped = getWrapped();

		if (wrapped != this) {
			wrapped.value().wrapBy(value());
		}
		value().getDefinitions().resolveAll();
	}

	protected void normalizeObject(Analyzer analyzer) {

		final Obj wrapped = getWrapped();

		if (wrapped != this) {
			wrapped.normalize(analyzer);
			return;
		}

		value().normalize(analyzer);
		normalizeFields(analyzer);
	}

	protected ObjectIR createIR(Generator generator) {

		final Obj wrapped = getWrapped();

		if (wrapped != this) {
			return wrapped.ir(generator);
		}

		return new ObjectIR(generator, this);
	}

	final Map<MemberKey, Member> members() {
		return Obj.this.members;
	}

	final Map<MemberId, Symbol> symbols() {
		return Obj.this.symbols;
	}

	private void checkEagerOverride() {
		if (value().getDefinitions().areDerived()) {
			return;
		}
		getLogger().error(
				"prohibited_eager_override",
				value().getDefinitions(),
				"Eagerly evaluated value can not be overridden");
	}

	private void declareMembers() {
		this.objectMembers = new ObjectMembers(this);
		declareOwnerField();
		declareMemberTypeParameters();
		declareMembers(this.objectMembers);

		final ObjectType objectType = type();
		final Sample sample = objectType.getSample();

		if (sample != null) {
			sample.deriveMembers(this.objectMembers);
		}

		final TypeRef ancestor = objectType.getAncestor();

		if (ancestor != null) {
			this.objectMembers.deriveMembers(ancestor.getType());
		}
	}

	private void declareOwnerField() {

		final OwnerField ownerField = ownerFieldFor(this);

		if (ownerField != null) {
			this.objectMembers.addMember(ownerField.toMember());
		}
	}

	private void declareMemberTypeParameters() {
		for (TypeParameter parameter : type().getParameters().all()) {
			if (parameter.getKey().getOrigin().is(getScope())) {
				this.objectMembers.addTypeParameter(
						new DeclaredMemberTypeParameter(parameter, this));
			}
		}
	}

	private MemberClause[] findExplicitClauses() {

		MemberClause[] explicitClauses = new MemberClause[0];
		final Scope origin = getScope();

		for (Member member : getMembers()) {
			if (member.isTypeMember() || member.isAlias()) {
				continue;
			}

			final MemberClause clause = member.toClause();

			if (clause == null) {
				continue;
			}

			final MemberKey key = member.getMemberKey();

			if (key.getOrigin() != origin) {
				continue;
			}
			if (key.getEnclosingKey() != null) {
				continue;
			}

			explicitClauses = ArrayUtil.append(explicitClauses, clause);
		}

		return explicitClauses;
	}

	private MemberClause[] findImplicitClauses() {

		MemberClause[] implicitClauses = new MemberClause[0];

		for (MemberClause clause : getExplicitClauses()) {
			if (clause.isImplicit()) {
				implicitClauses = ArrayUtil.append(implicitClauses, clause);
			}
		}

		final Sample sample = type().getSample();

		if (sample != null) {
			implicitClauses = ArrayUtil.append(
					implicitClauses,
					sample.getObject().getImplicitClauses());
		}

		final TypeRef ancestor = type().getAncestor();

		if (ancestor != null) {
			implicitClauses = ArrayUtil.append(
					implicitClauses,
					ancestor.getType().getImplicitClauses());
		}

		return implicitClauses;
	}

	private void resolveAllMembers() {

		final LinkUses linkUses = type().linkUses();
		final boolean abstractAllowed = abstractsAllowedIn(this);
		OverrideRequirement overrideRequirement = null;

		for (Member member : getMembers()) {

			final MemberField field = member.toField();

			if (field == null) {
				if (member.isClone()) {
					// Only field clones require full resolution.
					continue;
				}
			} else {
				if (linkUses != null) {
					linkUses.fieldChanged(field);
				}
				if (!abstractAllowed && field.isAbstract()) {
					if (overrideRequirement == null) {
						overrideRequirement = new OverrideRequirement(this);
					}
					if (overrideRequirement.overrideRequired(field)) {
						abstractNotOverridden(field);
					}
				}
			}

			member.resolveAll();
		}
	}

	private void resolveUpdatedFields() {
		if (!meta().isUpdated()) {
			// Non-updated object can not contain an updated fields.
			return;
		}

		final LinkUses linkUses = type().linkUses();

		for (Member member : getMembers()) {

			final MemberField field = member.toField();

			if (field == null || field.isUpdated()) {
				continue;
			}
			if (linkUses != null) {
				linkUses.fieldChanged(field);
			}

			field.resolveAll();
		}
	}

	private void abstractNotOverridden(MemberField member) {
		getLogger().error(
				"abstract_not_overridden",
				getLocation(),
				"Abstract field '%s' not overridden",
				member.getDisplayName());
	}

	private void normalizeFields(Analyzer analyzer) {
		for (Member member : getMembers()) {

			final MemberField memberField = member.toField();

			if (memberField == null) {
				continue;
			}
			if (memberField.isPropagated()) {
				continue;
			}

			final Field field = memberField.field(dummyUser());

			if (field.getFieldKind().isOwner()) {
				continue;
			}

			field.toObject().normalize(analyzer);
		}
	}

	private static final class ObjectDistributor extends Distributor {

		private final Scope scope;
		private final LocationInfo location;

		ObjectDistributor(Scope scope, LocationInfo location) {
			this.scope = scope;
			this.location = location;
		}

		@Override
		public Location getLocation() {
			return this.location.getLocation();
		}

		@Override
		public Container getContainer() {
			return null;
		}

		@Override
		public Scope getScope() {
			return this.scope;
		}

	}

}
