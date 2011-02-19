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
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.core.member.clause.Clause.validateImplicitSubClauses;
import static org.o42a.core.member.local.Dep.enclosingOwnerDep;
import static org.o42a.core.member.local.Dep.fieldDep;

import java.util.*;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.*;
import org.o42a.core.artifact.*;
import org.o42a.core.artifact.array.Array;
import org.o42a.core.artifact.link.Link;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.IRGenerator;
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
import org.o42a.core.ref.path.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.util.ArrayUtil;


public abstract class Obj extends Artifact<Obj>
		implements Container, ClauseContainer {

	public static final MemberId SCOPE_MEMBER_ID = memberName("_scope");

	public static DeclaredField<Obj> declareField(MemberField member) {
		return new DeclaredObjectField(member);
	}

	public static FieldIR<Obj> fieldIR(
			IRGenerator generator,
			Field<Obj> field) {
		return new ObjectFieldIR(generator, field);
	}

	private Resolution resolution = Resolution.NOT_RESOLVED;

	private final HashMap<MemberKey, Member> members =
		new HashMap<MemberKey, Member>();
	private final HashMap<MemberId, Symbol> symbols =
		new HashMap<MemberId, Symbol>();
	private final LinkedHashMap<MemberKey, Dep> deps =
		new LinkedHashMap<MemberKey, Dep>();

	private boolean resolveAll;
	private ValueType<?> valueType;

	private Ascendants ascendants;

	private Definitions definitions;
	private Value<?> value;

	private ObjectIR ir;
	private ObjectValueIR valueIR;

	private Clause[] explicitClauses;
	private Clause[] implicitClauses;

	private ObjectMembers objectMembers;

	public Obj(LocationSpec location, Distributor enclosing) {
		this(new ObjScope(location, enclosing));
	}

	public Obj(Scope scope) {
		super(scope);
	}

	protected Obj(ObjectScope scope) {
		super(scope);
		scope.setScopeObject(this);
	}

	protected Obj(Scope scope, Obj sample) {
		super(scope, sample);
	}

	@Override
	public final ArtifactKind<Obj> getKind() {
		return ArtifactKind.OBJECT;
	}

	@Override
	public final Container getContainer() {
		return this;
	}

	public boolean isRuntime() {
		resolve();
		return this.ascendants.isRuntime();
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
		resolve();
		return this.ascendants.getDirective();
	}

	public Obj getWrapped() {
		return this;
	}

	public final boolean isClone() {

		final Field<?> field = getScope().toField();

		return field != null && field.isClone();
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

	public final Ascendants getAscendants() {
		resolve();
		return this.ascendants;
	}

	public TypeRef getAncestor() {
		return getAscendants().getAncestor();
	}

	/**
	 * Object samples in descending precedence order.
	 *
	 * <p>This is an order reverse to their appearance in source code.</p>
	 *
	 * @return array of object samples.
	 */
	public final Sample[] getSamples() {
		return getAscendants().getSamples();
	}

	public final boolean membersResolved() {
		return this.resolution.membersResolved();
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

		for (Sample sample : getSamples()) {
			implicitClauses = ArrayUtil.append(
					implicitClauses,
					sample.getType().getImplicitClauses());
		}

		final TypeRef ancestor = getAncestor();

		if (ancestor != null) {
			implicitClauses = ArrayUtil.append(
					implicitClauses,
					ancestor.getType().getImplicitClauses());
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
			ScopeSpec user,
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

	public boolean inherits(Artifact<?> other) {
		if (this == other) {
			return true;
		}

		final TypeRef ancestor = getAncestor();

		if (ancestor == null) {
			return false;
		}

		return ancestor.getType().inherits(other);
	}

	public final boolean derivedFrom(Obj other) {
		return derivedFrom(other, Derivation.ANY, Integer.MAX_VALUE);
	}

	public final boolean derivedFrom(Obj other, Derivation derivation) {
		return derivedFrom(other, derivation, Integer.MAX_VALUE);
	}

	public boolean derivedFrom(Obj other, Derivation derivation, int depth) {
		if (derivation.match(this, other)) {
			return true;
		}

		final int newDepth = depth - 1;

		if (newDepth < 0) {
			return false;
		}

		if (derivation.acceptAncestor()) {

			final TypeRef ancestor = getAncestor();

			if (ancestor != null) {
				if (ancestor.getType().derivedFrom(
						other,
						derivation,
						newDepth)) {
					return true;
				}
			}
		}

		if (derivation.acceptsSamples()) {
			for (Sample sample : getSamples()) {
				if (!derivation.acceptSample(sample)) {
					continue;
				}
				if (sample.getType().derivedFrom(
						other,
						derivation,
						newDepth)) {
					return true;
				}
			}
			for (Sample sample : getAscendants().getDiscardedSamples()) {
				if (!derivation.acceptSample(sample)) {
					continue;
				}
				if (sample.getType().derivedFrom(
						other,
						derivation,
						newDepth)) {
					return true;
				}
			}
		}

		return false;
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
		if (!this.resolution.membersResolved()) {
			if (!resolveIfNotResolving()) {
				return;
			}

			final Resolution resolution = this.resolution;

			if (resolution.typeResolved() && !resolution.membersResolved()) {
				// resolution is not in progress - resolve members
				// otherwise members are empty
				this.resolution = Resolution.RESOLVING_MEMBERS;
				try {
					declareMembers();
					this.objectMembers.registerMembers(resolveAdapters);
				} finally {
					this.resolution = resolution;
				}
				this.resolution = Resolution.MEMBERS_RESOLVED;
				updateMembers();
				this.objectMembers.registerMembers(resolveAdapters);
			}
		}
	}

	@Override
	public final Path member(
			ScopeSpec user,
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
			adapter.getSubstance().toArtifact();
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

			final Obj type = typeRef.getType();

			if (type == null) {
				return null;
			}

			final Member foundInAdapterLink =
				type.objectMember(user, memberId, declaredIn);

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
			ScopeSpec user,
			MemberId memberId,
			Obj declaredIn) {
		return member(user, memberId, declaredIn);
	}

	@Override
	public final Path findPath(
			ScopeSpec user,
			MemberId memberId,
			Obj declaredIn) {
		return findContainerPath(this, user, memberId, declaredIn);
	}

	public final Value<?> getValue() {
		if (this.value == null) {
			this.value = calculateValue(getScope());
		}
		return this.value;
	}

	public final Value<?> value(Scope scope) {
		assertCompatible(scope);

		final Value<?> result;

		if (scope == getScope()) {
			result = getValue();
		} else {
			result = calculateValue(scope);
		}

		return result;
	}

	@Override
	public void resolveAll() {
		if (this.resolveAll) {
			return;
		}
		this.resolveAll = true;
		try {
			if (!resolveIfNotResolving()) {
				return;
			}
			resolveAllMembers();
			validateImplicitSubClauses(getExplicitClauses());
			getDefinitions();
		} finally {
			this.resolveAll = false;
		}
	}

	public final void assertDerivedFrom(Obj type) {
		assert derivedFrom(type) :
			this + " is not derived from " + type;
	}

	public final Definitions getDefinitions() {
		if (this.definitions != null) {
			return this.definitions;
		}

		resolve();

		final Definitions definitions =
			overrideDefinitions(getScope(), getOverriddenDefinitions());

		if (!isRuntime()) {
			return this.definitions = definitions;
		}

		return this.definitions = definitions.runtime(getScope());
	}

	public Definitions getOverriddenDefinitions() {
		return overriddenDefinitions(
				getScope(),
				getAncestorDefinitions());
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

	public final ObjectIR ir(IRGenerator generator) {
		if (this.ir == null || this.ir.getGenerator() != generator) {
			this.ir = createIR(generator);
		}
		return this.ir;
	}

	public final ObjectValueIR valueIR(IRGenerator generator) {
		if (this.valueIR == null || this.valueIR.getGenerator() != generator) {
			this.valueIR = createValueIR(ir(generator));
		}
		return this.valueIR;
	}

	protected final void resolve() {
		resolve(false);
	}

	protected final boolean resolveIfNotResolving() {
		return resolve(true);
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
		return getAncestor().getType().getValueType();
	}

	protected abstract void declareMembers(ObjectMembers members);

	protected void updateMembers() {
	}

	protected abstract Definitions overrideDefinitions(
			Scope scope,
			Definitions ascendantDefinitions);

	protected Value<?> calculateValue(Scope scope) {
		return getDefinitions().value(scope).getValue();
	}

	protected ObjectIR createIR(IRGenerator generator) {
		return new ObjectIR(generator, this);
	}

	protected ObjectValueIR createValueIR(ObjectIR objectIR) {
		return new ObjectValueIR(objectIR);
	}

	final Map<MemberKey, Member> members() {
		return Obj.this.members;
	}

	final Map<MemberId, Symbol> symbols() {
		return Obj.this.symbols;
	}

	Definitions overriddenDefinitions(
			Scope scope,
			Definitions overriddenDefinitions) {

		final Sample[] samples = getSamples();

		if (samples.length == 0) {
			return overriddenDefinitions;
		}

		Definitions definitions = overriddenDefinitions;

		for (int i = samples.length - 1; i >= 0; --i) {
			definitions =
				samples[i].overrideDefinitions(scope, definitions);
		}

		return definitions;
	}

	private final boolean resolve(boolean skipIfResolving) {
		if (this.resolution == Resolution.NOT_RESOLVED) {
			try {
				this.resolution = Resolution.RESOLVING_TYPE;
				assignAscendants();
			} finally {
				this.resolution = Resolution.NOT_RESOLVED;
			}
			this.resolution = Resolution.TYPE_RESOLVED;
			this.ascendants.validate();
			postResolve();
			this.resolution = Resolution.POST_RESOLVED;
		} else if (this.resolution == Resolution.RESOLVING_TYPE) {
			if (!skipIfResolving) {
				getLogger().recursiveResolution(this, this);
			}
			return false;
		}

		return this.resolution.resolved();
	}

	private void assignAscendants() {
		this.ascendants = buildAscendants();
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

		for (Sample sample : getSamples()) {
			sample.inheritMembers(this.objectMembers);
		}

		final TypeRef ancestor = getAncestor();

		if (ancestor != null) {
			this.objectMembers.deriveMembers(ancestor.getType());
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

	private Dep addDep(MemberKey memberKey) {

		final Dep found = this.deps.get(memberKey);

		if (found != null) {
			return found;
		}

		final Dep dep = fieldDep(this, memberKey);

		this.deps.put(memberKey, dep);

		return dep;
	}

	private Dep addEnclosingOwnerDep(Obj owner) {

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

	private Symbol memberById(MemberId memberId) {
		resolveMembers(memberId.containsAdapterId());
		return this.symbols.get(memberId);
	}

	private Definitions getAncestorDefinitions() {

		final Definitions ancestorDefinitions;
		final TypeRef ancestor = getAncestor();

		if (ancestor == null) {
			ancestorDefinitions = null;
		} else {
			ancestorDefinitions =
				ancestor.getType().getDefinitions().upgradeScope(getScope());
		}

		return ancestorDefinitions;
	}

	private static final class ParentObjectFragment extends MemberFragment {

		ParentObjectFragment(MemberKey memberKey) {
			super(memberKey);
		}

		@Override
		public Container resolve(
				LocationSpec location,
				Path path,
				int index,
				Scope start,
				PathWalker walker) {

			final Obj object = start.getContainer().toObject();

			if (!object.membersResolved()) {

				final Scope self = getMemberKey().getOrigin();

				if (start == self) {

					final Container result = self.getEnclosingContainer();

					walker.up(object, this, result);

					return result;
				}
			}

			final Member member = resolveMember(location, path, index, start);

			if (member == null) {
				return null;
			}

			final Container result = member.getSubstance();

			walker.up(object, this, result);

			return result;
		}

		@Override
		protected Reproduction reproduce(
				LocationSpec location,
				Scope origin,
				Scope scope) {

			final Clause fromClause = origin.getContainer().toClause();

			if (fromClause == null) {
				// Walked out of object, containing clauses.
				return outOfClause(scope.getScope().getEnclosingScopePath());
			}

			final Clause enclosingClause = fromClause.getEnclosingClause();

			if (enclosingClause == null && !fromClause.requiresInstance()) {
				// Left stand-alone clause without enclosing object.
				return outOfClause(scope.getScope().getEnclosingScopePath());
			}

			// Update to actual enclosing scope path.
			return reproduced(scope.getScope().getEnclosingScopePath());
		}

	}

	private static final class ParentLocalFragment extends PathFragment {

		private final Obj object;

		ParentLocalFragment(Obj object) {
			this.object = object;
		}

		@Override
		public Container resolve(
				LocationSpec location,
				Path path,
				int index,
				Scope start,
				PathWalker walker) {

			final Obj object = start.getContainer().toObject();

			assert object == this.object :
				"Wrong container: " + start
				+ ", but " + this.object + " expected";

			final Container result =
				this.object.getScope().getEnclosingContainer();

			walker.up(object, this, result);

			return result;
		}

		@Override
		public HostOp write(Code code, CodePos exit, HostOp start) {
			return start;
		}

		@Override
		public PathFragment combineWithMember(MemberKey memberKey) {
			return this.object.addDep(memberKey);
		}

		@Override
		public PathFragment combineWithLocalOwner(Obj owner) {
			return this.object.addEnclosingOwnerDep(owner);
		}

		@Override
		public Reproduction reproduce(LocationSpec location, Scope scope) {
			return reproduced(scope.getScope().getEnclosingScopePath());
		}

		@Override
		public int hashCode() {
			return this.object.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}

			final ParentLocalFragment other = (ParentLocalFragment) obj;

			return this.object == other.object;
		}

		@Override
		public String toString() {
			return "ParentLocal[" + this.object + ']';
		}

	}

	private enum Resolution {

		NOT_RESOLVED(0),
		RESOLVING_TYPE(-1),
		TYPE_RESOLVED(1),
		POST_RESOLVED(2),
		RESOLVING_MEMBERS(-3),
		MEMBERS_RESOLVED(3);

		private final int code;

		Resolution(int code) {
			this.code = code;
		}

		boolean resolved() {
			return this.code >= POST_RESOLVED.code;
		}

		boolean membersResolved() {
			return (this.code >= MEMBERS_RESOLVED.code
					|| this.code < RESOLVING_MEMBERS.code);
		}

		boolean typeResolved() {
			return this.code > 0;
		}

	}

}
