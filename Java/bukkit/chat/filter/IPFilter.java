/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: N/A (Private Codebase)
*/

package network.walrus.ubiquitous.bukkit.chat.filter;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import network.walrus.ubiquitous.bukkit.UbiquitousPermissions;

/**
 * Filter which removes domains and IPs from chat messages.
 *
 * @author Austin Mayes
 */
public class IPFilter extends ChatFilter {

    private final Pattern URL_PATTERN =
            Pattern.compile(
                    "(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]");
    private final Set<String> allowedDomains;

    /**
     * Constructor
     *
     * @param allowedDomains domains which are not filtered out
     */
    public IPFilter(Set<String> allowedDomains) {
        super(UbiquitousPermissions.IP_FILTER_BYPASS);
        this.allowedDomains = allowedDomains;
    }

    @Override
    public String filter(String original) {
        String res = original;
        Matcher matcher = URL_PATTERN.matcher(original);
        while (matcher.find()) {
            String url = matcher.group();
            for (String domain : allowedDomains) {
                if (!url.toLowerCase().contains(domain)) {
                    res = original.replaceAll("(?i)" + Pattern.quote(url), "[REDACTED URL]");
                }
            }
        }
        return res;
    }
}
