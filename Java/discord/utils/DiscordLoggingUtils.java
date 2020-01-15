package network.walrus.infrastructure.utils;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nullable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import network.walrus.infrastructure.InfrastructureConfig.Discord;
import network.walrus.infrastructure.Main;

/**
 * Various utilities for sending messages to Discord.
 *
 * @author Austin Mayes
 */
public class DiscordLoggingUtils {

  /**
   * Log an exception to the default status channel
   *
   * @param message describing why the exception was thrown
   * @param e that was thrown
   */
  public static void logException(String message, Throwable e) {
    if (!Discord.enabled() || !Main.getManager().connected) {
      return;
    }
    e.printStackTrace();

    Main.getManager()
        .getLogChannel()
        .sendMessage(
            generateRichMessage(
                Color.RED,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()),
                message,
                null,
                null,
                null,
                null,
                new MessageEmbed.Field("Exception", e.getClass() + " " + e.getMessage(), false)))
        .queue();
  }

  /**
   * Log a message to the default Discord logging channel
   *
   * @param color of the message
   * @param message to send
   */
  public static void log(Color color, String message) {
    if (!Discord.enabled() || !Main.getManager().connected) {
      System.out.println("[Discord] " + message);
      return;
    }

    Main.getManager()
        .getLogChannel()
        .sendMessage(
            generateRichMessage(
                color, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()), message))
        .queue();
  }

  /**
   * Generate a simple rich message with a color and description
   *
   * @param color of the message
   * @param description portion of the message
   * @return a message generated from the specified data
   */
  public static MessageEmbed generateRichMessage(Color color, String description) {
    return generateRichMessage(color, null, description, null, null, null, null);
  }

  /**
   * Generate a simple rich message with a color, title, and description
   *
   * @param color of the message
   * @param title at the top of the message
   * @param description portion of the message
   * @return a message generated from the specified data
   */
  public static MessageEmbed generateRichMessage(Color color, String title, String description) {
    return generateRichMessage(color, title, description, null, null, null, null);
  }

  /**
   * Generate a rich message with a color, title, description, and footer
   *
   * @param color of the message
   * @param title at the top of the message
   * @param description portion of the message
   * @param footer at the bottom of the message
   * @return a message generated from the specified data
   */
  public static MessageEmbed generateRichMessage(
      Color color, String title, String description, String footer) {
    return generateRichMessage(color, title, description, footer, null, null, null);
  }

  /**
   * Generate a simple rich message with a color, title, and link
   *
   * @param color of the message
   * @param title at the top of the message
   * @param url to embed with the message
   * @return a message generated from the specified data
   */
  public static MessageEmbed generateLinkedRichMessage(Color color, String title, String url) {
    return generateRichMessage(color, title, null, null, url, null, null);
  }

  /**
   * Generate a simple rich message with a color, title, description, and link
   *
   * @param color of the message
   * @param title at the top of the message
   * @param description portion of the message
   * @param url to embed with the message
   * @return a message generated from the specified data
   */
  public static MessageEmbed generateLinkedRichMessage(
      Color color, String title, String description, String url) {
    return generateRichMessage(color, title, description, null, url, null, null);
  }

  /**
   * Generate a rich message with a color, title, description, footer, and link
   *
   * @param color of the message
   * @param title at the top of the message
   * @param description portion of the message
   * @param footer at the bottom of the message
   * @param url to embed with the message
   * @return a message generated from the specified data
   */
  public static MessageEmbed generateLinkedRichMessage(
      Color color, String title, String description, String footer, String url) {
    return generateRichMessage(color, title, description, footer, url, null, null);
  }

  /**
   * Generate a rich message used a set of supplied data. Any fields marked as {@link Nullable} will
   * be omitted from the message generation if they are not provided.
   *
   * @param color of the message
   * @param title at the top of the message
   * @param description portion of the message
   * @param footer at the bottom of the message
   * @param url to embed with the message
   * @param imageUrl of the image embedded with the message
   * @param thumbUrl of the small image embedded with the message
   * @param fields to add to the bottom of the message above the footer
   * @return a message generated from the specified data
   */
  public static MessageEmbed generateRichMessage(
      Color color,
      @Nullable String title,
      @Nullable String description,
      @Nullable String footer,
      @Nullable String url,
      @Nullable String imageUrl,
      @Nullable String thumbUrl,
      Field... fields) {
    EmbedBuilder builder = new EmbedBuilder();

    if (title != null && url != null) builder.setTitle(title, url);
    else if (title != null) builder.setTitle(title);
    else if (url != null) throw new IllegalArgumentException("Cannot provide URL without title");

    if (description != null) builder.setDescription(description);

    if (footer != null) builder.setFooter(footer);

    if (imageUrl != null) builder.setImage(imageUrl);

    if (thumbUrl != null) builder.setThumbnail(thumbUrl);

    for (Field field : fields) {
      builder.addField(field);
    }

    builder.setColor(color);

    return builder.build();
  }
}
