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
package org.o42a.core.artifact.object;

import static org.o42a.core.AbstractContainer.parentContainer;
import static org.o42a.core.artifact.object.ObjectResolution.MEMBERS_RESOLVED;
import static org.o42a.core.artifact.object.ObjectResolution.RESOLVING_MEMBERS;
import static org.o42a.core.def.Definitions.emptyDefinitions;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberId.SCOPE_FIELD_ID;
import static org.o42a.core.member.MemberId.fieldName;
import static org.o42a.core.member.clause.Clause.validateImplicitSubClauses;
import static org.o42a.util.use.User.dummyUser;

import java.util.*;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.artifact.Accessor;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.ArtifactKind;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.artifact.object.impl.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.ClauseContainer;
import org.o42a.core.member.clause.MemberClause;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldUses;
import org.o42a.core.member.impl.local.EnclosingOwnerDep;
import org.o42a.core.member.impl.local.RefDep;
import org.o42a.core.member.local.Dep;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.impl.path.AbstractMemberStep;
import org.o42a.core.ref.impl.path.StaticObjectStep;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.StatementEnv;
import org.o42a.core.st.impl.ObjectEnv;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.ArrayUtil;


public abstract class Obj
		extends ObjectArtifact
		implements MemberContainer, ClauseContainer {

	private final OwningObject owningObject = new OwningObject(this);
	private Obj wrapped;
	private ObjectType type;
	private ObjectValue value;
	private int depNameSeq;

	private final HashMap<MemberKey, Member> members =
			new HashMap<MemberKey, Member>();
	private final HashMap<MemberId, Symbol> symbols =
			new HashMap<MemberId, Symbol>();
	private final LinkedHashMap<Object, Dep> deps =
			new LinkedHashMap<Object, Dep>();

	private ObjectMembers objectMembers;
	private MemberClause[] explicitClauses;
	private MemberClause[] implicitClauses;

	private FieldUses fieldUses;

	private ObjectIR ir;

	public Obj(LocationInfo location, Distributor enclosing) {
		this(new ObjScope(location, enclosing));
	}

	public Obj(Scope scope) {
		super(scope);
	}

	protected Obj(ObjectScope scope) {
		super(scope);
	}

	protected Obj(Scope scope, Obj sample) {
		super(scope, sample);
	}

	public Obj(Distributor enclosing, Obj sample) {
		this(new ObjScope(sample, enclosing), sample);
	}

	protected Obj(ObjectScope scope, Obj sample) {
		super(scope, sample);
	}

	@Override
	public final ArtifactKind<Obj> getKind() {
		return ArtifactKind.OBJECT;
	}

	public Artifact<?> getMaterializationOf() {
		return this;
	}

	public ConstructionMode getConstructionMode() {
		return type().getAscendants().getConstructionMode();
	}

	public final OwningObject toMemberOwner() {
		return this.owningObject;
	}

	@Override
	public Member toMember() {

		final Field<?> field = getScope().toField();

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
	public final LocalScope toLocal() {
		return null;
	}

	@Override
	public final Namespace toNamespace() {
		return null;
	}

	@Override
	public final Link toLink() {
		return null;
	}

	@Override
	public final Obj materialize() {
		return this;
	}

	public final boolean isWrapper() {
		return getWrapped() != this;
	}

	public final Obj getWrapped() {
		if (this.wrapped != null) {
			return this.wrapped;
		}
		return this.wrapped = findWrapped();
	}

	public final ObjectType type() {
		if (this.type != null) {
			return this.type;
		}
		return this.type = new ObjectType(this);
	}

	public final ObjectValue value() {
		if (this.value != null) {
			return this.value;
		}
		return this.value = new ObjectValue(this);
	}

	public final boolean membersResolved() {
		return type().getResolution().membersResolved();
	}

	@Override
	public Collection<? extends Member> getMembers() {
		resolveMembers(true);
		return this.members.values();
	}

	public MemberClause[] getExplicitClauses() {
		if (this.explicitClauses != null) {
			return this.explicitClauses;
		}

		MemberClause[] explicitClauses = new MemberClause[0];
		final Scope origin = getScope();

		for (Member member : getMembers()) {

			final MemberClause clause = member.toClause();

			if (clause == null) {
				continue;
			}

			final MemberKey key = member.getKey();

			if (key.getOrigin() != origin) {
				continue;
			}
			if (key.getEnclosingKey() != null) {
				continue;
			}

			explicitClauses = ArrayUtil.append(explicitClauses, clause);
		}

		return this.explicitClauses = explicitClauses;
	}

	@Override
	public boolean hasSubClauses() {
		if (getExplicitClauses().length != 0) {
			return true;
		}

		final TypeRef ancestor = type().getAncestor();

		if (ancestor != null) {
			return ancestor.typeObject(dummyUser()).hasSubClauses();
		}
		for (Sample sample : type().getSamples()) {
			if (sample.typeObject(dummyUser()).hasSubClauses()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public MemberClause[] getImplicitClauses() {
		if (this.implicitClauses != null) {
			return this.implicitClauses;
		}

		MemberClause[] implicitClauses = new MemberClause[0];

		for (MemberClause clause : getExplicitClauses()) {
			if (clause.isImplicit()) {
				implicitClauses = ArrayUtil.append(implicitClauses, clause);
			}
		}

		for (Sample sample : type().getSamples()) {
			implicitClauses = ArrayUtil.append(
					implicitClauses,
					sample.typeObject(dummyUser()).getImplicitClauses());
		}

		final TypeRef ancestor = type().getAncestor();

		if (ancestor != null) {
			implicitClauses = ArrayUtil.append(
					implicitClauses,
					ancestor.typeObject(dummyUser()).getImplicitClauses());
		}

		return this.implicitClauses = implicitClauses;
	}

	public Collection<? extends Dep> getDeps() {
		return this.deps.values();
	}

	@Override
	public Container getEnclosingContainer() {
		return getScope().getEnclosingContainer();
	}

	@Override
	public Container getParentContainer() {
		return parentContainer(this);
	}

	@Override
	public final Member member(MemberKey memberKey) {
		resolveMembers(memberKey.getMemberId().containsAdapterId());
		return members().get(memberKey);
	}

	public final Member field(String name) {
		return objectMember(Accessor.PUBLIC, fieldName(name), null);
	}

	public final Member field(String name, Accessor accessor) {
		return objectMember(accessor, fieldName(name), null);
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

		final Symbol found = memberById(memberId);

		if (found == null) {
			return null;
		}

		return found.member(declaredIn, accessor);
	}

	public final boolean cloneOf(Obj other) {
		if (other == this) {
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
	public final Path member(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {

		final Member found = objectMember(accessor, memberId, declaredIn);

		if (found != null) {
			return found.getKey().toPath();
		}
		if (declaredIn == null) {
			return null;
		}

		final Member adapter = member(adapterId(declaredIn));

		if (adapter == null) {
			return null;
		}

		final Path adapterPath = adapter.getKey().toPath();

		final Artifact<?> adapterArtifact =
				adapter.substance(dummyUser()).toArtifact();
		final Obj adapterObject = adapterArtifact.toObject();

		if (adapterObject != null) {

			final Member foundInAdapterObject =
					adapterObject.objectMember(accessor, memberId, declaredIn);

			if (foundInAdapterObject == null) {
				return null;
			}

			return adapterPath.append(foundInAdapterObject.getKey());
		}

		final TypeRef typeRef = adapterArtifact.getTypeRef();

		if (typeRef != null) {

			final ObjectType type = typeRef.type(dummyUser());

			if (type == null) {
				return null;
			}

			final Member foundInAdapterLink = type.getObject().objectMember(
					accessor,
					memberId,
					declaredIn);

			if (foundInAdapterLink == null) {
				return null;
			}

			return adapterPath.append(foundInAdapterLink.getKey());
		}

		return null;
	}

	@Override
	public MemberClause clause(MemberId memberId, Obj declaredIn) {

		// Clauses are always accessible to public.
		final Member found =
				objectMember(Accessor.PUBLIC, memberId, declaredIn);

		if (found == null) {
			return null;
		}

		return found.toClause();
	}

	@Override
	public final Path findMember(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId, Obj declaredIn) {
		return member(user, accessor, memberId, declaredIn);
	}

	public Path scopePath() {

		final Scope scope = getScope();
		final Step scopePathStep;
		final Container enclosing = scope.getEnclosingContainer();

		if (enclosing.toObject() != null) {

			final Obj propagatedFrom = getPropagatedFrom();

			if (propagatedFrom != null) {
				// Reuse enclosing scope path from object
				// this one is propagated from.
				return propagatedFrom.getScope().getEnclosingScopePath();
			}

			// New scope field is to be created.
			scopePathStep =
					new ParentObjectStep(this, SCOPE_FIELD_ID.key(scope));
		} else {
			// Enclosing local path.
			// Will be replaced with dependency during path rebuild.
			assert enclosing.toLocal() != null :
				"Unsupported kind of enclosing scope " + enclosing;
			scopePathStep = new ParentLocalStep(this);
		}

		return scopePathStep.toPath();
	}

	public final Obj findIn(Scope enclosing) {

		final Scope enclosingScope = getScope().getEnclosingScope();

		if (enclosingScope == enclosing) {
			return this;
		}

		enclosing.assertDerivedFrom(enclosingScope);

		return findObjectIn(enclosing);
	}

	public final Ref staticRef(Scope scope) {

		final BoundPath path =
				new StaticObjectStep(this).toPath().bindStatically(this, scope);

		return path.target(distributeIn(scope.getContainer()));
	}

	@Override
	public final FieldUses fieldUses() {
		if (this.fieldUses != null) {
			return this.fieldUses;
		}
		return this.fieldUses = new FieldUses(this);
	}

	public void pin() {

		final Member member = toMember();

		if (member != null) {
			member.pin(getScope());
		}
	}

	public StatementEnv definitionEnv() {
		return new ObjectEnv(this);
	}

	public final void assertDerivedFrom(Obj type) {
		assert type().derivedFrom(type.type()) :
			this + " is not derived from " + type;
	}

	public final ObjectIR ir(Generator generator) {

		final ObjectIR ir = this.ir;

		if (ir != null && ir.getGenerator() == generator) {
			return ir;
		}

		return this.ir = createIR(generator);
	}

	protected Obj findWrapped() {

		final Field<?> field = getScope().toField();

		if (field == null) {
			return this;
		}

		final Artifact<?> enclosingArtifact =
				getScope().getEnclosingScope().getArtifact();

		if (enclosingArtifact == null) {
			return this;
		}

		final Obj enclosingObject = enclosingArtifact.materialize();
		final Obj enclosingWrapped = enclosingObject.getWrapped();

		if (enclosingWrapped == enclosingObject) {
			return this;
		}

		return enclosingWrapped.member(field.getKey())
				.toField()
				.field(dummyUser())
				.getArtifact()
				.materialize();
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

	protected final void setValueStruct(ValueStruct<?, ?> valueStruct) {
		value().setValueStruct(valueStruct);
	}

	protected abstract void declareMembers(ObjectMembers members);

	protected void updateMembers() {
	}

	protected ValueStruct<?, ?> determineValueStruct() {

		final TypeRef ancestor = type().getAncestor();

		if (ancestor == null) {
			return ValueStruct.VOID;
		}

		final ValueStruct<?, ?> ancestorValueStruct = ancestor.getValueStruct();

		if (!ancestorValueStruct.isScoped()) {
			return ancestorValueStruct;
		}

		final Scope scope = getScope();
		final PrefixPath prefix =
				scope.getEnclosingScopePath().toPrefix(scope);

		return ancestorValueStruct.prefixWith(prefix);
	}

	protected Definitions overrideDefinitions(
			Scope scope,
			Definitions ascendantDefinitions) {

		final Definitions explicitDefinitions =
				value().getExplicitDefinitions().upgradeScope(scope);

		if (ascendantDefinitions == null) {
			return explicitDefinitions;
		}

		return ascendantDefinitions.override(explicitDefinitions);
	}

	protected abstract Definitions explicitDefinitions();

	@Override
	protected Obj findCloneOf() {

		final Artifact<?> materializationOf = getMaterializationOf();

		if (materializationOf != this) {

			final Artifact<?> cloneOfMaterialization =
					materializationOf.getCloneOf();

			if (cloneOfMaterialization == null) {
				return null;
			}

			return cloneOfMaterialization.materialize();
		}

		if (!isPropagated()) {
			assert !getScope().isClone() :
				this + " is not propagated, but scope "
				+ getScope() + " is clone";
			return null;
		}

		final Sample[] samples = type().getSamples();

		assert samples.length > 0 :
			"Propagated object has no samples: " + this;
		assert assertImplicitSamples(samples);

		if (samples.length != 1) {
			return null;
		}

		final Sample sample = samples[0];
		final Obj sampleObject = sample.typeObject(dummyUser());

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

		assert getScope().isClone();

		return cloneOf;
	}

	protected abstract Obj findObjectIn(Scope enclosing);

	@Override
	protected Dep addEnclosingOwnerDep(Obj owner) {
		assert getContext().fullResolution().assertIncomplete();

		final Dep found = this.deps.get(null);

		if (found != null) {
			return found;
		}

		final LocalScope enclosingLocal =
				getScope().getEnclosingContainer().toLocal();

		assert enclosingLocal.getOwner() == owner :
			owner + " is not owner of " + this
			+ " enclosing local scope " + enclosingLocal;

		return addDep(new EnclosingOwnerDep(this));
	}

	@Override
	protected Dep addRefDep(Ref ref) {
		assert getContext().fullResolution().assertIncomplete();

		final int newDepId = this.depNameSeq + 1;
		final RefDep newDep = new RefDep(
				this,
				ref,
				Integer.toString(newDepId));
		final Dep dep = addDep(newDep);

		if (dep == newDep) {
			this.depNameSeq = newDepId;
		}

		return dep;
	}

	@Override
	protected void fullyResolve() {

		final Obj wrapped = getWrapped();

		if (wrapped != this) {
			wrapped.type().wrapBy(type());
			wrapped.resolveAll();
		}
		type().resolveAll();
		if (isClone()) {
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

	Definitions overriddenDefinitions(
			Scope scope,
			Definitions overriddenDefinitions,
			Definitions ancestorDefinitions) {
		if (ancestorDefinitions != null) {
			ancestorDefinitions.assertScopeIs(scope);
		}

		Definitions definitions = overriddenDefinitions;

		if (overriddenDefinitions != null) {
			overriddenDefinitions.assertScopeIs(scope);
			definitions = overriddenDefinitions;
		} else {
			definitions = emptyDefinitions(this, getScope());
		}

		final ObjectType type =
				type().useBy(scope.toObject().value().uses());
		boolean hasExplicitAncestor =
				type.getAscendants().getExplicitAncestor() != null;
		final Sample[] samples = this.type.getSamples();

		for (int i = samples.length - 1; i >= 0; --i) {

			final Sample sample = samples[i];

			if (sample.isExplicit()) {
				if (hasExplicitAncestor) {
					definitions = definitions.override(ancestorDefinitions);
					hasExplicitAncestor = false;
				}
			}

			definitions = sample.overrideDefinitions(
					scope,
					definitions,
					ancestorDefinitions);
		}

		if (hasExplicitAncestor) {
			definitions = definitions.override(ancestorDefinitions);
		}

		return definitions;
	}

	private void declareMembers() {
		this.objectMembers = new ObjectMembers(this);
		declareScopeField();
		declareMembers(this.objectMembers);

		final ObjectType objectType = type();

		for (Sample sample : objectType.getSamples()) {
			sample.deriveMembers(this.objectMembers);
		}

		final TypeRef ancestor = objectType.getAncestor();

		if (ancestor != null) {
			this.objectMembers.deriveMembers(
					ancestor.typeObject(objectType));
		}
	}

	private void declareScopeField() {
		if (getScope().getEnclosingScope().toObject() == null) {
			// Only object members may have an enclosing scope path.
			return;
		}

		final Path enclosingScopePath = getScope().getEnclosingScopePath();

		if (enclosingScopePath == null) {
			// Enclosing scope path not defined.
			return;
		}

		final Step[] steps = enclosingScopePath.getSteps();

		assert steps.length == 1 :
			"Enclosing path scope should contain exactly one step";

		final AbstractMemberStep step = (AbstractMemberStep) steps[0];
		final MemberKey memberKey = step.getMemberKey();

		if (memberKey.getOrigin() != getScope()) {
			// Enclosing scope field is derived from overridden object.
			return;
		}

		this.objectMembers.addMember(
				new ScopeField(this, memberKey.getMemberId()).toMember());
	}

	private void resolveAllMembers() {

		final boolean abstractAllowed =
				isAbstract()
				|| isPrototype()
				|| toClause() != null
				|| isWrapper();

		for (Member member : getMembers()) {
			if (!abstractAllowed && member.isAbstract()) {
				getLogger().abstractNotOverridden(
						this,
						member.getDisplayName());
			}
			if (member.isClone() && member.toField() == null) {
				// Only field clones require full resolution.
				continue;
			}
			member.resolveAll();
		}
	}

	private Symbol memberById(MemberId memberId) {
		resolveMembers(memberId.containsAdapterId());
		return this.symbols.get(memberId);
	}

	private Dep addDep(Dep dep) {

		final Object key = dep.getDepKey();
		final Dep found = this.deps.put(key, dep);

		if (found == null) {
			return dep;
		}

		this.deps.put(key, found);
		found.setDisabled(false);

		return found;
	}

	private boolean assertImplicitSamples(Sample[] samples) {
		for (Sample sample : samples) {
			assert !sample.isExplicit() :
				sample + " is explicit";
		}
		return true;
	}

}
