package org.zharov.thermal_circuit_simulation.Elements.Seals;

import org.zharov.thermal_circuit_simulation.Elements.Element;
import org.zharov.thermal_circuit_simulation.Elements.Heater;
import org.zharov.thermal_circuit_simulation.Elements.Separator;
import org.zharov.thermal_circuit_simulation.Elements.TurbineCylinder;
import org.zharov.thermal_circuit_simulation.TCSimulation.Graph;
import org.zharov.thermal_circuit_simulation.TCSimulation.Vertex;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.Consumptions;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.Matrices;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.MatrixCompilation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Уплотнения штоков клапанов турбины
 */

public class ValveStemSeals  extends Element implements MatrixCompilation {
    // TODO: 26.08.2019 Костыль, можно сделать лучше...
    private HashMap<Element,Double> elementContributionToSteamConsumptionInSeals;
    private TurbineCylinder turbineCylinder;


    public ValveStemSeals(String name, HashMap<Element,Double> elementContributionToSteamConsumptionInSeals, TurbineCylinder turbineCylinder) {
        super(name);
        this.elementContributionToSteamConsumptionInSeals = elementContributionToSteamConsumptionInSeals;
        this.turbineCylinder = turbineCylinder;
    }

    public HashMap<Element, Double> getElementContributionToSteamConsumptionInSeals() {
        return elementContributionToSteamConsumptionInSeals;
    }

    @Override
    public void matrixCompilation(int v, Matrices matrices, Graph theGraph) {
        //--------------------------Инициализация-----------------------------------------------------------------------
        int nVerts = theGraph.getnVerts();
        Map<Integer, int[][]> adjMat = theGraph.getAdjMat();
        List<Vertex> vertexList = theGraph.getVertexList();
        double[][] coefficientMatrix = matrices.coefficientMatrix;
        double[] freeMemoryMatrix = matrices.freeMemoryMatrix;
        List<Consumptions> listOfConsumptions = matrices.getListOfColumnsOfConsumptions();

        //--------------------------------Связи с элементами по линии греющего пара-------------------------------------
        for (int j = 0; j < nVerts; j++) {
            int relations = adjMat.get(Graph.HEATING_STEAM)[v][j];
            if (relations == -1 || relations == 1) {
                Element element = vertexList.get(j).element;

                if (element instanceof TurbineCylinder) {
                    TurbineCylinder turbineCylinder = (TurbineCylinder) element;
                    int materialBalanceEquation = matrices.getListOfLinesOfEquations().indexOf(turbineCylinder.getMaterialBalanceEquation());
                    freeMemoryMatrix[materialBalanceEquation] += relations * elementContributionToSteamConsumptionInSeals.get(turbineCylinder);
                }

                if (element instanceof Separator) {
                    Separator separator = (Separator) element;
                    int materialBalanceEquation = matrices.getListOfLinesOfEquations().indexOf(separator.getMaterialBalanceEquation());
                    int heatBalanceEquation = matrices.getListOfLinesOfEquations().indexOf(separator.getHeatBalanceEquation());
                    freeMemoryMatrix[materialBalanceEquation] += relations * elementContributionToSteamConsumptionInSeals.get(separator);
                    freeMemoryMatrix[heatBalanceEquation] +=
                            relations * elementContributionToSteamConsumptionInSeals.get(separator) * turbineCylinder.parametersInSelection(0).getEnthalpy();

                }

                if (element instanceof Heater) {
                    Heater heater = (Heater) element;
                    if (heater.isSurfaceHeater()) {
                        int materialBalanceEquation = matrices.getListOfLinesOfEquations().indexOf(heater.getMaterialBalanceEquationOnSteamDrainLine());
                        int heatBalanceEquation = matrices.getListOfLinesOfEquations().indexOf(heater.getHeatBalanceEquation());
                        freeMemoryMatrix[materialBalanceEquation] += relations * elementContributionToSteamConsumptionInSeals.get(heater);
                        freeMemoryMatrix[heatBalanceEquation] +=
                                relations * elementContributionToSteamConsumptionInSeals.get(heater) * turbineCylinder.parametersInSelection(0).getEnthalpy();
                    } else {
                        int materialBalanceEquation = matrices.getListOfLinesOfEquations().indexOf(heater.getMaterialBalanceEquationOnHeatedMediumLine());
                        int heatBalanceEquation = matrices.getListOfLinesOfEquations().indexOf(heater.getHeatBalanceEquation());
                        freeMemoryMatrix[materialBalanceEquation] += relations * elementContributionToSteamConsumptionInSeals.get(heater);
                        freeMemoryMatrix[heatBalanceEquation] +=
                                relations * elementContributionToSteamConsumptionInSeals.get(heater) * turbineCylinder.parametersInSelection(0).getEnthalpy();
                    }
                }
            }
        }
        //--------------------------------------------------------------------------------------------------------------

    }

    @Override
    public void describe() {
        super.describe();
        for (Map.Entry<Element, Double> elementsDoubleEntry : elementContributionToSteamConsumptionInSeals.entrySet()) {
            System.out.println("Элемент схемы: " + elementsDoubleEntry.getKey().name +
                    " Расход пара из (в) уплотнения: " + elementContributionToSteamConsumptionInSeals.get(elementsDoubleEntry.getKey()));
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        System.out.println();
    }

}
