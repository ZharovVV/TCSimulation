package org.zharov.thermal_circuit_simulation.TCSimulation;

import org.zharov.thermal_circuit_simulation.Elements.SteamGenerator;
import org.zharov.thermal_circuit_simulation.Elements.TurboDrive;

import java.util.List;

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
        SteamGenerator steamGenerator = getSteamGenerator(graph);
        TurboDrive turboDrive = getTurboDrive(graph);
        if (calculationType == FOR_A_GIVEN_CONSUMPTION) {
            steamGenerator.setSteamConsumption(powerOrConsumptionValue);
            turboDrive.setFeedwaterFlow(powerOrConsumptionValue);
            graph.startCalculation();
            graph.dfsAndCalculationOfThermalEfficiencyIndicators(thermalEfficiencyIndicators);
        } else if (calculationType == FOR_A_GIVEN_POWER) {
            //Задаем начальное значение расхода
            double steamConsumption = (1786.0 - 1720.0) / (1045.0 - 1004.0) * (powerOrConsumptionValue - 1004.0) + 1720.0;
            steamGenerator.setSteamConsumption(steamConsumption);
            turboDrive.setFeedwaterFlow(steamConsumption);
            graph.startCalculation();
            graph.dfsAndCalculationOfThermalEfficiencyIndicators(thermalEfficiencyIndicators);

            List<Double> listOfInternalCompartmentPower = thermalEfficiencyIndicators.getListOfInternalCompartmentPower();
            double w = 0.0;
            for (Double internalCompartmentPower : listOfInternalCompartmentPower) {
                w += internalCompartmentPower;
            }
            double generatorEfficiency = thermalEfficiencyIndicators.getGeneratorEfficiency();
            double mechanicalEfficiencyOfTurbogenerator = thermalEfficiencyIndicators.getMechanicalEfficiencyOfTurbogenerator();
            steamConsumption *= powerOrConsumptionValue * 1000 / (0.98 * generatorEfficiency * mechanicalEfficiencyOfTurbogenerator) / (w);

            steamGenerator.setSteamConsumption(steamConsumption);
            turboDrive.setFeedwaterFlow(steamConsumption);
            graph.startCalculation();
            thermalEfficiencyIndicators = new ThermalEfficiencyIndicators(generatorEfficiency, mechanicalEfficiencyOfTurbogenerator);
            graph.dfsAndCalculationOfThermalEfficiencyIndicators(thermalEfficiencyIndicators);

        } else {
            // TODO: 30.09.2019 Исключение
        }

    }

    public Graph getGraph() {
        return graph;
    }

    public ThermalEfficiencyIndicators getThermalEfficiencyIndicators() {
        return thermalEfficiencyIndicators;
    }

    private SteamGenerator getSteamGenerator(Graph graph) {
        List<Vertex> vertexList = graph.getVertexList();
        SteamGenerator steamGenerator = null;
        for (Vertex vertex : vertexList) {
            if (vertex.element instanceof SteamGenerator) {
                steamGenerator = (SteamGenerator) vertex.element;
            }
        }
        return steamGenerator;
    }

    private TurboDrive getTurboDrive(Graph graph) {
        List<Vertex> vertexList = graph.getVertexList();
        TurboDrive turboDrive = null;
        for (Vertex vertex : vertexList) {
            if (vertex.element instanceof TurboDrive) {
                turboDrive = (TurboDrive) vertex.element;
            }
        }
        return turboDrive;
    }
}
