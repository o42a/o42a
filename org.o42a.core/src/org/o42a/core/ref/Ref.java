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
package org.o42a.core.ref;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.AdapterId.adapterId;
import static org.o42a.core.ref.RefUsage.TYPE_PARAMETER_REF_USAGE;
import static org.o42a.core.ref.RefUser.dummyRefUser;
import static org.o42a.core.ref.path.Path.FALSE_PATH;
import static org.o42a.core.ref.path.Path.SELF_PATH;
import static org.o42a.core.ref.path.Path.VOID_PATH;
import static org.o42a.core.ref.type.TypeRef.staticTypeRef;
import static org.o42a.core.ref.type.TypeRef.typeRef;
import static org.o42a.core.ref.type.impl.ValueTypeInterface.valueTypeInterfaceOf;
import static org.o42a.core.value.ValueAdapter.rawValueAdapter;
import static org.o42a.core.value.link.TargetRef.targetRef;

import org.o42a.analysis.Analyzer;
import org.o42a.codegen.Generator;
import org.o42a.core.Distributor;
import org.o42a.core.Scope;
import org.o42a.core.ir.op.*;
import org.o42a.core.ir.value.ValOp;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.member.field.FieldDefinition;
import org.o42a.core.member.field.MemberField;
import org.o42a.core.object.Obj;
import org.o42a.core.object.def.Definitions;
import org.o42a.core.ref.impl.RefCommand;
import org.o42a.core.ref.path.*;
import org.o42a.core.ref.path.impl.ErrorStep;
import org.o42a.core.ref.type.StaticTypeRef;
import org.o42a.core.ref.type.TypeRef;
import org.o42a.core.ref.type.TypeRefParameters;
import org.o42a.core.ref.type.impl.TargetTypeRefParameters;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.st.*;
import org.o42a.core.st.sentence.Statements;
import org.o42a.core.value.*;
import org.o42a.core.value.link.TargetRef;
import org.o42a.util.fn.Cancelable;


public class Ref extends Statement implements RefBuilder {

	public static Ref voidRef(LocationInfo location, Distributor distributor) {
		return VOID_PATH.bind(location, distributor.getScope()).target(
				distributor);
	}

	public static Ref falseRef(LocationInfo location, Distributor distributor) {
		return FALSE_PATH.bind(location, distributor.getScope()).target(
				distributor);
	}

	public static Ref errorRef(LocationInfo location, Distributor distributor) {
		return ErrorStep.ERROR_STEP.toPath().bindStatically(
				location,
				distributor.getScope())
				.target(distributor);
	}

	private final BoundPath path;
	private TypeRef iface;

	public Ref(LocationInfo location, Distributor distributor, BoundPath path) {
		super(location, distributor);
		this.path = path;
	}

	@Override
	public boolean isValid() {
		return !getResolution().isError();
	}

	public final boolean isConstant() {
		if (!isStatic()) {
			return false;
		}

		final Definitions definitions =
				getResolution().toObject().value().getDefinitions();

		return definitions.isConstant();
	}

	public final boolean isKnownStatic() {
		return getPath().isKnownStatic();
	}

	public final boolean isStatic() {
		return getPath().isStatic();
	}

	public final BoundPath getPath() {
		return this.path;
	}

	public final ValueType<?> getValueType() {

		final TypeParameters<?> typeParameters = typeParameters(getScope());

		return typeParameters != null ? typeParameters.getValueType() : null;
	}

	public final Ref setLocation(LocationInfo location) {
		return getPath().setLocation(location).target(distribute());
	}

	public final TypeParameters<?> typeParameters(Scope scope) {

		final Resolution resolution = resolve(scope.resolver());
		final TypeParameters<?> typeParameters =
				resolution.toObject().type().getParameters();

		return typeParameters.prefixWith(getPath().toPrefix(scope));
	}

	public final TypeRefParameters typeParameters() {
		return new TargetTypeRefParameters(this);
	}

