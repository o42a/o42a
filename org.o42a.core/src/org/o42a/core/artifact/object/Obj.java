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
package org.o42a.core.artifact.object;

import static org.o42a.core.AbstractContainer.findContainerPath;
import static org.o42a.core.AbstractContainer.parentContainer;
import static org.o42a.core.artifact.object.ObjectResolution.MEMBERS_RESOLVED;
import static org.o42a.core.artifact.object.ObjectResolution.RESOLVING_MEMBERS;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.clause.Clause.validateImplicitSubClauses;
import static org.o42a.core.member.local.Dep.enclosingOwnerDep;
import static org.o42a.core.member.local.Dep.fieldDep;
import static org.o42a.util.use.User.dummyUser;

import java.util.*;

import org.o42a.codegen.Generator;
import org.o42a.core.*;
import org.o42a.core.artifact.*;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.field.FieldIR;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.ir.object.ObjectValueIR;
import org.o42a.core.member.*;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.ClauseContainer;
import org.o42a.core.member.field.DeclaredField;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.member.local.Dep;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.Resolver;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.path.PathFragment;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.ArrayUtil;
import org.o42a.util.use.Usable;


public abstract class Obj extends Artifact<Obj>
		implements Container, ClauseContainer {

	public static final MemberId SCOPE_MEMBER_ID = memberName("_scope");

	public static DeclaredField<Obj, ?> declareField(MemberField member) {
		return new DeclaredObjectField(member);
	}

	public static FieldIR<Obj> fieldIR(
			Generator generator,
			Field<Obj> field) {
		return new ObjectFieldIR(generator, field);
	}

	private final ObjectType.UsableObjectType type;
	private final ObjectValue.UseableObjectValue value;

	private final HashMap<MemberKey, Member> members =
		new HashMap<MemberKey, Member>();
	private final HashMap<MemberId, Symbol> symbols =
		new HashMap<MemberId, Symbol>();
	private final LinkedHashMap<MemberKey, Dep> deps =
		new LinkedHashMap<MemberKey, Dep>();

	private ObjectMembers objectMembers;
	private Clause[] explicitClauses;
	private Clause[] implicitClauses;

	private ValueType<?> valueType;
	private Definitions definitions;

	private ObjectAnalysis analysis;

	private ObjectIR ir;
	private ObjectValueIR valueIR;

	public Obj(LocationInfo location, Distributor enclosing) {
		this(new ObjScope(location, enclosing));
	}

	public Obj(Scope scope) {
		super(scope);
		this.type = new ObjectType.UsableObjectType(this);
		this.value = new ObjectValue.UseableObjectValue(this);
	}

	protected Obj(ObjectScope scope) {
		super(scope);
		scope.setScopeObject(this);
		this.type = new ObjectType.UsableObjectType(this);
		this.value = new ObjectValue.UseableObjectValue(this);
	}

	protected Obj(Scope scope, Obj sample) {
		super(scope, sample);
		this.type = new ObjectType.UsableObjectType(this);
		this.value = new ObjectValue.UseableObjectValue(this);
	}

	@Override
	public final ArtifactKind<Obj> getKind() {
		return ArtifactKind.OBJECT;
	}

	public Artifact<?> getMaterializationOf() {
		return this;
	}

	public ConstructionMode getConstructionMode() {
		return objectType().getAscendants().getConstructionMode();
	}

	public final ObjectAnalysis getAnalysis() {
		if (this.analysis != null) {
			return this.analysis;
		}
		return this.analysis = new ObjectAnalysis(this);
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
	public final Array toArray() {
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

	@Override
	public Directive toDirective() {
		return objectType().getAscendants().getDirective();
	}

	public Obj getWrapped() {
		return this;
	}

	@Override
	public boolean isClone() {

		final Field<?> field = getScope().toField();

		if (field != null) {
			return field.isClone();
		}

		final Artifact<?> materializationOf = getMaterializationOf();

		if (materializationOf == this) {
			return false;
		}

		return materializationOf.isClone();
	}

	public final ValueType<?> getValueType() {
		if (this.valueType == null) {
			assignValueType();
		}
		return this.valueType;
	}

	public boolean isPropagated() {
		return false;
	}

	public final Usable<ObjectType> type() {
		return this.type;
	}

	public final Usable<ObjectValue> value() {
		return this.value;
	}

	public final boolean membersResolved() {
		return objectType().getResolution().membersResolved();
	}

	public Collection<Member> getMembers() {
		resolveMembers(true);
		return this.members.values();
	}

	public Clause[] getExplicitClauses() {
		if (this.explicitClauses != null) {
			return this.explicitClauses;
		}

		Clause[] explicitClauses = new Clause[0];
		final Scope origin = getScope();

		for (Member member : getMembers()) {

			final Clause clause = member.toClause();

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
	public Clause[] getImplicitClauses() {
		if (this.implicitClauses != null) {
			return this.implicitClauses;
		}

		Clause[] implicitClauses = new Clause[0];

		for (Clause clause : getExplicitClauses()) {
			if (clause.isImplicit()) {
				implicitClauses = ArrayUtil.append(implicitClauses, clause);
			}
		}

		for (Sample sample : objectType().getSamples()) {
			implicitClauses = ArrayUtil.append(
					implicitClauses,
					sample.typeObject(dummyUser()).getImplicitClauses());
		}

		final TypeRef ancestor = objectType().getAncestor();

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

	public final Member objectMember(
			ScopeInfo user,
			MemberId memberId,
			Obj declaredIn) {

		final Access access = accessBy(user);

		if (!access.isAccessible()) {
			return null;
		}

		return member(memberId, declaredIn, access.getAccessor());
	}

	public final Member member(String name) {
		return member(memberName(name), null, Accessor.PUBLIC);
	}

	public final Member member(String name, Accessor accessor) {
		return member(memberName(name), null, accessor);
	}

	public final Member member(MemberId memberId) {
		return member(memberId, null, Accessor.PUBLIC);
	}

	public final Member member(MemberId memberId, Accessor accessor) {
		return member(memberId, null, accessor);
	}

	public final Member member(
			MemberId memberId,
			Obj declaredIn,
			Accessor accessor) {
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

		final Field<?> field = getScope().toField();

		if (field == null || !field.isClone()) {
			return false;
		}

		final Obj overridden =
			field.getOverridden()[0].getArtifact().toObject();

		if (overridden == other) {
			return true;
		}

		return overridden.cloneOf(other);
	}

	public void resolveMembers(boolean resolveAdapters) {
		if (this.objectMembers != null) {
			// Register members incrementally.
			updateMembers();
			this.objectMembers.registerMembers(resolveAdapters);
			return;
		}

		final ObjectType objectType = objectType();

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
			ScopeInfo user,
			MemberId memberId,
			Obj declaredIn) {

		final Member found = objectMember(user, memberId, declaredIn);

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
				adapterObject.objectMember(user, memberId, declaredIn);

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

			final Member foundInAdapterLink =
				type.getObject().objectMember(user, memberId, declaredIn);

			if (foundInAdapterLink == null) {
				return null;
			}

			return adapterPath.materialize().append(
					foundInAdapterLink.getKey());
		}

		return null;
	}

	@Override
	public Clause clause(MemberId memberId, Obj declaredIn) {

		// Clauses are always accessible to public.
		final Member found = member(memberId, declaredIn, Accessor.PUBLIC);

		if (found == null) {
			return null;
		}

		return found.toClause();
	}

	@Override
	public final Path findMember(
			ScopeInfo user,
			MemberId memberId,
			Obj declaredIn) {
		return member(user, memberId, declaredIn);
	}

	@Override
	public final Path findPath(
			ScopeInfo user,
			MemberId memberId,
			Obj declaredIn) {
		return findContainerPath(this, user, memberId, declaredIn);
	}

	public final Definitions getDefinitions() {
		if (this.definitions != null) {
			return this.definitions;
		}

		resolve();

		final Definitions definitions =
			overrideDefinitions(getScope(), getOverriddenDefinitions());

		if (!getConstructionMode().isRuntime()) {
			return this.definitions = definitions;
		}

		return this.definitions = definitions.runtime();
	}

	public Definitions getOverriddenDefinitions() {

		final Definitions ancestorDefinitions = getAncestorDefinitions();

		return overriddenDefinitions(
				getScope(),
				ancestorDefinitions,
				ancestorDefinitions);
	}

	public Path scopePath() {

		final PathFragment scopePathFragment;
		final Container enclosing = getScope().getEnclosingContainer();

		if (enclosing.toObject() != null) {
			scopePathFragment = new ParentObjectFragment(
					SCOPE_MEMBER_ID.key(getScope()));
		} else {
			assert enclosing.toLocal() != null :
				"Unsupported kind of enclosing scope " + enclosing;
			scopePathFragment = new ParentLocalFragment(this);
		}

		return scopePathFragment.toPath();
	}

	public final void assertDerivedFrom(Obj type) {
		assert type().useBy(dummyUser()).derivedFrom(
				type.type().useBy(dummyUser())) :
					this + " is not derived from " + type;
	}

	public final ObjectIR ir(Generator generator) {

		final ObjectIR ir = this.ir;

		if (ir != null && ir.getGenerator() == generator) {
			return ir;
		}

		return this.ir = createIR(generator);
	}

	public final ObjectValueIR valueIR(Generator generator) {
		if (this.valueIR == null || this.valueIR.getGenerator() != generator) {
			this.valueIR = createValueIR(ir(generator));
		}
		return this.valueIR;
	}

	protected final void resolve() {
		objectType().resolve(false);
	}

	protected final boolean resolveIfNotResolving() {
		return objectType().resolve(true);
	}

	protected void postResolve() {
	}

	protected abstract Ascendants buildAscendants();

	protected final void setValueType(ValueType<?> valueType) {
		if (valueType != null) {
			this.valueType = valueType;
		}
	}

	protected ValueType<?> resolveValueType() {
		return objectType().getAncestor().typeObject(
				getScope().newResolver(dummyUser())).getValueType();
	}

	protected abstract void declareMembers(ObjectMembers members);

	protected void updateMembers() {
	}

	protected abstract Definitions overrideDefinitions(
			Scope scope,
			Definitions ascendantDefinitions);

	protected Value<?> calculateValue(Resolver resolver) {
		value().useBy(resolver);
		return getDefinitions().value(resolver).getValue();
	}

	@Override
	protected void fullyResolve() {
		resolve();
		objectType().getAscendants().resolveAll();
		if (!isClone()) {
			resolveAllMembers();
		}
		validateImplicitSubClauses(getExplicitClauses());
		getDefinitions().resolveAll();
	}

	protected ObjectIR createIR(Generator generator) {
		return new ObjectIR(generator, this);
	}

	protected ObjectValueIR createValueIR(ObjectIR objectIR) {
		return new ObjectValueIR(objectIR);
	}

	final ObjectType objectType() {
		return this.type.getType();
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
		if (overriddenDefinitions != null) {
			overriddenDefinitions.assertScopeIs(scope);
		}

		final Usable<ObjectValue> user =
			scope.getContainer().toObject().value();
		final ObjectType type = type().useBy(user);
		boolean hasExplicitAncestor =
			type.getAscendants().getExplicitAncestor() != null;
		final Sample[] samples = type.getSamples();
		Definitions definitions = overriddenDefinitions;

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

	Dep addDep(MemberKey memberKey) {
		assert getContext().fullResolution().assertIncomplete();

		final Dep found = this.deps.get(memberKey);

		if (found != null) {
			return found;
		}

		final Dep dep = fieldDep(this, memberKey);

		this.deps.put(memberKey, dep);

		return dep;
	}

	Dep addEnclosingOwnerDep(Obj owner) {
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

		final Dep dep = enclosingOwnerDep(this);

		this.deps.put(null, dep);

		return dep;
	}

	private void assignValueType() {
		if (this.valueType == null) {
			setValueType(resolveValueType());
		}
	}

	private void declareMembers() {
		this.objectMembers = new ObjectMembers(this);
		if (getScope().getEnclosingScopePath() != null
				&& getScope().getEnclosingContainer().toObject() != null) {
			this.objectMembers.addMember(new ScopeField(this).toMember());
		}
		declareMembers(this.objectMembers);

		for (Sample sample : objectType().getSamples()) {
			sample.deriveMembers(this.objectMembers);
		}

		final TypeRef ancestor = objectType().getAncestor();

		if (ancestor != null) {
			this.objectMembers.deriveMembers(ancestor.typeObject(type()));
		}
	}

	private void resolveAllMembers() {

		final boolean abstractAllowed =
			isAbstract()
			|| isPrototype()
			|| toClause() != null;

		for (Member member : getMembers()) {
			if (!abstractAllowed && member.isAbstract()) {
				getLogger().abstractNotOverridden(
						this,
						member.getDisplayName());
			}
			member.resolveAll();
		}
	}

	private Symbol memberById(MemberId memberId) {
		resolveMembers(memberId.containsAdapterId());
		return this.symbols.get(memberId);
	}

	private Definitions getAncestorDefinitions() {

		final Definitions ancestorDefinitions;
		final TypeRef ancestor = objectType().getAncestor();

		if (ancestor == null) {
			ancestorDefinitions = null;
		} else {
			ancestorDefinitions =
				ancestor.typeObject(value())
				.getDefinitions().upgradeScope(getScope());
		}

		return ancestorDefinitions;
	}

}
