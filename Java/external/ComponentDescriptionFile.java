/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: N/A (Private Codebase)
*/

package network.walrus.games.core.external;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Class to represent a file that holds basic information about an external component.
 *
 * <p>component.ymls must contain:
 *
 * <p>name - Name of the component for ID lookup and logging. main - Main class of the component
 * which must extend {@link ExternalComponent}.
 *
 * @author Austin Mayes
 */
public class ComponentDescriptionFile {

    private static final Yaml yaml = new Yaml(new SafeConstructor());
    private String main = null;
    private String name = null;

    /**
     * Constructor.
     *
     * @param stream containing the data to load
     * @throws Exception if the file contains errors
     */
    public ComponentDescriptionFile(final InputStream stream) throws Exception {
        loadMap(asMap(yaml.load(stream)));
    }

    /**
     * Constructor.
     *
     * @param reader containing the data to load
     * @throws Exception if the file contains errors
     */
    public ComponentDescriptionFile(final Reader reader) throws Exception {
        loadMap(asMap(yaml.load(reader)));
    }

    private void loadMap(Map<?, ?> map) throws Exception {
        try {
            name = map.get("name").toString();
        } catch (NullPointerException ex) {
            throw new Exception("name is not defined", ex);
        } catch (ClassCastException ex) {
            throw new Exception("name is of wrong type", ex);
        }

        try {
            main = map.get("main").toString();
        } catch (NullPointerException ex) {
            throw new Exception("main is not defined", ex);
        } catch (ClassCastException ex) {
            throw new Exception("main is of wrong type", ex);
        }
    }

    private Map<?, ?> asMap(Object object) throws Exception {
        if (object instanceof Map) {
            return (Map<?, ?>) object;
        }
        throw new Exception(object + " is not properly structured.");
    }

    public String getMain() {
        return main;
    }

    public String getName() {
        return name;
    }
}
