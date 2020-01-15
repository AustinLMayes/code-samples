/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.util.xml.conditionals.type;

import java.util.List;

import net.avicus.atlas.Atlas;
import net.avicus.atlas.module.checks.CheckResult;
import net.avicus.atlas.module.checks.StaticResultCheck;
import net.avicus.atlas.util.xml.conditionals.Conditional;
import org.jdom2.Element;

public class ConfigValueConditional extends Conditional {

    public ConfigValueConditional(String query, String value, List<Element> elements) {
        super(new StaticResultCheck(CheckResult.IGNORE), elements);
        super.setCheck(new StaticResultCheck(getValue(query, value)));
    }

    private CheckResult getValue(String query, String value) {
        String configRes = Atlas.get().getConfig().getString("variables." + query);
        if (configRes != null && configRes.equals(value)) {
            return CheckResult.ALLOW;
        }

        return CheckResult.DENY;
    }
}
