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

import static org.o42a.common.object.AnnotatedModule.moduleSources;
import static org.o42a.core.source.SectionTag.IMPLICIT_SECTION_TAG;
import static org.o42a.util.use.User.dummyUser;

import org.o42a.codegen.Generator;
import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.RelatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.common.source.TreeCompilerContext;
import org.o42a.common.source.URLSourceTree;
import org.o42a.core.Namespace;
import org.o42a.core.Scope;
import org.o42a.core.artifact.common.ObjectMemberRegistry;
import org.o42a.core.artifact.object.Ascendants;
import org.o42a.core.artifact.object.Obj;
import org.o42a.core.artifact.object.ObjectMembers;
import org.o42a.core.def.Definitions;
import org.o42a.core.ir.object.ObjectIR;
import org.o42a.core.member.field.Field;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.ModuleCompiler;
import org.o42a.core.st.Definer;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.value.ValueStruct;
import org.o42a.util.io.URLSource;


@SourcePath("root.o42a")
@RelatedSources({"number.o42a", "operators.o42a"})
public class Root extends Obj {

	public static Root createRoot(Scope topScope) {

		final AnnotatedSources sources = moduleSources(Root.class);
		final TreeCompilerContext<URLSource> context =
				sources.getSourceTree().context(topScope.getContext());

		return new Root(topScope, context.compileModule(), sources);
	}

	private final ModuleCompiler compiler;
	private final AnnotatedSources sources;

	private final VoidField voidField;
	private Obj directiveObject;
	private Obj falseObject;
	private Obj integerObject;
	private Obj floatObject;
	private Obj stringObject;
	private Obj arrayObject;
	private Obj constantArrayObject;

	private DeclarativeBlock definition;
	private ObjectMemberRegistry memberRegistry;
	private Definer definer;

	private Root(
			Scope topScope,
			ModuleCompiler compiler,
			AnnotatedSources sources) {
		super(new RootScope(compiler, topScope.distribute()));
		this.compiler = compiler;
		this.sources = sources;
		this.voidField = new VoidField(this);
		setValueStruct(ValueStruct.VOID);
	}

	public final URLSourceTree getSourceTree() {
		return this.sources.getSourceTree();
	}

	public final Field<Obj> getVoidField() {
		return this.voidField;
	}

	public final Obj getDirective() {
		if (this.directiveObject != null) {
			return this.directiveObject;
		}
		return this.directiveObject =
				field("directive").substance(dummyUser()).toObject();
	}

	public final Obj getFalse() {
		if (this.falseObject != null) {
			return this.falseObject;
		}
		return this.falseObject =
				field("false").substance(dummyUser()).toObject();
	}

	public final Obj getInteger() {
		if (this.integerObject != null) {
			return this.integerObject;
		}
		return this.integerObject =
				field("integer").substance(dummyUser()).toObject();
	}

	public final Obj getFloat() {
		if (this.floatObject != null) {
			return this.floatObject;
		}
		return this.floatObject =
				field("float").substance(dummyUser()).toObject();
	}

	public final Obj getString() {
		if (this.stringObject != null) {
			return this.stringObject;
		}
		return this.stringObject =
				field("string").substance(dummyUser()).toObject();
	}

	public final Obj getArray() {
		if (this.arrayObject != null) {
			return this.arrayObject;
		}
		return this.arrayObject =
				field("array").substance(dummyUser()).toObject();
	}

	public final Obj getConstantArray() {
		if (this.constantArrayObject != null) {
			return this.constantArrayObject;
		}
		return this.constantArrayObject =
				field("constant_array").substance(dummyUser()).toObject();
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
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(
				Path.VOID_PATH.target(
						this,
						getScope().getEnclosingScope().distribute())
						.toStaticTypeRef());
	}

	@Override
	protected void postResolve() {
		super.postResolve();

		this.memberRegistry =
				new ObjectMemberRegistry(new RootInclusions(), this);
		this.definition = new DeclarativeBlock(
				this,
				new Namespace(this, this),
				this.memberRegistry);
		this.definer = this.definition.define(definitionEnv());

		this.compiler.define(this.definition, IMPLICIT_SECTION_TAG);
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		this.memberRegistry.registerMembers(members);
		members.addMember(getVoidField().toMember());
		for (Field<?> field : this.sources.fields(toMemberOwner())) {
			members.addMember(field.toMember());
		}
	}

	@Override
	protected void updateMembers() {
		this.definition.executeInstructions();
	}

	@Override
	protected Definitions explicitDefinitions() {
		return this.definer.define(getScope());
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
