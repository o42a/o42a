package org.o42a.core.ref.impl.prediction;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.o42a.core.Scope;
import org.o42a.core.ref.Predicted;
import org.o42a.core.ref.Prediction;


public class CompatiblePrediction extends Prediction {

	private final Prediction filtered;

	public CompatiblePrediction(Scope scope, Prediction filtered) {
		super(scope);
		this.filtered = filtered;
	}

	@Override
	public final Predicted getPredicted() {
		return this.filtered.getPredicted();
	}

	@Override
	public Iterator<Scope> iterator() {
		return new Iter(getScope(), this.filtered);
	}

	@Override
	public String toString() {
		if (this.filtered == null) {
			return super.toString();
		}
		return "(" + getScope() + ") " + this.filtered;
	}

	private static final class Iter implements Iterator<Scope> {

		private final Scope scope;
		private final Iterator<Scope> i;
		private Scope next;

		Iter(Scope scope, Prediction prediction) {
			this.scope = scope;
			this.i = prediction.iterator();
		}

		@Override
		public boolean hasNext() {
			return this.next != null || findNext();
		}

		@Override
		public Scope next() {
			if (this.next == null) {
				if (!findNext()) {
					throw new NoSuchElementException();
				}
			}

			final Scope next = this.next;

			this.next = null;

			return next;
		}

		@Override
		public void remove() {
			this.i.remove();
		}

		private boolean findNext() {
			while (this.i.hasNext()) {

				final Scope next = this.i.next();

				if (next.derivedFrom(this.scope)) {
					this.next = next;
					return true;
				}
			}

			return false;
		}

	}

}
