package network.walrus.ubiquitous.bukkit.chat.filter;

import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import network.walrus.ubiquitous.bukkit.UbiquitousPermissions;

/**
 * Filter which replaces a set of words with a random replacement.
 *
 * @author Austin Mayes
 */
public class WordFilter extends ChatFilter {

  private static final Random RANDOM = new Random();
  private static final String[] REPLACEMENTS =
      new String[]{
          "Gardyloo",
          "Taradiddle",
          "Snickersnee",
          "Collywobbles",
          "Lollygag",
          "Malarkey",
          "Wabbit",
          "Quire",
          "Zoanthropy",
          "Bumfuzzle",
          "Gobbledygook",
          "Dongle",
          "Pronk",
          "Abear",
          "Oxter",
          "Fartlek",
          "Popple"
      };
  private final Set<String> words;

  /**
   * @param words to replace
   */
  public WordFilter(Set<String> words) {
    super(UbiquitousPermissions.WORDS_FILTER_BYPASS);
    this.words = words;
  }

  @Override
  public String filter(String original) {
    String res = original;
    for (String word : words) {
      res =
          original.replaceAll(
              "(?i)" + Pattern.quote(word), REPLACEMENTS[RANDOM.nextInt(REPLACEMENTS.length - 1)]);
    }
    return res;
  }
}
