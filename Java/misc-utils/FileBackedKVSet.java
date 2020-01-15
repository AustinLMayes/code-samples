/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: N/A (Private Codebase)
*/

package network.walrus.utils.core.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * A key->value representation of objects which can be parsed from .properties and .txt files.
 *
 * @param <V> type of value being stored for each key
 * @author Austin Mayes
 */
public abstract class FileBackedKVSet<V> {

    private final Map<String, V> data;

    /**
     * Constructor.
     */
    public FileBackedKVSet() {
        this(new HashMap<>());
    }

    /**
     * Constructor.
     *
     * @param data that makes up this collection
     */
    public FileBackedKVSet(Map<String, V> data) {
        this.data = data;
    }

    protected abstract V parse(String raw);

    /**
     * Load in strings from a collection of .properties and .txt files and add them to the collection.
     *
     * @param root path of the files
     * @throws IOException if a file fails to load
     */
    public void load(Path root) throws IOException {
        Files.walk(root)
                .filter(Files::isRegularFile)
                .forEach(
                        (f) -> {
                            String file = f.toString();
                            if (file.endsWith(".properties")) {
                                try (InputStream stream = new FileInputStream(file)) {
                                    Properties prop = new Properties();
                                    prop.load(stream);
                                    for (Entry<Object, Object> entry : prop.entrySet()) {
                                        Object k = entry.getKey();
                                        Object v = entry.getValue();
                                        V parsed = parse(v.toString());
                                        if (parsed != null) add(k.toString(), parsed);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else if (file.endsWith(".txt")) {
                                try {
                                    byte[] bytes = Files.readAllBytes(f.toAbsolutePath());
                                    String fileName = f.getFileName().toString();
                                    String key = fileName.substring(0, fileName.length() - 4);
                                    V parsed = parse(new String(bytes, StandardCharsets.UTF_8));
                                    if (parsed != null) add(key, parsed);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
    }

    /**
     * Add a value represented by a key to the collection of parsed data
     *
     * @param key   identifying the value
     * @param value to add to the collection
     */
    public void add(String key, V value) {
        this.data.put(key, value);
    }

    /**
     * Get a value from the collection corresponding to the given key, if one exists.
     *
     * @param key to search for
     * @return a value from the collection corresponding to the given key
     */
    public Optional<V> get(String key) {
        return Optional.ofNullable(data.get(key));
    }

    @Override
    public String toString() {
        return "FileBackedKVSet{" + "data=" + data + '}';
    }
}