	public final Resolution getResolution() {
		return resolve(getScope().resolver());
	}

	public final Ref dereference() {
		return getPath().dereference().target(distribute());
	}

	@Override
	public final Command command(CommandEnv env) {
		return new RefCommand(this, env);
	}

	public final Definitions toDefinitions(CommandEnv env) {
		return new RefCommand(this, env).createDefinitions();
	}

	public final Resolution resolve(Resolver resolver) {
		assertCompatible(resolver.getScope());
		return new Resolution(this, resolver);
	}

	public final Resolution resolveAll(FullResolver resolver) {
		assertCompatible(resolver.getScope());

		final Resolution resolution =
				resolve(resolver.getResolver()).resolveAll(resolver);

		if (this.iface != null && !this.iface.isFullyResolved()) {
			this.iface.resolveAll(
					getScope()
					.resolver()
					.fullResolver(dummyRefUser(), TYPE_PARAMETER_REF_USAGE));
		}

		return resolution;
	}

	public final Value<?> getValue() {
		return value(getScope().resolver());
	}

	public Value<?> value(Resolver resolver) {
		return resolve(resolver)
				.toObject()
				.value()
				.getValue()
				.prefixWith(getPath().toPrefix(resolver.getScope()));
	}

	public final ValueAdapter valueAdapter(ValueRequest request) {
		if (!request.isValueExpected()) {
			return rawValueAdapter(this);
		}

		final ValueAdapter adapter = requestValueAdapter(request);

		if (adapter == null) {
			request.getLogger().incompatible(
					getLocation(),
					request.getExpectedParameters());
			return rawValueAdapter(errorRef(this, distribute()));
		}

		return adapter;
	}

	private ValueAdapter requestValueAdapter(ValueRequest request) {

		final Step lastStep = getPath().lastStep();

		if (lastStep == null) {
			return typeParameters(getScope()).valueAdapter(this, request);
		}

		return lastStep.valueAdapter(this, request);
	}

	/**
	 * Builds ancestor reference.
	 *
	 * <p>This returns an ancestor of object or interface of the link.</p>
	 *
	 * <p>If this reference is an object constructor, the ancestor should be
	 * built before object construction.</p>
	 *
	 * @param location the location of caller.
	 *
	 * @return ancestor reference or <code>null</code> if can not be determined.
	 */
	public final TypeRef ancestor(LocationInfo location) {

		final RefPath path = getPath();

		return path.ancestor(location, this);
	}

	/**
	 * A referred object's interface.
	 *
	 * <p>This interface is the same as the one used when constructing a link
	 * with this reference as definition.</p>
	 *
	 * @return ancestor interface type reference.
	 */
	public final TypeRef getInterface() {
		if (this.iface != null) {
			return this.iface;
		}

		final RefPath path = getPath();

		return this.iface = path.iface(this, false);
	}

	public final TypeRef getValueTypeInterface() {
		return valueTypeInterfaceOf(this);
	}

	/**
	 * A referred object's interface built for rebuilt path.
	 *
	 * @return ancestor interface type reference.
	 */
	public final TypeRef rebuiltInterface() {

		final RefPath path = getPath();

		return path.iface(this, true);
	}

	@Override
	public final Ref buildRef(Distributor distributor) {
		return rescope(distributor.getScope());
	}

