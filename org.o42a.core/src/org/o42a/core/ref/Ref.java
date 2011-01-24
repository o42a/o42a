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

import static org.o42a.core.artifact.TypeRef.staticTypeRef;
import static org.o42a.core.artifact.TypeRef.typeRef;
import static org.o42a.core.ref.path.Path.ROOT_PATH;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.core.*;
import org.o42a.core.artifact.*;
import org.o42a.core.artifact.link.TargetRef;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.def.*;
import org.o42a.core.ir.HostOp;
import org.o42a.core.ir.local.Control;
import org.o42a.core.ir.local.LocalBuilder;
import org.o42a.core.ir.local.StOp;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.ir.op.ValOp;
import org.o42a.core.member.clause.Clause;
import org.o42a.core.member.clause.GroupClause;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.local.LocalScope;
import org.o42a.core.ref.path.AbsolutePath;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.*;
import org.o42a.core.st.action.Action;
import org.o42a.core.st.action.ExecuteCommand;
import org.o42a.core.st.action.ReturnValue;
import org.o42a.core.st.sentence.Block;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.LogicalValue;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public abstract class Ref extends RefBase {

	public static Ref voidRef(LocationSpec location, Distributor distributor) {

		final Field<Obj> wrapperField =
			location.getContext().getIntrinsics().getVoidField();

		return ROOT_PATH.append(
				wrapperField.getKey()).target(location, distributor);
	}

	public static Ref falseRef(LocationSpec location, Distributor distributor) {

		final Obj falseObject = location.getContext().getFalse();
		final AbsolutePath falsePath = ROOT_PATH.append(
			falseObject.getScope().toField().toMember().getKey());

		return falsePath.target(location, distributor);
	}

	public static Ref runtimeRef(
			LocationSpec location,
			Distributor distributor) {
		return new RuntimeRef(location, distributor);
	}

	private ConditionsWrap conditions;
	private Logical logical;
	private RefOp op;

	public Ref(LocationSpec location, Distributor distributor) {
		this(location, distributor, null);
	}

	Ref(LocationSpec location, Distributor distributor, Logical logical) {
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
			this.logical = new RefLogical();
		}
		return this.logical;
	}

	public final Resolution getResolution() {
		return resolve(getScope());
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
	public StatementKind getKind() {
		return StatementKind.VALUE;
	}

	@Override
	public Conditions setConditions(Conditions conditions) {
		assert this.conditions == null :
			"Conditions already assigned for: " + conditions;
		return this.conditions = new ConditionsWrap(conditions);
	}

	@Override
	public Definitions define(DefinitionTarget target) {

		final ValueType<?> expectedType = target.getExpectedType();
		final Def def;

		if (expectedType != null) {
			def = adapt(
					this,
					expectedType.typeRef(
							this,
							getScope())).toDef();
		} else {
			def = toDef();
		}

		return getInitialConditions().apply(def).toDefinitions();
	}

	public abstract Resolution resolve(Scope scope);

	public final Value<?> getValue() {
		return value(getScope());
	}

	public Value<?> value(Scope scope) {
		return resolve(scope).materialize().getValue();
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

	public Ref fixScope() {
		return new FixedScopeRef(this);
	}

	public final Ref adapt(LocationSpec location, StaticTypeRef adapterType) {
		return new Adapter(location, this, adapterType, true);
	}

	public final Ref findAdapter(
			LocationSpec location,
			StaticTypeRef adapterType) {
		return new Adapter(location, this, adapterType, false);
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

		return new ApplyDirective(directive);
	}

	public Ref and(Logical logical) {
		if (logical.isTrue()) {
			return this;
		}
		return new ConditionalRef(this, logical);
	}

	public TypeRef toTypeRef() {
		return typeRef(this);
	}

	public TargetRef toTargetRef() {
		return new TargetRef(this);
	}

	public StaticTypeRef toStaticTypeRef() {
		return staticTypeRef(this);
	}

	public Rescoper toRescoper() {
		return new RefRescoper(this);
	}

	public final RefOp op(HostOp host) {

		final RefOp op = this.op;

		if (op != null && op.host() == host) {
			return op;
		}

		return this.op = createOp(host);
	}

	protected abstract RefOp createOp(HostOp host);

	@Override
	protected final StOp createOp(LocalBuilder builder) {
		return new RefStOp(builder, this, op(builder.host()));
	}

	@Override
	protected Ref clone() throws CloneNotSupportedException {

		final Ref clone = (Ref) super.clone();

		clone.logical = null;
		clone.op = null;

		return clone;
	}

	private Conditions getInitialConditions() {
		return this.conditions.conditions;
	}

	private final class ConditionsWrap extends Conditions {

		private final Conditions conditions;
		private Conditions wrapped;

		ConditionsWrap(Conditions conditions) {
			this.conditions = conditions;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return getWrapped().prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {
			return getWrapped().precondition(scope);
		}

		@Override
		public String toString() {
			if (this.wrapped != null) {
				return this.wrapped.toString();
			}
			return this.conditions + ", " + Ref.this;
		}

		private Conditions getWrapped() {
			if (this.wrapped != null) {
				return this.wrapped;
			}
			return this.wrapped = new RefConditions(this.conditions);
		}

		private void setWrapped(Conditions wrapped) {
			assert this.wrapped == null :
				"Conditions already built for " + Ref.this;
			this.wrapped = wrapped;
		}

	}

	private final class RefConditions extends Conditions {

		private final Conditions conditions;

		RefConditions(Conditions conditions) {
			this.conditions = conditions;
		}

		@Override
		public Logical prerequisite(Scope scope) {
			return this.conditions.prerequisite(scope);
		}

		@Override
		public Logical precondition(Scope scope) {
			return this.conditions.precondition(scope).and(
					Ref.this.rescope(scope).getLogical());
		}

		@Override
		public String toString() {
			return this.conditions + ", " + Ref.this;
		}

	}

	private final class ApplyDirective implements Instruction {

		private final Directive directive;

		private ApplyDirective(Directive directive) {
			this.directive = directive;
		}

		@Override
		public <S extends Statements<S>> void execute(Block<S> block) {
			Ref.this.conditions.setWrapped(
					block.setConditions(getInitialConditions()));
			this.directive.apply(block, Ref.this);
		}

		@Override
		public String toString() {
			return "ApplyDirective[" + this.directive + ']';
		}

	}

	private final class RefLogical extends Logical {

		public RefLogical() {
			super(Ref.this, Ref.this.getScope());
		}

		@Override
		public LogicalValue getConstantValue() {
			return getValue().getLogicalValue();
		}

		@Override
		public LogicalValue logicalValue(Scope scope) {
			return value(scope).getLogicalValue();
		}

		@Override
		public Logical reproduce(Reproducer reproducer) {

			final Ref reproduced = Ref.this.reproduce(reproducer);

			if (reproduced == null) {
				return null;
			}

			return reproduced.getLogical();
		}

		@Override
		public void write(Code code, CodePos exit, HostOp host) {
			code.debug("Logical: " + this);
			Ref.this.op(host).writeLogicalValue(code, exit);
		}

		@Override
		public String toString() {
			return "(" + Ref.this + ")?";
		}

	}

	private static final class RefStOp extends StOp {

		private final RefOp ref;

		RefStOp(
				LocalBuilder builder,
				St statement,
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
