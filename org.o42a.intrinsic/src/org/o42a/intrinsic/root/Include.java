/*
    Intrinsics
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
package org.o42a.intrinsic.root;

import static org.o42a.core.member.MemberId.memberName;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.common.object.IntrinsicDirective;
import org.o42a.core.LocationInfo;
import org.o42a.core.Namespace;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ref;
import org.o42a.core.st.InstructionContext;
import org.o42a.core.st.sentence.*;
import org.o42a.core.value.Value;
import org.o42a.core.value.ValueType;


public final class Include extends IntrinsicDirective {

	private final MemberKey pathKey;

	public Include(Root root) {
		super(
				root.toMemberOwner(),
				sourcedDeclaration(
						root,
						"include",
						"root/include.o42a")
				.prototype());
		this.pathKey = memberName("path").key(getScope());
	}

	@Override
	public void apply(Ref directive, InstructionContext context) {

		final Obj object =
			directive.resolve(context.getResolver()).materialize();
		final Field<?> fileField =
			object.member(this.pathKey).toField(dummyUser());
		final Block<?> block = context.getBlock();
		final Sentence<?> sentence = block.propose(directive);
		final Value<String> value = ValueType.STRING.cast(
				fileField.getArtifact().materialize()
				.value().useBy(dummyUser()).getValue());
		final String file = value.getDefiniteValue();

		if (file == null) {
			getLogger().unresolvedValue(fileField, fileField.getDisplayName());
			return;
		}

		final LocationInfo location = directive.locationFor(file);
		final Statements<?> statements = sentence.alternative(location);
		final Block<?> destination = statements.parentheses(
				location,
				new Namespace(location, statements.getContainer()));
		final BlockBuilder builder = location.getContext().compileBlock();

		builder.buildBlock(destination);
		destination.executeInstructions();
	}

	@Override
	protected void postResolve() {
		includeSource();
		super.postResolve();
	}

}
