package org.zharov.thermal_circuit_simulation.TCSimulation;

import org.zharov.thermal_circuit_simulation.Elements.Element;

public class Vertex {
    public Element element;
    boolean wasVisited;

    public Vertex(Element element) {
        wasVisited = false;
        this.element = element;
    }

}
