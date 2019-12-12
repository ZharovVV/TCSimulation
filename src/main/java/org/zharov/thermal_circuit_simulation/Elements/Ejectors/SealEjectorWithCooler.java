package org.zharov.thermal_circuit_simulation.Elements.Ejectors;

import com.hummeling.if97.IF97;
import org.zharov.thermal_circuit_simulation.Elements.Condenser;
import org.zharov.thermal_circuit_simulation.Elements.Element;
import org.zharov.thermal_circuit_simulation.Elements.Pump;
import org.zharov.thermal_circuit_simulation.Elements.Deaerator;
import org.zharov.thermal_circuit_simulation.Elements.Seals.TurbineShaftSeals;
import org.zharov.thermal_circuit_simulation.Elements.Seals.ValveStemSeals;
import org.zharov.thermal_circuit_simulation.TCSimulation.Graph;
import org.zharov.thermal_circuit_simulation.TCSimulation.Vertex;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.Consumptions;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.Equation;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.Matrices;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.MatrixCompilation;

import java.util.List;
import java.util.Map;

/**
 * Эжектор уплотнений с охладителем пара эжектора
 */
public class SealEjectorWithCooler extends Element implements MatrixCompilation {

    private double hydraulicResistanceInCooler;                 // Гидравлическое сопротивление в охладителе пара эжектора
    private double enthalpyIncrease;                            // Повышение энтальпии
    private double inletTemperature;        // Температура конденсата на входе в охладитель пара эжектора
    private double inletPressure;           // Давление на входе в охладитель пара эжектора
    private double inletEnthalpy;           // Энтальпия на входе в охладитель пара эжектора
    private double outletTemperature;       // Температура на выходе из охладителя пара эжектора
    private double outletPressure;          // Давление на выходе из охладителя пара эжектора
    private double outletEnthalpy;          // Энтальпия на выходе из охладителя пара эжектора


    private double highPressureFlow;        // Высоконапорный поток пара (на входе в эжектор), кг/с
    private double lowPressureFlow;         // Низконапорный поток пара (на входе в эжектор), кг/с
    private double outputFlow;              // Выходной поток пара (дренажа пара после охладителя), кг/с

    private Consumptions consumptionOfWater = new Consumptions();
    private Equation materialBalanceEquation = new Equation(this);

    public SealEjectorWithCooler(String name, double hydraulicResistanceInCooler, double enthalpyIncrease, double highPressureFlow) {
        super(name);
        this.hydraulicResistanceInCooler = hydraulicResistanceInCooler;
        this.enthalpyIncrease = enthalpyIncrease;
        this.highPressureFlow = highPressureFlow;
        this.outputFlow = highPressureFlow;
    }

    @Override
    public void calculationOfInitialParameters(int v, Graph theGraph) {
        //--------------------------Инициализация-----------------------------------------------------------------------
        int nVerts = theGraph.getnVerts();
        Map<Integer, int[][]> adjMat = theGraph.getAdjMat();
        List<Vertex> vertexList = theGraph.getVertexList();
        IF97 waterSteam = new IF97(IF97.UnitSystem.DEFAULT);
        //--------------------------------------------------------------------------------------------------------------

        //--------------------------------Связи с элементами по линии питательной воды----------------------------------
        for (int j = 0; j < nVerts; j++) {
            int relations = adjMat.get(Graph.FEED_WATER)[v][j];
            if (relations == 1) {
                Element element = vertexList.get(j).element;

                if (element instanceof Pump) {
                    Pump pump = (Pump) element;
                    this.inletPressure = pump.getOutletPressure();
                    this.inletTemperature = pump.getOutletTemperature();
                    this.inletEnthalpy = pump.getOutletEnthalpy();
                }

                if (element instanceof MainEjectorWithCooler) {
                    MainEjectorWithCooler mainEjectorWithCooler = (MainEjectorWithCooler) element;
                    this.inletPressure = mainEjectorWithCooler.getOutletPressure();
                    this.inletTemperature = mainEjectorWithCooler.getOutletTemperature();
                    this.inletEnthalpy = mainEjectorWithCooler.getOutletEnthalpy();
                }
            }
        }
        //--------------------------------------------------------------------------------------------------------------

        this.outletPressure = inletPressure - hydraulicResistanceInCooler;
        this.outletEnthalpy = inletEnthalpy + enthalpyIncrease;
        this.outletTemperature = waterSteam.temperaturePH(outletPressure, outletEnthalpy) - 273.15;
    }

    public double getOutletTemperature() {
        return outletTemperature;
    }

    public double getOutletPressure() {
        return outletPressure;
    }

    public double getOutletEnthalpy() {
        return outletEnthalpy;
    }

    public Consumptions getConsumptionOfWater() {
        return consumptionOfWater;
    }

    public Equation getMaterialBalanceEquation() {
        return materialBalanceEquation;
    }

