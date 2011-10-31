package org.o42a.core.artifact.array.impl;

import org.o42a.core.ref.Ref;
import org.o42a.core.ref.path.BoundPath;
import org.o42a.core.ref.path.PathBinding;
import org.o42a.core.ref.path.PrefixPath;
import org.o42a.core.st.Reproducer;


public class ArrayIndex implements PathBinding<Ref> {

	private final Ref indexRef;

	public ArrayIndex(Ref indexRef) {
		this.indexRef = indexRef;
	}

	@Override
	public Ref getBound() {
		return this.indexRef;
	}

	@Override
	public ArrayIndex prefixWith(PrefixPath prefix) {
		return new ArrayIndex(this.indexRef.prefixWith(prefix));
	}

	@Override
	public ArrayIndex reproduce(Reproducer reproducer) {

		final Ref indexRef = this.indexRef.reproduce(reproducer);

		if (indexRef == null) {
			return null;
		}

		return new ArrayIndex(indexRef);
	}

	public BoundPath appendToPath(BoundPath path) {
		this.indexRef.assertScopeIs(path.getOrigin());
		return path.addBinding(this).append(new ArrayElementStep(this));
	}

	@Override
	public String toString() {
		if (this.indexRef == null) {
			return super.toString();
		}
		return this.indexRef.toString();
	}

}
