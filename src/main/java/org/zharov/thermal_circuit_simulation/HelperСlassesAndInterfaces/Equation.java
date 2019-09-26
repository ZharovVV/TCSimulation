package org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces;

import org.zharov.thermal_circuit_simulation.Elements.Element;

public class Equation {
    private Element element;

    public Equation(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}
