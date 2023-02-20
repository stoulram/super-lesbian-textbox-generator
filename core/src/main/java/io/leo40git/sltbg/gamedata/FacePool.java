/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class FacePool {
	public static final int DEFAULT_ORDER_BASE = 1000;

	public static int getNextOrder(int order) {
		order += DEFAULT_ORDER_BASE;
		int rem = order % DEFAULT_ORDER_BASE;
		if (rem > 0) {
			order += DEFAULT_ORDER_BASE - rem;
		}
		return order;
	}

	final @NotNull LinkedHashMap<String, FaceCategory> categories;
	private @Nullable FaceCategory lastCategory;
	private boolean needsSort;

	public FacePool() {
		categories = new LinkedHashMap<>();

		lastCategory = null;
		needsSort = false;
	}

	private static final ThreadLocal<ArrayList<FaceCategory>> TL_SORT_BUF = new ThreadLocal<>();

	void markDirty() {
		needsSort = true;
	}

	public void sortIfNeeded() {
		if (needsSort) {
			var sortBuf = TL_SORT_BUF.get();
			if (sortBuf == null) {
				TL_SORT_BUF.set(sortBuf = new ArrayList<>(categories.size()));
			} else {
				sortBuf.ensureCapacity(categories.size());
			}

			try {
				sortBuf.addAll(categories.values());
				sortBuf.sort(Comparator.naturalOrder());

				categories.clear();
				for (var category : sortBuf) {
					categories.put(category.getName(), category);
					category.sortIfNeeded();
				}
			} finally {
				sortBuf.clear();
			}

			needsSort = false;
		}
	}

	public @NotNull @UnmodifiableView Map<String, FaceCategory> getCategories() {
		sortIfNeeded();
		return Collections.unmodifiableMap(categories);
	}

	public @Nullable FaceCategory getCategory(@NotNull String name) {
		return categories.get(name);
	}

	public @Nullable Face getFace(@NotNull String path) {
		int delIdx = path.indexOf(Face.PATH_DELIMITER);
		if (delIdx < 0) {
			throw new IllegalArgumentException("Path \"%s\" is missing delimiter '%s'".formatted(path, Face.PATH_DELIMITER));
		}

		var category = getCategory(path.substring(0, delIdx));
		if (category == null) {
			return null;
		}

		return category.getFace(path.substring(delIdx + 1));
	}

	public void add(@NotNull FaceCategory category) {
		if (category.getPool() != null) {
			throw new IllegalArgumentException("Category is already part of other pool");
		}

		if (categories.containsKey(category.getName())) {
			throw new IllegalArgumentException("Category with name \"" + category.getName() + "\" already exists in this pool");
		}

		categories.put(category.getName(), category);
		category.setPool(this);

		if (!category.isOrderSet() && lastCategory != null) {
			category.setOrder(getNextOrder(lastCategory.getOrder()));
		}
		lastCategory = category;

		needsSort = true;
	}

	void rename(@NotNull FaceCategory category, @NotNull String newName) {
		if (categories.containsKey(newName)) {
			throw new IllegalArgumentException("Category with name \"" + newName + "\" already exists in this pool");
		}

		categories.remove(category.getName(), category);
		categories.put(newName, category);
	}

	@Contract("_, _ -> new")
	public static @NotNull FacePool read(@NotNull JsonReader reader, @NotNull Path rootDir) throws IOException {
		var pool = new FacePool();

		reader.beginObject();
		while (reader.hasNext()) {
			pool.add(FaceCategory.read(reader, rootDir));
		}
		reader.endObject();

		return pool;
	}

	public void write(@NotNull JsonWriter writer, @NotNull Path rootDir) throws IOException {
		sortIfNeeded();

		writer.beginObject();
		for (var category : categories.values()) {
			category.write(writer, rootDir);
		}
		writer.endObject();
	}
}
