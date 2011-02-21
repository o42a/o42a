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

import static org.o42a.lib.test.run.TestRunner.runTest;

import org.o42a.core.Distributor;
import org.o42a.core.LocationSpec;
import org.o42a.core.artifact.common.DefinedObject;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.Member;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.common.ObjectConstructor;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.st.sentence.Declaratives;
import org.o42a.core.st.sentence.ImperativeSentence;
import org.o42a.core.value.ValueType;
import org.o42a.lib.test.TestModule;


final class ObjectTestsRunner extends DefinedObject {

	public static Ref runObjectTests(
			LocationSpec location,
			Distributor distributor,
			TestModule module) {
		return new RunObjectTests(location, distributor, module);
	}

	private TestModule module;

	private ObjectTestsRunner(RunObjectTests runAll) {
		super(runAll, runAll.distribute());
		setValueType(ValueType.VOID);
		this.module = runAll.module;
	}

	@Override
	public String toString() {
		return "Run tests[" + getScope().getEnclosingScope() + ']';
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(
				getValueType().typeRef(
						this,
						getScope().getEnclosingScope()));
	}

	@Override
	protected void buildDefinition(DeclarativeBlock definition) {

		final Obj object = getScope().getEnclosingContainer().toObject();

		if (object == null) {
			getLogger().error(
					"test.not_in_object",
					this,
					"Run tests directive is only allowed inside object");
			return;
		}

		runTests(definition, object);
	}

	private void runTests(DeclarativeBlock definition, Obj object) {

		final Declaratives statements =
			definition.propose(this).alternative(this);
		final ImperativeSentence sentence =
			statements.braces(this, "_tests_").propose(this);

		for (Member member : object.getMembers()) {

			final Field<?> field = member.toField();

			if (field == null) {
				continue;
			}

			runTest(this.module, sentence, field);
		}
	}

	private static final class RunObjectTests extends ObjectConstructor {

		private final TestModule module;

		RunObjectTests(
				LocationSpec location,
				Distributor distributor,
				TestModule module) {
			super(location, distributor);
			this.module = module;
		}

		@Override
		public TypeRef ancestor(LocationSpec location) {
			return ValueType.VOID.typeRef(location, getScope());
		}

		@Override
		public Ref reproduce(Reproducer reproducer) {
			return new RunObjectTests(
					this,
					reproducer.distribute(),
					this.module);
		}

		@Override
		public String toString() {
			return "Run tests[" + getScope() + ']';
		}

		@Override
		protected Obj createObject() {
			return new ObjectTestsRunner(this);
		}

	}

}
