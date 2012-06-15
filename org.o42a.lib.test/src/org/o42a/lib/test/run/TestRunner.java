/*
    Test Framework
    Copyright (C) 2011,2012 Ruslan Lopatin

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

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.member.MemberId.fieldName;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.analysis.use.UserInfo;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.member.*;
import org.o42a.core.member.field.Field;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.ObjectType;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.Imperatives;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;
import org.o42a.lib.test.TestModule;


final class TestRunner extends ConstructedObject {

	private static final MemberName NAME_MEMBER =
			fieldName(CASE_INSENSITIVE.canonicalName("name"));

	public static void runTest(
			TestModule module,
			UserInfo user,
			Imperatives statements,
			Field field) {
		if (field.getVisibility() != Visibility.PUBLIC) {
			return; // Only public fields recognized as tests.
		}

		final Obj test = field.toObject();

		if (!test.isValid()) {
			return;// Invalid test object.
		}

		final ObjectType testType = module.test(user);

		if (test.type().useBy(user).derivedFrom(testType)) {
			run(statements, testName(statements, field, test), field);
			return;
		}

		final Member adapterMember =
				test.member(adapterId(testType.getObject()));

		if (adapterMember == null) {
			return;
		}

		final Obj adapter = adapterMember.toField().object(user);

		if (!adapter.isValid()) {
			return;
		}

		run(statements, testName(statements, field, adapter), field);
	}

	private static void run(Imperatives statements, String name, Field field) {
		if (field.isPrototype()) {
			statements.expression(new RunTest(
					statements,
					statements.nextDistributor(),
					name,
					field.getKey()).toRef());
			return;
		}

		final Path testPath = field.getKey().toPath();
		final Scope localScope = statements.getScope();
		final Path pathFromLocal =
				localScope.getEnclosingScopePath().append(testPath);

		statements.expression(
				pathFromLocal.bind(statements, statements.getScope())
				.target(statements.nextDistributor()));
	}

	private static String testName(
			Imperatives statements,
			Field field,
			Obj test) {

		final Obj nameObject =
				test.member(NAME_MEMBER).substance(dummyUser()).toObject();
		final Value<?> nameValue = nameObject.value().getValue();

		if (!nameValue.getKnowledge().isKnown()) {
			statements.getLogger().indefiniteValue(nameObject);
		} else {
			if (!nameValue.getKnowledge().isFalse()) {

				final String name =
						ValueType.STRING.cast(nameValue).getCompilerValue();

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
		private final String name;

		RunTest(
				LocationInfo location,
				Distributor distributor,
				String name,
				MemberKey testKey) {
			super(location, distributor);
			this.testKey = testKey;
			this.name = name;
		}

		@Override
		public TypeRef ancestor(LocationInfo location) {

			final Scope localScope = getScope();

			assert localScope.toLocal() != null :
				this + " should be inside of local scope, but is inside of "
				+ localScope;

			final Path objectPath = localScope.getEnclosingScopePath();
			final Path testPath = objectPath.append(this.testKey);

			return testPath.bind(location, localScope)
					.target(localScope.distribute()).toTypeRef();
		}

		@Override
		public FieldDefinition fieldDefinition(
				BoundPath path,
				Distributor distributor) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ObjectConstructor reproduce(PathReproducer reproducer) {
			return new RunTest(
					this,
					reproducer.distribute(),
					this.name,
					this.testKey);
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
