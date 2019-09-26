package org.zharov.thermal_circuit_simulation.TCSimulation;

import org.zharov.thermal_circuit_simulation.Elements.SteamGenerator;

public class TCSimulation {
    //Calculation type
    public static final int FOR_A_GIVEN_POWER = 0;
    public static final int FOR_A_GIVEN_CONSUMPTION = 1;
    private int calculationType;
    private double powerOrConsumptionValue;


    private Graph graph;
    private ThermalEfficiencyIndicators thermalEfficiencyIndicators;

    public TCSimulation(int calculationType, double powerOrConsumptionValue, double generatorEfficiency, double mechanicalEfficiencyOfTurbogenerator, Graph graph) {
        this.powerOrConsumptionValue = powerOrConsumptionValue;
        this.graph = graph;
        this.calculationType = calculationType;
        thermalEfficiencyIndicators = new ThermalEfficiencyIndicators(generatorEfficiency, mechanicalEfficiencyOfTurbogenerator);
    }

    public void start() {
        if (calculationType == FOR_A_GIVEN_CONSUMPTION) {
            SteamGenerator steamGenerator = (SteamGenerator) graph.getVertexList().get(0).element;
            steamGenerator.setSteamConsumption(powerOrConsumptionValue);

        }
    }

    public Graph getGraph() {
        return graph;
    }

    public ThermalEfficiencyIndicators getThermalEfficiencyIndicators() {
        return thermalEfficiencyIndicators;
    }
}
