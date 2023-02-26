/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.leo40git.sltbg.gamedata.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import io.leo40git.sltbg.gamedata.FacePool;
import io.leo40git.sltbg.json.JsonReadUtils;
import io.leo40git.sltbg.json.JsonWriteUtils;
import io.leo40git.sltbg.status.StatusTreeNode;
import io.leo40git.sltbg.status.StatusTreeNodeIcon;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

public final class FacePoolIO {
	private FacePoolIO() {
		throw new UnsupportedOperationException("FacePoolIO only contains static declarations.");
	}

	@Contract("_ -> new")
	public static @NotNull FacePool read(@NotNull JsonReader reader) throws IOException {
		var pool = new FacePool();
		JsonReadUtils.readSimpleMap(reader, FaceCategoryIO::read, pool::add);
		pool.sortIfNeeded();
		return pool;
	}

	public static void readImages(@NotNull FacePool pool, @NotNull Path rootDir) throws FacePoolIOException {
		FacePoolIOException bigExc = null;

		for (var category : pool.getCategories().values()) {
			try {
				FaceCategoryIO.readImages(category, rootDir);
			} catch (FaceCategoryIOException e) {
				if (bigExc == null) {
					bigExc = new FacePoolIOException(pool, "Failed to read all face images");
				}
				bigExc.addSubException(e);
			}
		}

		if (bigExc != null) {
			throw bigExc;
		}
	}

	public static @NotNull CompletableFuture<Void> readImagesAsync(@NotNull FacePool pool, @NotNull Path rootDir, @NotNull Executor executor) {
		var categories = pool.getCategories();
		if (categories.isEmpty()) {
			// nothing to do
			return CompletableFuture.completedFuture(null);
		}

		final var exceptions = new ConcurrentLinkedQueue<FaceCategoryIOException>();

		var futures = new CompletableFuture[categories.size()];
		int futureI = 0;
		for (var category : categories.values()) {
			futures[futureI] = FaceCategoryIO.readImagesAsync(category, rootDir, executor)
					.exceptionallyCompose(ex -> {
						if (ex instanceof FaceCategoryIOException fcioe) {
							exceptions.add(fcioe);
							return CompletableFuture.completedStage(null);
						} else {
							return CompletableFuture.failedStage(ex);
						}
					});
			futureI++;
		}

		return CompletableFuture.allOf(futures)
				.thenCompose(unused -> {
					if (exceptions.isEmpty()) {
						return CompletableFuture.completedStage(null);
					} else {
						var e = new FacePoolIOException(pool, "Failed to read all face images", exceptions);
						e.fillInStackTrace();
						return CompletableFuture.failedStage(e);
					}
				});
	}

	public static @NotNull CompletableFuture<Void> readImagesAsync(@NotNull FacePool pool, @NotNull Path rootDir, @NotNull Executor executor,
			@NotNull StatusTreeNode node) {
		var categories = pool.getCategories();
		if (categories.isEmpty()) {
			// nothing to do
			return CompletableFuture.completedFuture(null);
		}

		final var exceptions = new ConcurrentLinkedQueue<FaceCategoryIOException>();

		node.setIcon(StatusTreeNodeIcon.OPERATION_IN_PROGRESS);

		var futures = new CompletableFuture[categories.size()];
		int futureI = 0;
		for (var category : categories.values()) {
			final var child = node.addChild(StatusTreeNodeIcon.OPERATION_PENDING, category.getName());
			futures[futureI] = FaceCategoryIO.readImagesAsync(category, rootDir, executor, child)
					.exceptionallyCompose(ex -> {
						if (ex instanceof FaceCategoryIOException fcioe) {
							exceptions.add(fcioe);
							return CompletableFuture.completedStage(null);
						} else {
							return CompletableFuture.failedStage(ex);
						}
					});
			futureI++;
		}

		return CompletableFuture.allOf(futures)
				.thenCompose(unused -> {
					if (exceptions.isEmpty()) {
						node.setIcon(StatusTreeNodeIcon.OPERATION_FINISHED);
						return CompletableFuture.completedStage(null);
					} else {
						var e = new FacePoolIOException(pool, "Failed to read all face images", exceptions);
						e.fillInStackTrace();
						node.setIcon(StatusTreeNodeIcon.MESSAGE_ERROR);
						return CompletableFuture.failedStage(e);
					}
				});
	}