	@Override
	public Ref reproduce(Reproducer reproducer) {
		assertCompatible(reproducer.getReproducingScope());

		final BoundPath path = getPath();

		if (path.isSelf()) {
			return reproducer.getPhrasePrefix();
		}

		final PathReproducer pathReproducer = path.reproducer(reproducer);
		final PathReproduction pathReproduction =
				pathReproducer.reproducePath();

		if (pathReproduction == null) {
			return null;
		}
		if (pathReproduction.isUnchanged()) {
			if (!reproducer.isTopLevel()) {
				return reproducePart(
						reproducer,
						pathReproduction.getExternalPath());
			}
			// Top-level reproducer`s scope is not compatible with path
			// and requires rescoping.
			return startWithPrefix(
					reproducer,
					pathReproduction,
					reproducer.getPhrasePrefix()
					.getPath());
		}

		if (!pathReproduction.isOutOfClause()) {
			return reproducePart(
					reproducer,
					pathReproduction.getReproducedPath());
		}

		return startWithPrefix(
				reproducer,
				pathReproduction,
				pathReproduction.getReproducedPath()
				.bind(this, reproducer.getScope())
				.append(reproducer.getPhrasePrefix().getPath()));
	}

	public final InlineValue inline(Normalizer normalizer, Scope origin) {

		final NormalPath normalPath = getPath().normalize(normalizer, origin);

		if (!normalPath.isNormalized()) {
			return null;
		}

		return new InlineRef(normalPath);
	}

	public final void normalize(Analyzer analyzer) {
		getPath().normalizePath(analyzer);
	}

	public final Ref toStatic() {
		if (isKnownStatic()) {
			return this;
		}
		return getPath().toStatic().target(distribute());
	}

	public final Ref adapt(LocationInfo location, StaticTypeRef adapterType) {

		final Resolution resolution = getResolution();

		if (resolution.isNone()) {
			return this;
		}

		final Obj object = resolution.toObject();
		final Obj adaptTo = adapterType.getType();
		final Path adapterPath = adapt(object, adaptTo, true);

		if (adapterPath == null) {
			return null;
		}
		if (adapterPath.isSelf()) {
			return this;
		}

		return getPath().append(adapterPath).target(location, distribute());
	}

	public final Ref prefixWith(PrefixPath prefix) {
		if (prefix.emptyFor(this)) {
			return this;
		}

		final BoundPath path = getPath().prefixWith(prefix);

		return path.target(distributeIn(prefix.getStart().getContainer()));
	}

	public final Ref upgradeScope(Scope toScope) {

		final BoundPath oldPath = getPath();
		final BoundPath newPath = oldPath.upgradeScope(toScope);

		if (oldPath == newPath) {
			return this;
		}

		return newPath.target(distributeIn(toScope.getContainer()));
	}

	public final Ref rescope(Scope toScope) {
		if (getScope().is(toScope)) {
			return this;
		}
		return prefixWith(toScope.pathTo(getScope()));
	}

	public final Ref rebuildIn(Scope scope) {
		return getPath().rebuildIn(scope).target(
				distributeIn(scope.getContainer()));
	}

	public final TypeRef toTypeRef() {
		if (isKnownStatic()) {
			return toStaticTypeRef(null);
		}
		return toTypeRef(null);
	}

	public TypeRef toTypeRef(TypeRefParameters typeParameters) {
		return typeRef(this, typeParameters);
	}

	public final StaticTypeRef toStaticTypeRef() {
		return toStaticTypeRef(null);
	}

	public final StaticTypeRef toStaticTypeRef(
			TypeRefParameters typeParameters) {
		return staticTypeRef(this, typeParameters);
	}

	public final TargetRef toTargetRef(TypeRef typeRef) {
		return targetRef(this, typeRef);
	}

	public final Statement toCondition(Statements<?> statements) {

		final RefPath path = getPath();

		return path.toCondition(this, statements);
	}

	public final Statement toValue(
			LocationInfo location,
			Statements<?> statements) {

		final RefPath path = getPath();

		return path.toValue(location, this, statements);
	}

	public final Ref toStateful() {

		final RefPath path = getPath();

		return path.toStateful(this);
	}

	public final Ref toStateful(boolean stateful) {
		if (!stateful) {
			return this;
		}
		return toStateful();
	}

	public final FieldDefinition toFieldDefinition() {

		final RefPath path = getPath();

		return path.toFieldDefinition(this, false);
	}

