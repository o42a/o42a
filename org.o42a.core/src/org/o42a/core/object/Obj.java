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
package org.o42a.core.object;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.AbstractContainer.parentContainer;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberId.SCOPE_FIELD_ID;
import static org.o42a.core.member.clause.Clause.validateImplicitSubClauses;
import static org.o42a.core.object.impl.ObjectResolution.MEMBERS_RESOLVED;
import static org.o42a.core.object.impl.ObjectResolution.RESOLVING_MEMBERS;

import java.util.*;

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
import org.o42a.core.member.local.Dep;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.impl.*;
import org.o42a.core.object.link.Link;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.object.type.Sample;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.AbstractMemberStep;
import org.o42a.core.ref.path.impl.StaticObjectStep;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.DefinerEnv;
import org.o42a.core.st.impl.ObjectEnv;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.ArrayUtil;
import org.o42a.util.fn.Holder;
import org.o42a.util.log.Loggable;


public abstract class Obj
		extends ObjectBase
		implements MemberContainer, ClauseContainer {

	private final OwningObject owningObject = new OwningObject(this);

	private ObjectContent content;
	private ObjectContent clonesContent;
	private Ref self;
	private final Obj propagatedFrom;
	private Holder<Obj> cloneOf;
	private byte fullResolution;

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

	protected Obj(ObjectScope scope, Obj sample) {
		super(scope, new ObjectDistributor(scope, sample));
		this.propagatedFrom = sample;
		scope.setScopeObject(this);
	}

	public Obj(LocationInfo location, Distributor enclosing) {
		this(new ObjScope(location, enclosing));
	}

	public Obj(Distributor enclosing, Obj sample) {
		this(new ObjScope(sample, enclosing), sample);
	}

	public final boolean isNone() {
		return is(getContext().getNone());
	}

	@Override
	public Obj getContainer() {
		return this;
	}

	public boolean isAbstract() {

		final Field field = getScope().toField();

		return field != null && field.isAbstract();
	}

	public boolean isPrototype() {

		final Field field = getScope().toField();

		return field != null && field.isPrototype();
	}

	public boolean isValid() {
		return true;
	}

	public Link getDereferencedLink() {
		return null;
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
		if (this.cloneOf != null) {
			return this.cloneOf.get();
		}

		final Obj cloneOf = findCloneOf();

		this.cloneOf = new Holder<Obj>(cloneOf);

		return cloneOf;
	}

	public ConstructionMode getConstructionMode() {
		return type().getAscendants().getConstructionMode();
	}

	public final OwningObject toMemberOwner() {
		return this.owningObject;
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
	public final LocalScope toLocal() {
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
			if (member.toAlias() != null) {
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

		return this.explicitClauses = explicitClauses;
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
	public final Path member(
			PlaceInfo user,
			Accessor accessor,
			MemberId memberId,
			Obj declaredIn) {

		final Member found = objectMember(accessor, memberId, declaredIn);

		if (found != null) {
			return found.getMemberKey().toPath();
		}
		if (declaredIn == null) {
			return null;
		}

		final Member adapter = member(adapterId(declaredIn));

		if (adapter == null) {
			return null;
		}

		final Path adapterPath = adapter.getMemberKey().toPath();
		final Obj adapterObject = adapter.substance(dummyUser()).toObject();

		if (adapterObject != null) {

			final Member foundInAdapterObject =
					adapterObject.objectMember(accessor, memberId, declaredIn);

			if (foundInAdapterObject == null) {
				return null;
			}

			return adapterPath.append(foundInAdapterObject.getMemberKey());
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
			MemberId memberId,
			Obj declaredIn) {

		final Path found = member(user, accessor, memberId, declaredIn);

		if (found != null) {
			return found;
		}
		if (declaredIn != null) {
			return null;
		}

		final Field field = getScope().toField();

		if (field == null) {
			return null;
		}
		if (field.getKey().getMemberId().equals(memberId)) {
			return Path.SELF_PATH;
		}

		return null;
	}

	public Definitions overrideDefinitions(
			Scope scope,
			Definitions ascendantDefinitions) {

		final Definitions explicitDefinitions =
				value().getExplicitDefinitions().upgradeScope(scope);

		if (ascendantDefinitions == null) {
			return explicitDefinitions;
		}

		return ascendantDefinitions.override(explicitDefinitions);
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

		if (enclosingScope.is(enclosing)) {
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

	public final FieldUses fieldUses() {
		if (this.fieldUses != null) {
			return this.fieldUses;
		}
		return this.fieldUses = new FieldUses(this);
	}

	public final Ref selfRef() {
		if (this.self != null) {
			return this.self;
		}
		return this.self =
				Path.SELF_PATH
				.bindStatically(this, getScope())
				.target(distribute());
	}

	public final ObjectContent content() {
		if (this.content != null) {
			return this.content;
		}

		final Obj cloneOf = getCloneOf();

		if (cloneOf != null) {
			return this.content = cloneOf.clonesContent();
		}

		return this.content = new ObjectContent(this, false);
	}

	public final ObjectContent clonesContent() {
		if (this.clonesContent != null) {
			return this.clonesContent;
		}

		final Obj cloneOf = getCloneOf();

		if (cloneOf != null) {
			return this.clonesContent = cloneOf.clonesContent();
		}

		return this.clonesContent = new ObjectContent(this, true);
	}

	public DefinerEnv definitionEnv() {
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

		final ObjectIR ir = this.ir;

		if (ir != null && ir.getGenerator() == generator) {
			return ir;
		}

		return this.ir = createIR(generator);
	}

	public final boolean assertFullyResolved() {
		assert this.fullResolution > 0
			|| (isClone() && getCloneOf().fullResolution > 0):
				this + " is not fully resolved";
		return true;
	}

	public final void assertDerivedFrom(Obj type) {
		assert type().derivedFrom(type.type()) :
			this + " is not derived from " + type;
	}

	@Override
	public String toString() {

		final Scope scope = getScope();

		if (scope == null) {
			return super.toString();
		}

		return scope.toString();
	}

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

	protected abstract Definitions explicitDefinitions();

	protected Obj findCloneOf() {
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
	protected Dep addDep(Ref ref) {
		assert getContext().fullResolution().assertIncomplete();

		final int newDepId = this.depNameSeq + 1;
		final Dep newDep = new Dep(
				this,
				ref,
				Integer.toString(newDepId));
		final Dep dep = addDep(newDep);

		if (dep == newDep) {
			this.depNameSeq = newDepId;
		}

		return dep;
	}

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
			this.objectMembers.deriveMembers(ancestor.getType());
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

		final LinkUses linkUses = type().linkUses();
		final boolean abstractAllowed = abstractAllowed();

		for (Member member : getMembers()) {
			if (!abstractAllowed && member.isAbstract()) {
				getLogger().abstractNotOverridden(
						this,
						member.getDisplayName());
			}

			final MemberField field = member.toField();

			if (field == null) {
				if (member.isClone()) {
					// Only field clones require full resolution.
					continue;
				}
			} else if (linkUses != null) {
				linkUses.fieldChanged(field);
			}
			member.resolveAll();
		}
	}

	private boolean abstractAllowed() {
		if (isAbstract() || isPrototype()) {
			return true;
		}
		if (toClause() != null) {
			return true;
		}
		return isWrapper() || getDereferencedLink() != null;
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

			if (field.isScopeField()) {
				continue;
			}

			field.toObject().normalize(analyzer);
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

	private static final class ObjectDistributor extends Distributor {

		private final Scope scope;
		private final PlaceInfo placed;

		ObjectDistributor(Scope scope, PlaceInfo placed) {
			this.scope = scope;
			this.placed = placed;
		}

		@Override
		public Loggable getLoggable() {
			return this.placed.getLoggable();
		}

		@Override
		public CompilerContext getContext() {
			return this.placed.getContext();
		}

		@Override
		public ScopePlace getPlace() {
			return this.placed.getPlace();
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
