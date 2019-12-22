package org.zharov.thermal_circuit_simulation.Elements;

import org.zharov.thermal_circuit_simulation.TCSimulation.Graph;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.MatrixCompilation;
import org.zharov.thermal_circuit_simulation.TCSimulation.ThermalEfficiencyIndicators;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.Calculation;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.Describable;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.Matrices;

public abstract class Element implements Describable, Calculation, MatrixCompilation {
    public final String name;
    public Element(String name) {
        this.name = name;
    }

    @Override
    public void describe() {
        System.out.println("Параметры " + name + " :");
        System.out.println("------------------------------------------------------------------------------------------------------------------");
    }

    @Override
    public void calculationOfInitialParameters(int v, Graph theGraph) {
    }

    @Override
    public void setSelectionNumber(int selectionNumber) {
    }

    @Override
    public void calculationOfThermalEfficiencyIndicators(int v, ThermalEfficiencyIndicators thermalEfficiencyIndicators, Graph theGraph) {
    }

    @Override
    public void matrixCompilation(int v, Matrices matrices, Graph theGraph) {
    }
}

