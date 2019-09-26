package org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces;

import org.zharov.thermal_circuit_simulation.TCSimulation.Graph;
import org.zharov.thermal_circuit_simulation.TCSimulation.ThermalEfficiencyIndicators;

public interface Calculation {
    void calculationOfInitialParameters(int v, Graph theGraph);

    void setSelectionNumber(int selectionNumber);

    void calculationOfThermalEfficiencyIndicators(int v, ThermalEfficiencyIndicators thermalEfficiencyIndicators, Graph theGraph);
}