	public static void write(@NotNull FacePool pool, @NotNull JsonWriter writer) throws IOException {
		JsonWriteUtils.writeObject(writer, FaceCategoryIO::write, pool.getCategories().values());
	}

	public static void writeImages(@NotNull FacePool pool, @NotNull Path rootDir) throws FacePoolIOException {
		FacePoolIOException bigExc = null;

		for (var category : pool.getCategories().values()) {
			try {
				FaceCategoryIO.writeImages(category, rootDir);
			} catch (FaceCategoryIOException e) {
				if (bigExc == null) {
					bigExc = new FacePoolIOException(pool, "Failed to write all face images");
				}
				bigExc.addSubException(e);
			}
		}

		if (bigExc != null) {
			throw bigExc;
		}
	}

	public static @NotNull CompletableFuture<Void> writeImagesAsync(@NotNull FacePool pool, @NotNull Path rootDir, @NotNull Executor executor) {
		var categories = pool.getCategories();
		if (categories.isEmpty()) {
			// nothing to do
			return CompletableFuture.completedFuture(null);
		}

		final var exceptions = new ConcurrentLinkedQueue<FaceCategoryIOException>();

		var futures = new CompletableFuture[categories.size()];
		int futureI = 0;
		for (var category : categories.values()) {
			futures[futureI] = FaceCategoryIO.writeImagesAsync(category, rootDir, executor)
					.exceptionallyCompose(ex -> {
						if (ex instanceof FaceCategoryIOException fcioe) {
							exceptions.add(fcioe);
							return CompletableFuture.completedStage(null);
						} else {
							return CompletableFuture.failedStage(ex);
						}
					});
			futureI++;
		}

		return CompletableFuture.allOf(futures)
				.thenCompose(unused -> {
					if (exceptions.isEmpty()) {
						return CompletableFuture.completedStage(null);
					} else {
						var e = new FacePoolIOException(pool, "Failed to write all face images", exceptions);
						e.fillInStackTrace();
						return CompletableFuture.failedStage(e);
					}
				});
	}

	public static @NotNull CompletableFuture<Void> writeImagesAsync(@NotNull FacePool pool, @NotNull Path rootDir, @NotNull Executor executor,
			@NotNull StatusTreeNode node) {
		var categories = pool.getCategories();
		if (categories.isEmpty()) {
			// nothing to do
			return CompletableFuture.completedFuture(null);
		}

		final var exceptions = new ConcurrentLinkedQueue<FaceCategoryIOException>();

		node.setIcon(StatusTreeNodeIcon.OPERATION_IN_PROGRESS);

		var futures = new CompletableFuture[categories.size()];
		int futureI = 0;
		for (var category : categories.values()) {
			final var child = node.addChild(StatusTreeNodeIcon.OPERATION_PENDING, category.getName());
			futures[futureI] = FaceCategoryIO.writeImagesAsync(category, rootDir, executor, child)
					.exceptionallyCompose(ex -> {
						if (ex instanceof FaceCategoryIOException fcioe) {
							exceptions.add(fcioe);
							return CompletableFuture.completedStage(null);
						} else {
							return CompletableFuture.failedStage(ex);
						}
					});
			futureI++;
		}

		return CompletableFuture.allOf(futures)
				.thenCompose(unused -> {
					if (exceptions.isEmpty()) {
						node.setIcon(StatusTreeNodeIcon.OPERATION_FINISHED);
						return CompletableFuture.completedStage(null);
					} else {
						var e = new FacePoolIOException(pool, "Failed to write all face images", exceptions);
						e.fillInStackTrace();
						node.setIcon(StatusTreeNodeIcon.MESSAGE_ERROR);
						return CompletableFuture.failedStage(e);
					}
				});
	}
}