	public final FieldDefinition rebuiltFieldDefinition() {

		final RefPath path = getPath();

		return path.toFieldDefinition(this, true);
	}

	public final Ref consume(Consumer consumer) {

		final RefPath path = getPath();

		return path.consume(this, consumer);
	}

	public final RefIR ir(Generator generator) {
		assert assertFullyResolved();
		return new RefIR(generator, this);
	}

	public final RefOp op(HostOp host) {
		return new RefOp(host, this);
	}

	@Override
	public String toString() {
		if (this.path == null) {
			return super.toString();
		}
		return this.path.toString();
	}

	final void refFullyResolved() {
		fullyResolved();
	}

	private Path adapt(Obj object, Obj adaptTo, boolean dereference) {
		if (object.type().derivedFrom(adaptTo.type())) {
			return SELF_PATH;
		}

		final Path memberAdapter = adaptByMember(object, adaptTo);

		if (memberAdapter != null) {
			return memberAdapter;
		}
		if (dereference && object.type().getValueType().isLink()) {
			return adaptLink(object, adaptTo);
		}

		return null;
	}

	private Path adaptByMember(Obj object, Obj target) {

		final Member adapterMember = object.member(adapterId(target));

		if (adapterMember == null) {
			return null;
		}

		final MemberKey key = adapterMember.getMemberKey();
		final MemberField adapterField = adapterMember.toField();

		if (adapterField == null) {
			return key.toPath();
		}

		// Select the adapter based on it's declaration.
		// If the adapter field was transformed to a link when overridden,
		// this fact will be ignored.
		final MemberField adapterDecl = adapterField.getFirstDeclaration();
		final Obj adapterObject = adapterDecl.substance(dummyUser());

		final int expectedLinkDepth =
				target.type().getParameters().getLinkDepth();
		final int adapterLinkDepth =
				adapterObject.type().getParameters().getLinkDepth();

		if (adapterLinkDepth - expectedLinkDepth  == 1) {
			// Adapter was declared as link.
			// Use this link's target as adapter.
			return key.toPath().dereference();
		}

		// Adapter was declared as a plain object or
		// adapter to the link of the same depth.
		// Use the field itself as adapter.
		return key.toPath();
	}

	private Path adaptLink(Obj object, Obj adaptTo) {

		final TypeParameters<?> linkParameters = object.type().getParameters();
		final Obj targetType =
				linkParameters.getValueType()
				.toLinkType()
				.interfaceRef(linkParameters)
				.getType();
		final Path targetAdapter = adapt(targetType, adaptTo, false);

		if (targetAdapter == null) {
			return null;
		}

		return SELF_PATH.dereference().append(targetAdapter);
	}

	private Ref reproducePart(Reproducer reproducer, Path path) {
		return path.bind(this, reproducer.getScope()).target(
				reproducer.distribute());
	}

	private Ref startWithPrefix(
			Reproducer reproducer,
			PathReproduction pathReproduction,
			BoundPath phrasePrefix) {

		final Path externalPath = pathReproduction.getExternalPath();

		if (externalPath.isSelf()) {
			return phrasePrefix.target(reproducer.distribute());
		}

		return phrasePrefix.append(externalPath)
				.target(reproducer.distribute());
	}

	private static final class InlineRef extends InlineValue {

		private final NormalPath normalPath;

		InlineRef(NormalPath normalPath) {
			super(null);
			this.normalPath = normalPath;
		}

		@Override
		public void writeCond(CodeDirs dirs, HostOp host) {
			this.normalPath.writeCond(dirs, host);
		}

		@Override
		public ValOp writeValue(ValDirs dirs, HostOp host) {
			return this.normalPath.writeValue(dirs, host);
		}

		@Override
		public String toString() {
			if (this.normalPath == null) {
				return super.toString();
			}
			return this.normalPath.toString();
		}

		@Override
		protected Cancelable cancelable() {
			return null;
		}

	}

}
