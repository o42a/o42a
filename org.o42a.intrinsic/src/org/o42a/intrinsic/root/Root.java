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

import static org.o42a.core.member.field.FieldDeclaration.fieldDeclaration;
import static org.o42a.util.log.Logger.DECLARATION_LOGGER;

import java.net.MalformedURLException;
import java.net.URL;

import org.o42a.codegen.code.Code;
import org.o42a.codegen.code.CodePos;
import org.o42a.common.intrinsic.IntrinsicDirective;
import org.o42a.common.intrinsic.IntrinsicObject;
import org.o42a.core.*;
import org.o42a.core.artifact.object.*;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.*;
import org.o42a.core.ir.op.RefOp;
import org.o42a.core.member.MemberId;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.Ex;
import org.o42a.core.ref.Ref;
import org.o42a.core.ref.Resolution;
import org.o42a.core.ref.path.Path;
import org.o42a.core.st.Reproducer;
import org.o42a.core.st.sentence.BlockBuilder;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.value.ValueType;
import org.o42a.intrinsic.numeric.FloatObject;
import org.o42a.intrinsic.numeric.IntegerObject;


public class Root extends Obj {

	public static Root createRoot(Scope topScope) {

		final URL base;

		try {

			final URL self =
				Root.class.getResource(Root.class.getSimpleName() + ".class");

			base = new URL(self, "../../../..");
		} catch (MalformedURLException e) {
			throw new ExceptionInInitializerError(e);
		}

		final CompilerContext context =
			topScope.getContext().urlContext(
				"ROOT",
				base,
				"root.o42a",
				DECLARATION_LOGGER);
		final Location location = new Location(context, context.getSource());

		return new Root(location, topScope);
	}

	private final VoidField voidField;
	private final IntrinsicObject falseObject;
	private final IntrinsicDirective include;
	private final UseNamespace useNamespace;
	private final UseObject useObject;
	private final IntrinsicObject integerObject;
	private final IntrinsicObject floatObject;
	private final IntrinsicObject stringObject;

	private Root(LocationSpec location, Scope topScope) {
		super(new RootScope(location, topScope.distribute()));
		setValueType(ValueType.VOID);
		this.voidField = new VoidField(this);
		this.falseObject = new False(this);
		this.include = new Include(this);
		this.useNamespace = new UseNamespace(this);
		this.useObject = new UseObject(this);
		this.integerObject = new IntegerObject(this);
		this.floatObject = new FloatObject(this);
		this.stringObject = new StringObject(this);
	}

	public final Field<Obj> getVoidField() {
		return this.voidField;
	}

	public final IntrinsicObject getFalse() {
		return this.falseObject;
	}

	public final IntrinsicObject getInteger() {
		return this.integerObject;
	}

	public final IntrinsicObject getFloat() {
		return this.floatObject;
	}

	public final IntrinsicObject getString() {
		return this.stringObject;
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
		members.addMember(getFloat().toMember());
		members.addMember(getString().toMember());
		members.addMember(this.include.toMember());
		members.addMember(this.useNamespace.toMember());
		members.addMember(this.useObject.toMember());

		final ObjectMemberRegistry memberRegistry =
			new ObjectMemberRegistry(this);
		final BlockBuilder compiled = getContext().compileBlock();
		final DeclarativeBlock block =
			new DeclarativeBlock(this, distribute(), memberRegistry);

		compiled.buildBlock(block);
		memberRegistry.registerMembers(members);
		block.executeInstructions();
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(getScope()).setAncestor(
				new RootAncestor(
						new Location(getContext(), null),
						getScope().getEnclosingScope())
				.toStaticTypeRef());
	}

	@Override
	protected Definitions overrideDefinitions(
			Scope scope,
			Definitions ascendantDefinitions) {
		return ascendantDefinitions;
	}

	private static final class VoidField extends ObjectField {

		VoidField(Root root) {
			super(fieldDeclaration(
					root,
					root.distribute(),
					MemberId.memberName("void")));
			setScopeArtifact(getContext().getVoid());
		}

		private VoidField(Container enclosingContainer, VoidField sample) {
			super(enclosingContainer, sample);
		}

		@Override
		public Obj getArtifact() {
			return getContext().getVoid();
		}

		@Override
		public String toString() {
			return "$$void";
		}

		@Override
		protected VoidField propagate(Scope enclosingScope) {
			return new VoidField(enclosingScope.getContainer(), this);
		}

	}

	private static final class RootScope extends ObjectScope {

		RootScope(LocationSpec location, Distributor enclosing) {
			super(location, enclosing);
		}

		@Override
		protected ScopeIR createIR(IRGenerator generator) {
			return new IR(generator, this);
		}

	}

	private static final class IR extends ScopeIR {

		IR(IRGenerator generator, Scope scope) {
			super(generator, scope);
		}

		@Override
		public String getId() {
			return "ROOT";
		}

		@Override
		public String prefix(IRSymbolSeparator separator, String suffix) {
			if (separator == IRSymbolSeparator.SUB) {
				return separator + suffix;
			}
			return "." + separator + suffix;
		}

		@Override
		public void allocate() {

			final Obj object = getScope().getContainer().toObject();

			object.ir(getGenerator()).getData();
		}

		@Override
		protected void targetAllocated() {
		}

		@Override
		protected HostOp createOp(CodeBuilder builder, Code code) {

			final Obj object = getScope().getContainer().toObject();

			return object.ir(getGenerator()).op(builder, code);
		}

	}

	private static final class RootAncestor extends Ex {

		RootAncestor(LocationSpec location, Scope scope) {
			super(location, scope.distribute());
		}

		@Override
		public Ref reproduce(Reproducer reproducer) {
			assertCompatible(reproducer.getReproducingScope());
			getLogger().notReproducible(this);
			return null;
		}

		@Override
		protected Resolution resolveExpression(Scope scope) {
			return objectResolution(getContext().getVoid());
		}

		@Override
		protected RefOp createOp(HostOp host) {
			return new VoidOp(host, this);
		}

	}

	private static final class VoidOp extends RefOp {

		VoidOp(HostOp host, Ref ref) {
			super(host, ref);
		}

		@Override
		public HostOp target(Code code, CodePos exit) {
			return getRef().getContext().getVoid().ir(getGenerator()).op(
					getBuilder(),
					code);
		}

		@Override
		public String toString() {
			return "void";
		}

	}

}
