/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.util.xml.conditionals;


import java.util.ArrayList;
import java.util.List;

import net.avicus.atlas.module.checks.CheckContext;
import org.jdom2.Element;

/**
 * A single if/unless/else context.
 */
public class ConditionalContext {

    private final Conditional conditional;
    private final List<Element> elseElements;

    public ConditionalContext(Conditional conditional, List<Element> elseElements) {
        this.conditional = conditional;
        this.elseElements = elseElements;
    }

    public List<Element> getPassingElements(CheckContext context) {
        List<Element> result = new ArrayList<>();
        if (!this.conditional.shouldExclude(context)) {
            result.addAll(this.conditional.getElements());
        }

        // Didn't pass, add else
        if (result.isEmpty()) {
            result.addAll(this.elseElements);
        }

        return result;
    }
}
