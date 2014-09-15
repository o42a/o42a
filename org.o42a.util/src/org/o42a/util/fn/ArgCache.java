/*
    Utilities
    Copyright (C) 2014 Ruslan Lopatin

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
package org.o42a.util.fn;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;



public final class ArgCache<K, P, V> implements BiFunction<K, P, V> {

	public static <K, P, V> ArgCache<K, P, V> argCache(
			BiFunction<K, P, V> valueByKey) {
		return new ArgCache<>(new HashMap<>(), valueByKey);
	}

	public static <K, P, V> ArgCache<K, P, V> argCache(
			Map<K, V> cache,
			BiFunction<K, P, V> valueByKey) {
		assert cache != null :
			"Cache not specified";
		return new ArgCache<>(cache, valueByKey);
	}

	private final Map<K, V> cache;
	private final BiFunction<K, P, V> valueByKey;

	private ArgCache(Map<K, V> cache, BiFunction<K, P, V> valueByKey) {
		this.cache = cache;
		this.valueByKey = valueByKey;
	}

	public final V get(K key, P arg) {

		final V cached = this.cache.get(key);

		if (cached != null) {
			return cached;
		}

		final V value = this.valueByKey.apply(key, arg);

		this.cache.put(key, value);

		return value;
	}

	@Override
	public final V apply(K key, P arg) {
		return get(key, arg);
	}

}
