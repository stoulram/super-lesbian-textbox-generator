/*
 * To the extent possible under law, the author(s) have dedicated all copyright
 * and related and neighboring rights to this software to the public domain worldwide.
 * This software is distributed without any warranty.
 *
 * A copy of the Unlicense should have been supplied as LICENSE in this repository.
 * Alternatively, you can find it at <https://unlicense.org/>.
 */

package io.github.leo40git.sltbg.app.assets.game;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import io.github.leo40git.sltbg.app.json.JsonReadUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonToken;

public final class GameFaces {
	private static final Map<String, Map<String, GameFace>> FACES = new LinkedHashMap<>();

	private GameFaces() {
		throw new UnsupportedOperationException("GameFaces only contains static declarations.");
	}

	public static void read(@NotNull Path rootPath) throws IOException {
		FACES.clear();

		try (var reader = JsonReader.json5(rootPath.resolve("faces.json5"))) {
			reader.beginObject();
			while (reader.hasNext()) {
				String category = reader.nextName();
				reader.beginObject();
				FACES.put(category, readCategory(rootPath, reader, category));
				reader.endObject();
			}
			reader.endObject();
		}
	}

	private static @NotNull Map<String, GameFace> readCategory(@NotNull Path rootPath, @NotNull JsonReader reader, @NotNull String category) throws IOException {
		var faces = new LinkedHashMap<String, GameFace>();

		String name = reader.nextName();
		String path = null;
		var tags = Set.<String>of();

		if (reader.peek() == JsonToken.STRING) {
			path = reader.nextString();
		} else {
			reader.beginObject();
			while (reader.hasNext()) {
				switch (reader.nextName()) {
					case "path" -> path = reader.nextString();
					case "tags" -> {
						tags = new HashSet<>();
						JsonReadUtils.readArray(reader, JsonReader::nextString, tags::add);
					}
					default -> reader.skipValue();
				}
			}
			reader.endObject();
		}

		if (path == null) {
			throw new IOException("%s/%s: missing path".formatted(category, name));
		}

		BufferedImage image;
		var imagePath = rootPath.resolve(path).toAbsolutePath();
		try (var imageIn = Files.newInputStream(imagePath)) {
			image = ImageIO.read(imageIn);
		} catch (IOException e) {
			throw new IOException("%s/%s: failed to read image from file '%s'".formatted(category, name, imagePath));
		}

		faces.put(name, new GameFace(category, name, tags, image));

		return faces;
	}

	public static @NotNull Map<String, Map<String, GameFace>> getAll() {
		return FACES;
	}

	public static @Nullable Map<String, GameFace> getCategory(@NotNull String category) {
		return FACES.get(category);
	}
}