    @Override
    public void matrixCompilation(int v, Matrices matrices, Graph theGraph) {
        this.outputFlow = highPressureFlow;
        //--------------------------Инициализация-----------------------------------------------------------------------
        int nVerts = theGraph.getnVerts();
        Map<Integer, int[][]> adjMat = theGraph.getAdjMat();
        List<Vertex> vertexList = theGraph.getVertexList();
        double[][] coefficientMatrix = matrices.coefficientMatrix;
        double[] freeMemoryMatrix = matrices.freeMemoryMatrix;
        List<Consumptions> listOfConsumptions = matrices.getListOfColumnsOfConsumptions();
        // Получение номера строки в матрице, в которую записывается уравнение материального баланса для охладителя пара эжектора на линии воды
        int coolerMaterialBalanceEquationOnHeatedMediumLine = matrices.getListOfLinesOfEquations().indexOf(this.getMaterialBalanceEquation());
        //--------------------------------------------------------------------------------------------------------------

        //--------------------------------Связи с элементами по линии греющего пара-------------------------------------
        for (int j = 0; j < nVerts; j++) {
            int relations = adjMat.get(Graph.HEATING_STEAM)[v][j];
            if (relations == -1 || relations == 1) {
                Element element = vertexList.get(j).element;

                if (element instanceof Deaerator && relations == 1) {
                    Deaerator deaerator = (Deaerator) element;
                    int materialBalanceEquation = matrices.getListOfLinesOfEquations().indexOf(deaerator.getMaterialBalanceEquationOnHeatedMediumLine());
                    int heatBalanceEquation = matrices.getListOfLinesOfEquations().indexOf(deaerator.getHeatBalanceEquation());
                    IF97 waterSteam = new IF97(IF97.UnitSystem.DEFAULT);
                    freeMemoryMatrix[materialBalanceEquation] += relations * highPressureFlow;
                    freeMemoryMatrix[heatBalanceEquation] +=
                            relations * highPressureFlow *
                                    waterSteam.specificEnthalpySaturatedVapourP(deaerator.getPressureOfHeatedMedium());
                }

                if (element instanceof TurbineShaftSeals) {
                    TurbineShaftSeals turbineShaftSeal = (TurbineShaftSeals) element;
                    outputFlow += turbineShaftSeal.getElementContributionToSteamConsumptionInSeals().get(this);
                }

                if (element instanceof ValveStemSeals) {
                    ValveStemSeals valveStemSeal = (ValveStemSeals) element;
                    outputFlow += valveStemSeal.getElementContributionToSteamConsumptionInSeals().get(this);
                }

            }
        }
        //--------------------------------------------------------------------------------------------------------------

        //--------------------------------Связи с элементами по линии дренажа пара--------------------------------------
        for (int j = 0; j < nVerts; j++) {
            int relations = adjMat.get(Graph.STEAM_DRAIN)[v][j];
            if (relations == -1 || relations == 1) {
                Element element = vertexList.get(j).element;

                if (element instanceof Condenser) {
                    Condenser condenser = (Condenser) element;
                    int materialBalanceEquation = matrices.getListOfLinesOfEquations().indexOf(condenser.getMaterialBalanceEquation());
                    freeMemoryMatrix[materialBalanceEquation] += relations * outputFlow;
                }
            }
        }
        //--------------------------------------------------------------------------------------------------------------

        //--------------------------------Связи с элементами по линии питательной воды----------------------------------
        for (int j = 0; j < nVerts; j++) {
            int relations = adjMat.get(Graph.FEED_WATER)[v][j];
            // Получение номера столбца расхода воды в охладителе
            int coolerIndexOfListConsumption = listOfConsumptions.indexOf(this.getConsumptionOfWater());
            if (relations == -1 || relations == 1) {
                Element element = vertexList.get(j).element;

                if (element instanceof Pump) {
                    if (relations == -1) {
                        coefficientMatrix[coolerMaterialBalanceEquationOnHeatedMediumLine][coolerIndexOfListConsumption] = relations;
                    } else {
                        Pump pump = (Pump) element;
                        // Получение номера столбца расхода воды насоса
                        int indexOfListConsumption = listOfConsumptions.indexOf(pump.getConsumptionOfWater());
                        coefficientMatrix[coolerMaterialBalanceEquationOnHeatedMediumLine][indexOfListConsumption] = relations;
                    }
                }

                if (element instanceof MainEjectorWithCooler) {
                    MainEjectorWithCooler mainEjectorWithCooler = (MainEjectorWithCooler) element;
                    // Получение номера столбца расхода воды охладителя пара эжектора
                    int indexOfListConsumption = listOfConsumptions.indexOf(mainEjectorWithCooler.getConsumptionOfWater());
                    coefficientMatrix[coolerMaterialBalanceEquationOnHeatedMediumLine][indexOfListConsumption] = relations;
                }
            }
        }
        //--------------------------------------------------------------------------------------------------------------
    }

    @Override
    public void describe() {
        super.describe();
        System.out.println("Повышение энтальпии в в охладителе пара эжектора: " + enthalpyIncrease + " ,кДж/кг");
        System.out.println("Параметры на входе в в охладитель пара эжектора:");
        System.out.println("Давление: " + inletPressure + " ,МПа");
        System.out.println("Температура: " + inletTemperature + " ,℃");
        System.out.println("Энтальпия: " + inletEnthalpy + " ,кДж/кг");
        System.out.println();
        System.out.println("Параметры на выходе из в охладителя пара эжектора:");
        System.out.println("Давление: " + outletPressure + " ,МПа");
        System.out.println("Температура: " + outletTemperature + " ,℃");
        System.out.println("Энтальпия: " + outletEnthalpy + " ,кДж/кг");
        System.out.println("Высоконапорный поток пара (на входе в эжектор): " + highPressureFlow + " ,кг/c");
        System.out.println("Выходной поток пара (дренажа пара после охладителя): " + outputFlow + " ,кг/c");
        System.out.println();
        System.out.println("Расход воды на выходе из охладителя: " + consumptionOfWater.consumptionValue + " ,кг/с");
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        System.out.println();
    }
}
