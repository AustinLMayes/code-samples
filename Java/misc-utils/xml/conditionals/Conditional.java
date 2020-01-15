/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: https://github.com/Avicus/AvicusNetwork
*/

package net.avicus.atlas.util.xml.conditionals;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.avicus.atlas.module.checks.Check;
import net.avicus.atlas.module.checks.CheckContext;
import net.avicus.atlas.module.checks.modifiers.NotCheck;
import org.jdom2.Element;

public abstract class Conditional {

    @Getter
    private final List<Element> elements;
    @Getter
    @Setter
    private Check check;

    public Conditional(Check check, List<Element> elements) {
        this.check = check;
        this.elements = elements;
    }

    public Conditional inverse() {
        this.check = new NotCheck(this.check);
        return this;
    }

    public boolean shouldExclude(CheckContext context) {
        return this.check.test(context).fails();
    }
}
