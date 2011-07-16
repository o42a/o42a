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

import static org.o42a.common.object.CompiledObject.compileField;
import static org.o42a.core.member.Inclusions.noInclusions;
import static org.o42a.util.log.Logger.DECLARATION_LOGGER;

import java.net.MalformedURLException;
import java.net.URL;

import org.o42a.codegen.Generator;
import org.o42a.common.object.ValueTypeObject;
import org.o42a.common.source.SingleURLSource;
import org.o42a.common.source.URLCompilerContext;
import org.o42a.common.source.URLSourceTree;
import org.o42a.core.Scope;
import org.o42a.core.artifact.object.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.Location;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.numeric.Floats;
import org.o42a.intrinsic.numeric.Integers;
import org.o42a.intrinsic.string.StringValueTypeObject;
import org.o42a.intrinsic.string.Strings;


public class Root extends Obj {

	public static final URLSourceTree ROOT =
			new SingleURLSource("ROOT", base(), "root.o42a");

	private static final URLSourceTree INTEGER =
			new SingleURLSource(Root.ROOT, "integer.o42a");
	private static final URLSourceTree FLOAT =
			new SingleURLSource(Root.ROOT, "float.o42a");

	public static Root createRoot(Scope topScope) {

		final URLCompilerContext context = new URLCompilerContext(
				topScope.getContext(),
				"ROOT",
				base(),
				"root.o42a",
				DECLARATION_LOGGER);
		final Location location = new Location(context, context.getSource());

		return new Root(location, topScope);
	}

	private static URL base() {
		try {

			final URL self = Root.class.getResource(
					Root.class.getSimpleName() + ".class");

			return new URL(self, "../../../..");
		} catch (MalformedURLException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private final VoidField voidField;
	private final Obj falseObject;
	private final Obj include;
	private final Obj useNamespace;
	private final Obj useObject;
	private final Obj integerObject;
	private final Obj floatObject;
	private final Obj stringObject;
	private final Obj directiveObject;

	private Root(LocationInfo location, Scope topScope) {
		super(new RootScope(location, topScope.distribute()));
		setValueType(ValueType.VOID);
		this.voidField = new VoidField(this);
		this.falseObject = new False(this);
		this.include = new Include(this);
		this.useNamespace = new UseNamespace(this);
		this.useObject = new UseObject(this);
		this.integerObject = new ValueTypeObject(
				compileField(this, INTEGER),
				ValueType.INTEGER);
		this.floatObject = new ValueTypeObject(
				compileField(this, FLOAT),
				ValueType.FLOAT);
		this.stringObject = new StringValueTypeObject(this);
		this.directiveObject = new DirectiveValueTypeObject(this);
	}

	public final Field<Obj> getVoidField() {
		return this.voidField;
	}

	public final Obj getFalse() {
		return this.falseObject;
	}

	public final Obj getInteger() {
		return this.integerObject;
	}

	public final Obj getFloat() {
		return this.floatObject;
	}

	public final Obj getString() {
		return this.stringObject;
	}

	public final Obj getDirective() {
		return this.directiveObject;
	}

	@Override
	public Path scopePath() {
		return null;
	}

	@Override
	public String toString() {
		return "ROOT";
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		members.addMember(getVoidField().toMember());
		members.addMember(getFalse().toMember());
		members.addMember(getInteger().toMember());
		members.addMember(new Integers(this).toMember());
		members.addMember(getFloat().toMember());
		members.addMember(new Floats(this).toMember());
		members.addMember(getString().toMember());
		members.addMember(this.include.toMember());
		members.addMember(this.useNamespace.toMember());
		members.addMember(this.useObject.toMember());
		members.addMember(new Strings(this).toMember());
		members.addMember(new DirectiveValueTypeObject(this).toMember());

		final ObjectMemberRegistry memberRegistry =
				new ObjectMemberRegistry(noInclusions(), this);
		final BlockBuilder compiled = getContext().compileBlock();
		final DeclarativeBlock block =
				new DeclarativeBlock(this, distribute(), memberRegistry);

		compiled.buildBlock(block);
		memberRegistry.registerMembers(members);
		block.executeInstructions();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(
				getContext().getVoid().fixedRef(
						getScope().getEnclosingScope().distribute())
						.toStaticTypeRef());
	}

	@Override
	protected Definitions overrideDefinitions(
			Scope scope,
			Definitions ascendantDefinitions) {
		return ascendantDefinitions;
	}

	@Override
	protected ObjectIR createIR(Generator generator) {
		return new IR(generator, this);
	}

	private static final class IR extends ObjectIR {

		IR(Generator generator, Root root) {
			super(generator, root);
		}

		@Override
		protected void allocateData() {

			final IntrinsicsIR intrinsicsIR =
				new IntrinsicsIR((Root) getObject());

			getGenerator().newGlobal().setConstant().export().struct(
					intrinsicsIR);
			if (getGenerator().isDebug()) {
				getGenerator().newGlobal().export().struct(new DebugIR());
			}
		}

	}

}
