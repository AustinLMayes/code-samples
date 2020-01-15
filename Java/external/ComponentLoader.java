/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: N/A (Private Codebase)
*/

package network.walrus.games.core.external;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import network.walrus.games.core.GamesPlugin;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;

/**
 * Class to handle the loading and unloading of external components.
 *
 * @author Austin Mayes
 */
public class ComponentLoader {

  private File directory;
  private List<ExternalComponentInfo> loadedComponents;

  /**
   * Constructor.
   *
   * @param directory containing all of the components to load
   */
  public ComponentLoader(File directory) {
    this.directory = directory;
    this.loadedComponents = Lists.newArrayList();

    if (!directory.exists()) {
      if (directory.mkdirs()) {
        Bukkit.getLogger()
            .info("Components folder did not exist, created one at " + directory.getPath());
      } else {
        Bukkit.getLogger().info("Failed to create components folder at " + directory.getPath());
      }
    }
  }

  public List<ExternalComponentInfo> getLoadedComponents() {
    return loadedComponents;
  }

  /** Loads the components contained within the specified directory */
  public void loadComponents() {
    Validate.notNull(directory, "Directory cannot be null");
    Validate.isTrue(directory.isDirectory(), "Directory must be a directory");

    Map<String, File> components = new HashMap<String, File>();

    // This is where it figures out all possible components
    for (File file : directory.listFiles()) {
      if (file.getName().startsWith(".")) {
        continue;
      }
      JarFile jar;
      try {
        jar = new JarFile(file, true);
      } catch (IOException ex) {
        Bukkit.getLogger()
            .severe(
                "Could not load component at "
                    + file.getPath()
                    + ": "
                    + ExceptionUtils.getMessage(ex));
        ex.printStackTrace();
        continue;
      }

      ZipEntry entry = jar.getEntry("component.yml");
      ComponentDescriptionFile descriptionFile;
      try {
        InputStream stream = jar.getInputStream(entry);
        descriptionFile = new ComponentDescriptionFile(stream);
      } catch (Exception ex) {
        Bukkit.getLogger()
            .severe(
                "Could not load component at "
                    + file.getPath()
                    + ": "
                    + ExceptionUtils.getMessage(ex));
        ex.printStackTrace();
        continue;
      }

      File replacedFile = components.put(descriptionFile.getName(), file);
      if (replacedFile != null) {
        Bukkit.getLogger()
            .severe(
                String.format(
                    "Ambiguous component name `%s' for files `%s' and `%s' in `%s'",
                    descriptionFile.getName(),
                    file.getPath(),
                    replacedFile.getPath(),
                    directory.getPath()));
      }
    }

    Iterator<String> componentIterator = components.keySet().iterator();

    while (componentIterator.hasNext()) {
      String component = componentIterator.next();
      File file = components.get(component);
      componentIterator.remove();

      try {
        loadComponent(file);
      } catch (Exception ex) {
        Bukkit.getLogger()
            .log(
                Level.SEVERE,
                "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "'",
                ex);
      }
    }
  }

  /**
   * Loads the component in the specified file
   *
   * @param file File containing the component to load
   * @return The Component loaded, or null if it was invalid
   * @throws Exception Thrown when the specified file is not a valid component
   */
  public synchronized ExternalComponentInfo loadComponent(File file) throws Exception {
    Validate.notNull(file, "File cannot be null");
    try (Timing timing =
        Timings.ofStart(GamesPlugin.instance, "Component load: " + file.getName())) {
      ExternalComponentInfo result = load(file);

      if (result != null) {
        loadedComponents.add(result);
      }
      return result;
    }
  }

  /**
   * Load the external component contained in the provided file.
   *
   * <p>The file must contain a component.yml in it's root which is used to provide general
   * information about the properties of the component.
   *
   * @param file to attempt to load a component from
   * @return information about the loaded component
   */
  private ExternalComponentInfo load(File file) {
    JarFile jar;
    try {
      jar = new JarFile(file, true);
    } catch (IOException ex) {
      Bukkit.getLogger()
          .severe(
              "Could not load component at "
                  + file.getPath()
                  + ": "
                  + ExceptionUtils.getMessage(ex));
      return null;
    }

    ZipEntry entry = jar.getEntry("component.yml");
    ComponentDescriptionFile descriptionFile;
    try {
      InputStream stream = jar.getInputStream(entry);

      descriptionFile = new ComponentDescriptionFile(stream);
    } catch (Exception ex) {
      Bukkit.getLogger()
          .severe(
              "Could not load component at "
                  + file.getPath()
                  + ": "
                  + ExceptionUtils.getMessage(ex));
      ex.printStackTrace();
      return null;
    }

    try {
      ComponentClassLoader.addFile(file);
    } catch (IOException ex) {
      Bukkit.getLogger()
          .severe(
              "Invalid component.yml for "
                  + file.getPath()
                  + ": Could not load "
                  + file.getName()
                  + " into the classpath");
      return null;
    }

    Class<?> externalComponent;
    try {
      externalComponent = Class.forName(descriptionFile.getMain());
    } catch (ClassNotFoundException ex) {
      Bukkit.getLogger()
          .severe(
              "Invalid component.yml for "
                  + file.getPath()
                  + ": "
                  + descriptionFile.getMain()
                  + " does not exist");
      return null;
    }

    if (!ExternalComponent.class.isAssignableFrom(externalComponent)) {
      Bukkit.getLogger()
          .severe(
              "Invalid component.yml for "
                  + file.getPath()
                  + ": "
                  + descriptionFile.getMain()
                  + " is not assignable from "
                  + ExternalComponent.class.getSimpleName());
      return null;
    }

    try {
      Constructor constructor = externalComponent.getDeclaredConstructor(GamesPlugin.class);
      constructor.setAccessible(true);
      ExternalComponent externalComponentInstance =
          (ExternalComponent) constructor.newInstance(GamesPlugin.instance);
      return new ExternalComponentInfo(externalComponentInstance, descriptionFile);
    } catch (Exception ex) {
      Bukkit.getLogger()
          .severe("Failed to load " + file.getPath() + ": " + ExceptionUtils.getMessage(ex));
      ex.printStackTrace();
    }

    return null;
  }

  /** Disable all loaded components. */
  public void disableAll() {
    for (ExternalComponentInfo component : loadedComponents) {
      component.getComponentInstance().onDisable();
    }
  }

  /**
   * Determine if a specific component is loaded.
   *
   * @param id to be used to check for component existence. IDs are the down cased versions of the
   *     names from component.yml with dashes instead of spaces.
   * @return if the component is loaded
   */
  public boolean hasComponent(String id) {
    for (ExternalComponentInfo i : this.loadedComponents) {
      if (i.getDescriptionFile().getName().toLowerCase().replace(" ", "-").equals(id)) {
        return true;
      }
    }
    return false;
  }

  /** Data class containing data gathered from the loading of a {@link ExternalComponent}. */
  public class ExternalComponentInfo {

    private final ExternalComponent componentInstance;
    private final ComponentDescriptionFile descriptionFile;

    /**
     * Constructor
     *
     * @param componentInstance instance of the component's main class
     * @param descriptionFile containing info about the component
     */
    ExternalComponentInfo(
        ExternalComponent componentInstance, ComponentDescriptionFile descriptionFile) {
      this.componentInstance = componentInstance;
      this.descriptionFile = descriptionFile;
    }

    public ExternalComponent getComponentInstance() {
      return componentInstance;
    }

    public ComponentDescriptionFile getDescriptionFile() {
      return descriptionFile;
    }
  }
}
