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
package org.o42a.core.member.clause;

import static org.o42a.core.member.MemberIdKind.CLAUSE_NAME;
import static org.o42a.core.member.clause.ClauseSubstitution.NO_SUBSTITUTION;
import static org.o42a.core.member.clause.impl.DeclaredGroupClause.declaredGroupClause;
import static org.o42a.core.member.clause.impl.DeclaredPlainClause.plainClause;
import static org.o42a.core.member.field.PrototypeMode.AUTO_PROTOTYPE;
import static org.o42a.core.member.field.PrototypeMode.NOT_PROTOTYPE;
import static org.o42a.core.member.field.PrototypeMode.PROTOTYPE;
import static org.o42a.core.st.impl.SentenceErrors.prohibitedInterrogativeAssignment;
import static org.o42a.util.ArrayUtil.append;

import org.o42a.core.Container;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.MemberName;
import org.o42a.core.member.MemberRegistry;
import org.o42a.core.member.clause.impl.*;
import org.o42a.core.member.field.AscendantsDefinition;
import org.o42a.core.member.field.PrototypeMode;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.source.Location;
import org.o42a.core.st.sentence.*;
import org.o42a.util.ArrayUtil;
import org.o42a.util.string.Name;


public final class ClauseBuilder extends ClauseBuilderBase {

	static final ReusedClauseRef[] NOTHING_REUSED = new ReusedClauseRef[0];

	private final Statements statements;
	private final MemberRegistry memberRegistry;
	private final ClauseDeclaration declaration;

	private MemberId overridden;
	private StaticTypeRef declaredIn;
	private AscendantsDefinition ascendants;
	private ReusedClauseRef[] reusedClauses = NOTHING_REUSED;
	private BlockBuilder declarations;
	private boolean mandatory;
	private PrototypeMode prototypeMode = NOT_PROTOTYPE;
	private boolean assignment;
	private ClauseSubstitution substitution = NO_SUBSTITUTION;

	ClauseBuilder(
			Statements statements,
			MemberRegistry memberRegistry,
			ClauseDeclaration declaration) {
		this.statements = statements;
		this.memberRegistry = memberRegistry;
		this.declaration = declaration;
	}

	public final Statements getStatements() {
		return this.statements;
	}

	public final Obj getMemberOwner() {
		return this.memberRegistry.getOwner();
	}

	@Override
	public final Scope getScope() {
		return this.declaration.getScope();
	}

	@Override
	public final Location getLocation() {
		return this.declaration.getLocation();
	}

	@Override
	public final Container getContainer() {
		return this.declaration.getContainer();
	}

	public final ClauseDeclaration getDeclaration() {
		return this.declaration;
	}

	public final boolean isMandatory() {
		return this.mandatory;
	}

	public final ClauseBuilder mandatory() {
		this.mandatory = true;
		return this;
	}

	public final MemberId getOverridden() {
		return this.overridden;
	}

	public final ClauseBuilder setOverridden(MemberId overridden) {
		assert getDeclaration().getKind() == ClauseKind.OVERRIDER :
			"Field override expected";
		this.overridden = overridden;
		return this;
	}

	public final StaticTypeRef getDeclaredIn() {
		return this.declaredIn;
	}

	public final ClauseBuilder setDeclaredIn(StaticTypeRef declaredIn) {
		assert getDeclaration().getKind() == ClauseKind.OVERRIDER :
			"Field override expected";
		this.declaredIn = declaredIn;
		return this;
	}

	public final PrototypeMode getPrototypeMode() {
		return this.prototypeMode;
	}

	public final ClauseBuilder prototype() {
		assert getDeclaration().getKind() == ClauseKind.OVERRIDER :
			"Field override expected";
		this.prototypeMode = PROTOTYPE;
		return this;
	}

	public final ClauseBuilder autoPrototype() {
		assert getDeclaration().getKind() == ClauseKind.OVERRIDER :
			"Field override expected";
		this.prototypeMode = AUTO_PROTOTYPE;
		return this;
	}

	public final boolean isAssignment() {
		return this.assignment;
	}

	public final ClauseBuilder assignment() {
		assert getDeclaration().getKind() == ClauseKind.EXPRESSION :
			"Only expressioncan be assigned";
		if (isAssignment() && getStatements().isInterrogation()) {
			prohibitedInterrogativeAssignment(getDeclaration());
			return null;
		}
		this.assignment = true;
		return this;
	}

	public final ClauseSubstitution getSubstitution() {
		return this.substitution;
	}

