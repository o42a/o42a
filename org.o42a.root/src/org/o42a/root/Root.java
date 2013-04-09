/*
    Root Object Definition
    Copyright (C) 2010-2013 Ruslan Lopatin

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
package org.o42a.root;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.common.object.AnnotatedModule.moduleSources;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.core.ref.Ref.voidRef;
import static org.o42a.core.source.SectionTag.IMPLICIT_SECTION_TAG;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import org.o42a.common.object.AnnotatedSources;
import org.o42a.common.object.RelatedSources;
import org.o42a.common.object.SourcePath;
import org.o42a.common.source.TreeCompilerContext;
import org.o42a.common.source.URLSourceTree;
import org.o42a.core.Namespace;
import org.o42a.core.Scope;
import org.o42a.core.member.MemberName;
import org.o42a.core.member.field.Field;
import org.o42a.core.object.Obj;
import org.o42a.core.object.ObjectMembers;
import org.o42a.core.object.common.ObjectMemberRegistry;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.object.def.DefinitionsBuilder;
import org.o42a.core.object.meta.Nesting;
import org.o42a.core.object.type.Ascendants;
import org.o42a.core.ref.path.Path;
import org.o42a.core.source.ModuleCompiler;
import org.o42a.core.st.sentence.DeclarativeBlock;
import org.o42a.core.value.ValueType;
import org.o42a.util.io.URLSource;


@SourcePath("root.o42a")
@RelatedSources({
	"assignable.o42a",
	"number.o42a",
	"indexed.o42a",
	"property.o42a"
})
public class Root extends Obj {

	private static final MemberName DIRECTIVE_MEMBER =
			fieldName(CASE_INSENSITIVE.canonicalName("directive"));
	private static final MemberName MACRO_MEMBER =
			fieldName(CASE_INSENSITIVE.canonicalName("macro"));
	private static final MemberName INTEGER_MEMBER =
			fieldName(CASE_INSENSITIVE.canonicalName("integer"));
	private static final MemberName FLOAT_MEMBER =
			fieldName(CASE_INSENSITIVE.canonicalName("float"));
	private static final MemberName STRING_MEMBER =
			fieldName(CASE_INSENSITIVE.canonicalName("string"));
	private static final MemberName LINK_MEMBER =
			fieldName(CASE_INSENSITIVE.canonicalName("link"));
	private static final MemberName VARIABLE_MEMBER =
			fieldName(CASE_INSENSITIVE.canonicalName("variable"));
	private static final MemberName ARRAY_MEMBER =
			fieldName(CASE_INSENSITIVE.canonicalName("array"));
	private static final MemberName ROW_MEMBER =
			fieldName(CASE_INSENSITIVE.canonicalName("row"));

	public static Root createRoot(Scope topScope) {

		final AnnotatedSources sources = moduleSources(Root.class);
		final TreeCompilerContext<URLSource> context =
				sources.getSourceTree().context(topScope.getContext());

		return new Root(topScope, context.compileModule(), sources);
	}

	private final ModuleCompiler compiler;
	private final AnnotatedSources sources;

	private Obj directiveObject;
	private Obj macroObject;
	private Obj integerObject;
	private Obj floatObject;
	private Obj stringObject;
	private Obj linkObject;
	private Obj variableObject;
	private Obj arrayObject;
	private Obj rowObject;

	private DeclarativeBlock definition;
	private ObjectMemberRegistry memberRegistry;
	private DefinitionsBuilder definitionsBuilder;

	private Root(
			Scope topScope,
			ModuleCompiler compiler,
			AnnotatedSources sources) {
		super(new RootScope(compiler, topScope.distribute()));
		this.compiler = compiler;
		this.sources = sources;
		setValueType(ValueType.VOID);
	}

	public final URLSourceTree getSourceTree() {
		return this.sources.getSourceTree();
	}

	public final Obj getDirective() {
		if (this.directiveObject != null) {
			return this.directiveObject;
		}
		return this.directiveObject =
				member(DIRECTIVE_MEMBER).substance(dummyUser()).toObject();
	}

	public final Obj getMacro() {
		if (this.macroObject != null) {
			return this.macroObject;
		}
		return this.macroObject =
				member(MACRO_MEMBER).substance(dummyUser()).toObject();
	}

	public final Obj getInteger() {
		if (this.integerObject != null) {
			return this.integerObject;
		}
		return this.integerObject =
				member(INTEGER_MEMBER).substance(dummyUser()).toObject();
	}

	public final Obj getFloat() {
		if (this.floatObject != null) {
			return this.floatObject;
		}
		return this.floatObject =
				member(FLOAT_MEMBER).substance(dummyUser()).toObject();
	}

	public final Obj getString() {
		if (this.stringObject != null) {
			return this.stringObject;
		}
		return this.stringObject =
				member(STRING_MEMBER).substance(dummyUser()).toObject();
	}

	public final Obj getLink() {
		if (this.linkObject != null) {
			return this.linkObject;
		}
		return this.linkObject =
				member(LINK_MEMBER).substance(dummyUser()).toObject();
	}

	public final Obj getVariable() {
		if (this.variableObject != null) {
			return this.variableObject;
		}
		return this.variableObject =
				member(VARIABLE_MEMBER).substance(dummyUser()).toObject();
	}

	public final Obj getArray() {
		if (this.arrayObject != null) {
			return this.arrayObject;
		}
		return this.arrayObject =
				member(ARRAY_MEMBER).substance(dummyUser()).toObject();
	}

	public final Obj getRow() {
		if (this.rowObject != null) {
			return this.rowObject;
		}
		return this.rowObject =
				member(ROW_MEMBER).substance(dummyUser()).toObject();
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
	protected Nesting createNesting() {
		return Nesting.NO_NESTING;
	}

	@Override
	protected Ascendants buildAscendants() {
		return new Ascendants(this).setAncestor(voidRef(
				this,
				getScope().getEnclosingScope().distribute()).toStaticTypeRef());
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
		this.definitionsBuilder = this.definition.definitions(definitionEnv());

		this.compiler.define(this.definition, IMPLICIT_SECTION_TAG);
	}

	@Override
	protected void declareMembers(ObjectMembers members) {
		this.memberRegistry.registerMembers(members);
		for (Field field : this.sources.fields(this)) {
			members.addMember(field.toMember());
		}
	}

	@Override
	protected void updateMembers() {
		this.definition.executeInstructions();
	}

	@Override
	protected Definitions explicitDefinitions() {
		return this.definitionsBuilder.buildDefinitions();
	}

}
