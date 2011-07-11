/*
    Test Framework
    Copyright (C) 2011 Ruslan Lopatin

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
package org.o42a.lib.test.run;

import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.artifact.Artifact;
import org.o42a.core.artifact.common.PlainObject;
import org.o42a.core.artifact.object.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.Visibility;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.path.Path;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.ImperativeSentence;
import org.o42a.core.st.sentence.Imperatives;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.lib.test.TestModule;
import org.o42a.util.use.UserInfo;


final class TestRunner extends PlainObject {

	public static void runTest(
			TestModule module,
			UserInfo user,
			ImperativeSentence sentence,
			Field<?> field) {
		if (field.getVisibility() != Visibility.PUBLIC) {
			return; // Only public fields recognized as tests.
		}

		final Artifact<?> artifact = field.getArtifact();

		if (!artifact.isValid()) {
			return;// Invalid artifact.
		}

		final Obj test = artifact.materialize();

		if (test == null) {
			return;
		}

		final ObjectType testType = module.test(user);

		if (test.type().useBy(user).derivedFrom(testType)) {
			run(
					sentence,
					testName(sentence, field, test),
					field,
					null);
			return;
		}

		final Member adapterMember =
			test.member(adapterId(testType.getObject()));

		if (adapterMember == null) {
			return;
		}

		final Artifact<?> adapterArtifact =
			adapterMember.toField(user).getArtifact();

		if (!adapterArtifact.isValid()) {
			return;
		}

		final Obj adapter = adapterArtifact.materialize();

		if (adapter == null) {
			return;
		}

		run(
				sentence,
				testName(sentence, field, adapter),
				field,
				adapterMember.getKey());
	}

	private static void run(
			ImperativeSentence sentence,
			String name,
			Field<?> field,
			MemberKey adapterKey) {

		final Imperatives statements =
			sentence.alternative(sentence.getBlock());
		final RunTest runTest = new RunTest(
				sentence,
				statements.nextDistributor(),
				name,
				field.getKey(),
				adapterKey);

		statements.expression(runTest);
	}

	private static String testName(
			ImperativeSentence sentence,
			Field<?> field,
			Obj test) {

		final Obj nameObject =
			test.field("name").substance(dummyUser()).toObject();
		final Value<?> nameValue = nameObject.value().getValue();

		if (!nameValue.isDefinite()) {
			sentence.getLogger().indefiniteValue(nameObject);
		} else {
			if (!nameValue.isFalse()) {

				final String name =
					ValueType.STRING.cast(nameValue).getDefiniteValue();

				if (!name.isEmpty()) {
					return name;
				}
			}
		}

		return field.getDisplayName().replace('_', ' ');
	}

	private final RunTest runTest;

	private TestRunner(RunTest runTest) {
		super(runTest, runTest.distribute());
		this.runTest = runTest;
	}

	@Override
	public String toString() {
		return this.runTest.toString();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(this.runTest.ancestor(this));
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
	}

	@Override
	protected Definitions explicitDefinitions() {
		return null;
	}

	private static final class RunTest extends ObjectConstructor {

		private final MemberKey testKey;
		private final MemberKey adapterKey;
		private final String name;

		RunTest(
				LocationInfo location,
				Distributor distributor,
				String name,
				MemberKey testKey,
				MemberKey adapterKey) {
			super(location, distributor);
			this.testKey = testKey;
			this.adapterKey = adapterKey;
			this.name = name;
		}

		@Override
		public TypeRef ancestor(LocationInfo location) {

			final Scope localScope = getScope();

			assert localScope.toLocal() != null :
				this + " should be inside of local scope, but is inside of "
				+ localScope;

			final Scope objectTestsScope = localScope.getEnclosingScope();
			final Path objectPath =
				localScope.getEnclosingScopePath().append(
						objectTestsScope.getEnclosingScopePath());
			final Path testPath = objectPath.append(this.testKey);

			return testPath.target(
					location,
					localScope.distribute()).toTypeRef();
		}

		@Override
		public Ref reproduce(Reproducer reproducer) {
			return new RunTest(
					this,
					reproducer.distribute(),
					this.name,
					this.testKey,
					this.adapterKey);
		}

		@Override
		public String toString() {
			return "Run test[" + this.name + ']';
		}

		@Override
		protected Obj createObject() {
			return new TestRunner(this);
		}

	}

}
