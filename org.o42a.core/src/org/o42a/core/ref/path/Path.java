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
package org.o42a.core.ref.path;

import static org.o42a.analysis.use.User.dummyUser;
import static org.o42a.core.member.MemberName.fieldName;
import static org.o42a.core.ref.path.PathKind.ABSOLUTE_PATH;
import static org.o42a.core.ref.path.PathKind.RELATIVE_PATH;
import static org.o42a.util.string.Capitalization.CASE_INSENSITIVE;

import java.util.Arrays;

import org.o42a.core.Scope;
import org.o42a.core.member.Member;
import org.o42a.core.member.MemberKey;
import org.o42a.core.object.Obj;
import org.o42a.core.ref.path.impl.*;
import org.o42a.core.ref.path.impl.member.MemberFragment;
import org.o42a.core.source.CompilerContext;
import org.o42a.core.source.LocationInfo;
import org.o42a.core.value.link.impl.DereferenceStep;
import org.o42a.util.ArrayUtil;
import org.o42a.util.string.Name;


public final class Path {

	public static final Path ROOT_PATH = ABSOLUTE_PATH.emptyPath();
	public static final Path SELF_PATH = RELATIVE_PATH.emptyPath();
	public static final Path VOID_PATH =
			new Path(ABSOLUTE_PATH, true, null, new VoidStep());
	public static final Path FALSE_PATH =
			new Path(ABSOLUTE_PATH, true, null, new FalseStep());
	public static final Path NONE_PATH =
			new Path(ABSOLUTE_PATH, true, null, new NoneStep());

	public static Path absolutePath(
			CompilerContext context,
			String... fields) {

		Path path = ROOT_PATH;
		Obj object = context.getRoot();

		for (String field : fields) {

			final Member member = object.member(
					fieldName(CASE_INSENSITIVE.canonicalName(field)));

			assert member != null :
				"Field \"" + field + "\" not found in " + object;

			path = path.append(member.getMemberKey());
			object = member.substance(dummyUser()).toObject();
		}

		return path;
	}

	public static Path staticPath(Scope from, Scope to) {
		return new StaticStep(from, to).toPath();
	}

	public static Path modulePath(Name moduleName) {
		return new Path(ABSOLUTE_PATH, true, null, new ModuleStep(moduleName));
	}

	private final PathKind kind;
	private final Path template;
	private final Step[] steps;
	private final boolean isStatic;

	Path(PathKind kind, boolean isStatic, Path template, Step... steps) {
		this.kind = kind;
		this.template = template;
		this.isStatic = kind.isAbsolute() ? true : isStatic;
		this.steps = steps;
		assert stepsNotNull(steps);
		assert validTemplate();
	}

	public final PathKind getKind() {
		return this.kind;
	}

	public final boolean isAbsolute() {
		return getKind().isAbsolute();
	}

	public final boolean isStatic() {
		return this.isStatic;
	}

	public final boolean isSelf() {
		return this.steps.length == 0 && !isStatic();
	}

	/**
	 * Whether the path is a template.
	 *
	 * <p>The last step of template path is a {@link PathFragment#isTemplate()
	 * template fragment}.</p>
	 *
	 * @return <code>true</code> if this path is a template,
	 * or <code>false</code> otherwise.
	 */
	public final boolean isTemplate() {

		final int len = this.steps.length;

		if (len == 0) {
			return false;
		}

		final AbstractPathFragment fragment =
				this.steps[len - 1].getPathFragment();

		return fragment != null && fragment.isTemplate();
	}

	/**
	 * The template this path is based on.
	 *
	 * @return the path template, the path itself if it is a {@link
	 * #isTemplate() template}, or <code>null</code> if this path has no
	 * template.
	 */
	public final Path getTemplate() {
		if (this.template != null) {
			return this.template;
		}
		if (isTemplate()) {
			return this;
		}
		return null;
	}

	public final boolean hasTemplate(PathTemplate template) {

		final Path templatePath = getTemplate();

		if (template == null) {
			return false;
		}

		final int len = templatePath.steps.length;

		if (len == 0) {
			return false;
		}

		final AbstractPathFragment fragment =
				templatePath.steps[len - 1].getPathFragment();

		return fragment == template;
	}

	public final Step[] getSteps() {
		return this.steps;
	}

