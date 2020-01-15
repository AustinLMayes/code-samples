package network.walrus.infrastructure.objects;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.util.Yaml;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import network.walrus.infrastructure.Main;

public abstract class ResourceTemplate<T> {

  private final String name;

  public ResourceTemplate(String name) {
    this.name = name;
  }

  abstract String type();

  abstract void passToCluster(T parsed, CoreV1Api kube) throws ApiException;

  public void create(Map<String, String> environment, CoreV1Api api)
      throws IOException, ApiException {
    String text =
        new String(
            Files.readAllBytes(
                Paths.get(
                    Main.getResourcesFolder().getAbsolutePath(),
                    "templates",
                    type(),
                    this.name + ".yml")),
            StandardCharsets.UTF_8);
    Pattern pattern = Pattern.compile("\\$\\{([ a-zA-Z0-9_-]{1,})\\b\\}|\\$([a-zA-Z0-9_-]{1,})\\b");
    Matcher matcher = pattern.matcher(text);
    StringBuilder builder = new StringBuilder();
    int i = 0;
    while (matcher.find()) {
      String var = matcher.group(1);
      String replacement = environment.get(var);
      builder.append(text, i, matcher.start());
      if (replacement == null) throw new IllegalArgumentException(var + " has no value!");
      else builder.append(replacement);
      i = matcher.end();
    }
    passToCluster((T) Yaml.load(builder.toString()), api);
  }
}
