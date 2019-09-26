package org.zharov.thermal_circuit_simulation;

import org.zharov.thermal_circuit_simulation.TCSimulation.Graph;
import org.zharov.thermal_circuit_simulation.TCSimulation.TCSimulation;
import org.zharov.thermal_circuit_simulation.TCSimulation.Vertex;
import org.zharov.thermal_circuit_simulation.Elements.Separator;
import org.zharov.thermal_circuit_simulation.Elements.SteamGenerator;
import org.zharov.thermal_circuit_simulation.Elements.TurbineCylinder;

public class Example {
    public static void main(String[] args) {
        Graph graph = new Graph();
        TCSimulation simulation = new TCSimulation(TCSimulation.FOR_A_GIVEN_CONSUMPTION, 1720, 0.988, 0.99, graph);

        Vertex pg = new Vertex(new SteamGenerator("ПГ"));
        graph.addVertex(pg);

        TurbineCylinder turbineCylinder_csd = new TurbineCylinder("ЦСД", 3);
        turbineCylinder_csd.addSelection(0, 5.88, 0.995);
        turbineCylinder_csd.addSelection(1, 2.982, 0.929);
        turbineCylinder_csd.addSelection(2, 1.92, 0.902);
        turbineCylinder_csd.addSelection(3, 1.203, 0.881);
        turbineCylinder_csd.addSelection(4, 1.203, 0.881);
        Vertex csd = new Vertex(turbineCylinder_csd);
        graph.addVertex(csd);

        Vertex valveStemSeal = new Vertex(null);
        Vertex turbineShaftSealForCSD = new Vertex(null);
        Vertex separator = new Vertex(new Separator("Сепаратор", 0.02, 0.999));
        graph.addVertex(separator);

    }
}