	public Path append(Step step) {
		assert step != null :
			"Path step not specified";
		assert getTemplate() == null :
			"Can not append to template-based path";

		final PathKind pathKind = step.getPathKind();

		if (pathKind.isAbsolute()) {
			return new Path(pathKind, true, null, step);
		}

		final Step[] newSteps = ArrayUtil.append(this.steps, step);

		return new Path(getKind(), isStatic(), null, newSteps);
	}

	public final Path append(MemberKey memberKey) {
		assert memberKey != null :
			"Member key not specified";
		return append(new MemberFragment(memberKey));
	}

	public final Path append(PathFragment fragment) {
		assert fragment != null :
			"Path fragment not specified";
		return append(fragment.toStep());
	}

	public final Path dereference() {
		return append(new DereferenceStep());
	}

	public final Path newObject(ObjectConstructor constructor) {
		return append(constructor.toStep());
	}

	public Path append(Path path) {
		assert path != null :
			"Path to append not specified";
		assert getTemplate() == null :
			"Can not append to template-based path";

		final Path oldTemplate = path.getTemplate();
		final Path newTemplate;

		if (oldTemplate == null || path.isTemplate()) {
			newTemplate = null;
		} else {
			newTemplate = append(oldTemplate);
		}

		if (path.isAbsolute()) {
			if (oldTemplate == newTemplate) {
				return path;
			}
			return new Path(ABSOLUTE_PATH, true, newTemplate, path.getSteps());
		}

		return new Path(
				getKind(),
				isStatic() || path.isStatic(),
				newTemplate,
				ArrayUtil.append(getSteps(), path.getSteps()));
	}

	public Path prefixWith(PrefixPath prefix) {
		if (prefix.isEmpty()) {
			return this;
		}
		if (isAbsolute() && getTemplate() == null) {
			return this;
		}
		return prefix.getPrefix().append(this);
	}

	public final Step lastStep() {
		if (this.steps.length == 0) {
			return null;
		}
		return this.steps[this.steps.length - 1];
	}

	public final Path cut(int stepsToCut) {

		final Step[] newSteps =
				Arrays.copyOf(this.steps, this.steps.length - stepsToCut);

		return new Path(getKind(), isStatic(), null, newSteps);
	}

	public final Path removeTemplate() {
		if (getTemplate() == null) {
			return this;
		}
		assert !isTemplate() :
			this + " is template path";
		return new Path(getKind(), isStatic(), null, getSteps());
	}

	public final BoundPath bind(LocationInfo location, Scope origin) {
		return new BoundPath(location, origin, this);
	}

	public final BoundPath bindStatically(LocationInfo location, Scope origin) {
		if (isStatic()) {
			return bind(location, origin);
		}

		final StaticStep prefix = new StaticStep(origin);
		final Path staticPath = new Path(
				getKind(),
				true,
				getTemplate(),
				ArrayUtil.prepend(prefix, getSteps()));

		return staticPath.bind(location, origin);
	}

	public final PrefixPath toPrefix(Scope start) {
		assert start != null :
			"Prefix start not specified";
		return new PrefixPath(start, this);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.steps);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final Path other = (Path) obj;

		return Arrays.equals(this.steps, other.steps);
	}

	@Override
	public String toString() {
		return toString(0);
	}

	public String toString(int length) {
		return toString(null, length);
	}

	String toString(Object origin, int length) {

		final int len;

		if (length <= 0) {
			len = this.steps.length - length;
		} else {
			len = length;
		}

		final StringBuilder out = new StringBuilder();

		if (isAbsolute()) {
			out.append("</");
		} else {
			out.append('<');
		}
		if (origin != null) {
			out.append('[').append(origin).append("] ");
		}

		for (int i = 0; i < len; ++i) {

			final Step step = this.steps[i];

			if (i != 0) {
				out.append('/');
			}

			out.append(step);
		}
		out.append('>');

		return out.toString();
	}

	boolean assertNoFragments() {
		for (Step step : getSteps()) {
			assert step.getPathFragment() == null :
				"Rebuilt path should never contain path fragments: " + this;
		}
		return true;
	}

	private static final boolean stepsNotNull(Step[] steps) {
		for (Step step : steps) {
			assert step != null :
				"Path step is null";
		}
		return true;
	}

	private final boolean validTemplate() {
		if (this.template != null) {
			assert !isTemplate() :
				"Template path can not be templated";
		}
		return true;
	}

}