	public final ClauseBuilder setSubstitution(
			ClauseSubstitution substitution) {
		assert validSubstitution(substitution);
		this.substitution = substitution;
		return this;
	}

	public final AscendantsDefinition getAscendants() {
		return this.ascendants;
	}

	public final ClauseBuilder setAscendants(AscendantsDefinition ascendants) {
		this.ascendants = ascendants;
		return this;
	}

	public final BlockBuilder getDeclarations() {
		return this.declarations;
	}

	public final ClauseBuilder setDeclarations(BlockBuilder declarations) {
		assert getDeclaration().getKind().isPlain() :
			"Declarations block is only allowed for plain clause";
		assert !getSubstitution().substitutes() :
			"Can not provide declarations for substitution";
		this.declarations = declarations;
		return this;
	}

	public final ClauseBuilder reuseClause(
			Ref reusedClause,
			boolean reuseContents) {
		this.reusedClauses = append(
				this.reusedClauses,
				new ReusedClauseRef(reusedClause, reuseContents));
		return this;
	}

	public ReusedClause[] reuseClauses(Clause clause) {

		final ReusedClauseRef[] reusedRefs = this.reusedClauses;

		if (reusedRefs.length == 0) {
			return Clause.NOTHING_REUSED;
		}

		final ReusedClause[] reused = new ReusedClause[reusedRefs.length];
		int idx = 0;

		// Reuse in descending precedence order,
		// i.e. reverse to declaration order.
		for (int i = reusedRefs.length - 1; i >= 0; --i) {

			final ReusedClause reusedClause = reusedRefs[i].reuse(clause);

			if (reusedClause != null) {
				reused[idx++] = reusedClause;
			}
		}

		return ArrayUtil.clip(reused, idx);
	}

	public void build() {
		assert getDeclaration().getKind().isPlain() :
			"Plain clause declaration expected: " + getDeclaration();

		final DeclaredPlainClause clause = plainClause(this);

		getMemberRegistry().declareMember(clause.toMember());
		declareAlias(clause);

		getStatements().statement(
				new ClauseDeclarationStatement(this, clause, null));
	}

	@Override
	protected Block parentheses(Group group) {
		assert getDeclaration().getKind() == ClauseKind.GROUP :
			"Group declaration expected: " + getDeclaration();

		final DeclaredGroupClause clause = declaredGroupClause(this);
		final Block definition = clause.parentheses(group);

		if (definition == null) {
			return null;
		}

		getMemberRegistry().declareMember(clause.toMember());
		declareAlias(clause);

		group.getStatements().statement(
				new ClauseDeclarationStatement(this, clause, definition));

		return definition;
	}

	@Override
	protected ImperativeBlock braces(Group group, Name name) {
		assert getDeclaration().getKind() == ClauseKind.GROUP :
			"Group declaration expected: " + getDeclaration();

		final DeclaredGroupClause clause = declaredGroupClause(this);
		final ImperativeBlock definition = clause.braces(group, name);

		if (definition == null) {
			return null;
		}

		getMemberRegistry().declareMember(clause.toMember());
		declareAlias(clause);

		group.getStatements().statement(
				new ClauseDeclarationStatement(this, clause, definition));

		return definition;
	}

	final MemberRegistry getMemberRegistry() {
		return this.memberRegistry;
	}

	private boolean validSubstitution(ClauseSubstitution substitution) {
		if (substitution.substitutes()) {
			assert getDeclaration().getKind() == ClauseKind.EXPRESSION
					|| getDeclaration().getKind() == ClauseKind.OVERRIDER:
				"Can only substitute the value to assignment or overrider";
			assert getDeclarations() == null :
				"Can not provide the declarations for a value substitution";
		}
		return true;
	}

	private void declareAlias(Clause clause) {

		final Name name = getDeclaration().getName();

		if (name == null) {
			return;
		}

		final MemberId id = clause.toMember().getMemberId();
		final MemberName memberName = id.getMemberName();

		if (memberName != null && memberName.getName().is(name)) {
			return;
		}

		final MemberId aliasName = CLAUSE_NAME.memberName(name);
		final MemberId aliasId;
		final MemberId enclosingId = id.getEnclosingId();

		if (enclosingId != null) {
			aliasId = enclosingId.append(aliasName);
		} else {
			aliasId = aliasName;
		}

		final ClauseAlias alias = new ClauseAlias(aliasId, clause.toMember());

		getMemberRegistry().declareMember(alias);
	}

}
