package org.zharov.thermal_circuit_simulation.Elements;

import com.hummeling.if97.IF97;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.Consumptions;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.Equation;
import org.zharov.thermal_circuit_simulation.HelperСlassesAndInterfaces.Matrices;
import org.zharov.thermal_circuit_simulation.TCSimulation.Graph;
import org.zharov.thermal_circuit_simulation.TCSimulation.Vertex;

import java.util.List;
import java.util.Map;

public class Superheater extends Element {
    //-----------------------------Характеристики подогревателя---------------------------------------------------------
    private int heaterNumber;                                   // Номер подогревателя по ходу пара
    private int selectionNumber;                                // Номер отбора
    private double hydraulicResistanceFromSelectionToHeater;    // Гидравлическое сопротивление от отбора до подогревателя
    private double hydraulicResistanceInHeater;                 // Гидравлическое сопротивление в подогревателе
    private double underheatingOfSteamDrain;                    // Недогрев дренажа (температурный напор между обогреваемой средой на входе и дренажом на выходе)
    private double underheatingOfHeatedMedium;                  // Недогрев обогреваемой среды на выходе до температуры в подогревателе
    private double coefficient;                                           // Коэффициент, учитывающий тепловые потери

    //-----------------------------Характеристики греющего пара---------------------------------------------------------
    private double pressureOfHeatingSteam;                      // Давление греющего пара на входе в подогреватель
    private double temperatureOfHeatingSteam;                   // Температура греющего пара на входе в подогреватель
    private double enthalpyOfHeatingSteam;                      // Энтальпия греющего пара на входе в подогреватель
    private Consumptions consumptionOfHeatingSteam = new Consumptions();                      // Расход греющего пара на входе в подогреватель
    //-----------------------------Характеристики дренажа пара----------------------------------------------------------
    private double pressureOfSteamDrain;                        // Давление дренажа пара на выходе из подогревателя
    private double temperatureOfSteamDrain;                     // Температура дренажа пара на выходе из подогревателя
    private double enthalpyOfSteamDrain;                        // Энтальпия дренажа пара на выходе из подогревателя
    private Consumptions consumptionOfSteamDrain = new Consumptions();                        // Расход дренажа пара на выходе из подогревателя
    //-----------------------------Характеристики обогреваемой среды на выходе------------------------------------------
    private double pressureOfHeatedMedium;                      // Давление обогреваемой среды на выходе из подогревателя
    private double temperatureOfHeatedMedium;                   // Температура обогреваемой среды на выходе из подогревателя
    private double enthalpyOfHeatedMedium;                      // Энтальпия обогреваемой среды на выходе из подогревателя
    private Consumptions consumptionOfHeatedMedium = new Consumptions();                      // Расход обогреваемой среды на выходе из подогревателя

    private Equation materialBalanceEquationOnSteamDrainLine = new Equation(this);
    private Equation materialBalanceEquationOnHeatedMediumLine = new Equation(this);
    private Equation heatBalanceEquation = new Equation(this);

    public Superheater(String name,
                       int heaterNumber,
                       double hydraulicResistanceFromSelectionToHeater,
                       double hydraulicResistanceInHeater,
                       double underheatingOfHeatedMedium) {
        super(name);
        this.heaterNumber = heaterNumber;
        this.hydraulicResistanceFromSelectionToHeater = hydraulicResistanceFromSelectionToHeater;
        this.hydraulicResistanceInHeater = hydraulicResistanceInHeater;
        this.underheatingOfSteamDrain = Double.NaN;
        this.underheatingOfHeatedMedium = underheatingOfHeatedMedium;
    }

    @Override
    public void calculationOfInitialParameters(int v, Graph theGraph) {
        //--------------------------Инициализация-----------------------------------------------------------------------
        int nVerts = theGraph.getnVerts();
        Map<Integer, int[][]> adjMat = theGraph.getAdjMat();
        List<Vertex> vertexList = theGraph.getVertexList();
        IF97 waterSteam = new IF97(IF97.UnitSystem.DEFAULT);
        //--------------------------------------------------------------------------------------------------------------

        //--------------------------------Связи с элементами по линии греющего пара-------------------------------------
        for (int j = 0; j < nVerts; j++) {
            int relations = adjMat.get(Graph.HEATING_STEAM)[v][j];
            if (relations == 1) {
                Element element = vertexList.get(j).element;

                if (element instanceof TurbineCylinder) {
                    TurbineCylinder turbineCylinder = (TurbineCylinder) element;
                    this.pressureOfHeatingSteam =                           // Считаем давление в подогревателе
                            turbineCylinder.parametersInSelection(selectionNumber).getPressure() - hydraulicResistanceFromSelectionToHeater;
                    this.pressureOfSteamDrain = pressureOfHeatingSteam;
                    this.temperatureOfHeatingSteam =                        // Температура греющего пара
                            waterSteam.saturationTemperatureP(pressureOfHeatingSteam) - 273.15;

                    this.enthalpyOfHeatingSteam =                           // Энтальпия греющего пара
                            turbineCylinder.parametersInSelection(selectionNumber).getEnthalpy();
                }
            }
        }
        //--------------------------------------------------------------------------------------------------------------

        //--------------------------------Связи с элементами по линии перегретого пара----------------------------------
        for (int j = 0; j < nVerts; j++) {
            int relations = adjMat.get(Graph.SUPERHEATED_STEAM)[v][j];
            if (relations == 1) {
                Element element = vertexList.get(j).element;

                if (element instanceof Separator) {
                    Separator separator = (Separator) element;
                    this.pressureOfHeatedMedium =                                           // Давление обогреваемой среды на выходе = давлению обогреваемой среды на выходе из предыдущего элемента - потери в подогревателе
                            separator.getPressureOfHeatedMedium() - hydraulicResistanceInHeater;
                    if (Double.isNaN(underheatingOfSteamDrain)) {                           // Если охладитель дренажа отсутствует
                        this.temperatureOfSteamDrain = temperatureOfHeatingSteam;               // Температура дренажа = температуре греющего пара
                    } else {                                                                // иначе
                        this.temperatureOfSteamDrain =                                          // Температура дренажа = температуре обогреваемой среды на выходе из предыдущего подогревателя + недогрев
                                separator.getTemperatureOfHeatedMedium() + underheatingOfSteamDrain;
                    }
                }

                if (element instanceof Superheater) {
                    Superheater superheater = (Superheater) element;
                    this.pressureOfHeatedMedium =                                           // Давление обогреваемой среды на выходе = давлению обогреваемой среды на выходе из предыдущего элемента - потери в подогревателе
                            superheater.getPressureOfHeatedMedium() - hydraulicResistanceInHeater;
                    if (Double.isNaN(underheatingOfSteamDrain)) {                           // Если охладитель дренажа отсутствует
                        this.temperatureOfSteamDrain = temperatureOfHeatingSteam;               // Температура дренажа = температуре греющего пара
                    } else {                                                                // иначе
                        this.temperatureOfSteamDrain =                                          // Температура дренажа = температуре обогреваемой среды на выходе из предыдущего подогревателя + недогрев
                                superheater.getTemperatureOfHeatedMedium() + underheatingOfSteamDrain;
                    }
                }
            }
        }
        //--------------------------------------------------------------------------------------------------------------

        this.enthalpyOfSteamDrain =                                             // Энтальпия дренажа находится по температуре дренажа на линии насыщения для воды
                waterSteam.specificEnthalpySaturatedLiquidT(temperatureOfSteamDrain + 273.15);

        this.temperatureOfHeatedMedium = temperatureOfHeatingSteam - underheatingOfHeatedMedium;
        this.enthalpyOfHeatedMedium = waterSteam.specificEnthalpyPT(pressureOfHeatedMedium, temperatureOfHeatedMedium + 273.15);
        this.coefficient = 1 - heaterNumber / 1000;
    }

    @Override
    public void setSelectionNumber(int selectionNumber) {
        this.selectionNumber = selectionNumber;
    }

    public double getPressureOfHeatedMedium() {
        return pressureOfHeatedMedium;
    }

    public double getTemperatureOfHeatedMedium() {
        return temperatureOfHeatedMedium;
    }

    public double getEnthalpyOfHeatedMedium() {
        return enthalpyOfHeatedMedium;
    }

    public double getEnthalpyOfHeatingSteam() {
        return enthalpyOfHeatingSteam;
    }

    public double getEnthalpyOfSteamDrain() {
        return enthalpyOfSteamDrain;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public int getSelectionNumber() {
        return selectionNumber;
    }

    public Consumptions getConsumptionOfHeatingSteam() {
        return consumptionOfHeatingSteam;
    }

    public Consumptions getConsumptionOfSteamDrain() {
        return consumptionOfSteamDrain;
    }

    public Consumptions getConsumptionOfHeatedMedium() {
        return consumptionOfHeatedMedium;
    }

    public Equation getMaterialBalanceEquationOnSteamDrainLine() {
        return materialBalanceEquationOnSteamDrainLine;
    }

    public Equation getMaterialBalanceEquationOnHeatedMediumLine() {
        return materialBalanceEquationOnHeatedMediumLine;
    }

    public Equation getHeatBalanceEquation() {
        return heatBalanceEquation;
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

        // Получение номера строки в матрице, в которую записывается уравнение материального баланса по линии дренажа пара для Пароперегревателя
        int materialBalanceEquationOnSteamDrainLine = matrices.getListOfLinesOfEquations().indexOf(this.getMaterialBalanceEquationOnSteamDrainLine());
        // Получение номера строки в матрице, в которую записывается уравнение материального баланса по линии обогреваемой среды для Пароперегревателя
        int materialBalanceEquationOnHeatedMediumLine = matrices.getListOfLinesOfEquations().indexOf(this.getMaterialBalanceEquationOnHeatedMediumLine());
        // Получение номера строки в матрице, в которую записывается уравнение теплового баланса для ПП
        int heatBalanceEquation = matrices.getListOfLinesOfEquations().indexOf(this.getHeatBalanceEquation());
        //--------------------------------------------------------------------------------------------------------------

        //--------------------------------Связи с элементами по линии перегретого пара----------------------------------
        for (int j = 0; j < nVerts; j++) {
            int relations = adjMat.get(Graph.SUPERHEATED_STEAM)[v][j];
            int superheaterIndexOfListConsumption = listOfConsumptions.indexOf(this.getConsumptionOfHeatedMedium());

            if (relations == -1 || relations == 1) {
                Element element = vertexList.get(j).element;

                if (element instanceof Superheater) {
                    Superheater superheater2 = (Superheater) element;
                    int indexOfListConsumption = listOfConsumptions.indexOf(superheater2.getConsumptionOfHeatedMedium());
                    if (relations == -1) {
                        coefficientMatrix[materialBalanceEquationOnHeatedMediumLine][superheaterIndexOfListConsumption] = relations;
                        coefficientMatrix[heatBalanceEquation][superheaterIndexOfListConsumption] = relations * this.getEnthalpyOfHeatedMedium();
                    } else {
                        coefficientMatrix[materialBalanceEquationOnHeatedMediumLine][indexOfListConsumption] = relations;
                        coefficientMatrix[heatBalanceEquation][indexOfListConsumption] = relations * superheater2.getEnthalpyOfHeatedMedium();
                    }
                }

                if (element instanceof TurbineCylinder) {
                    TurbineCylinder turbineCylinder = (TurbineCylinder) element;
                    coefficientMatrix[materialBalanceEquationOnHeatedMediumLine][superheaterIndexOfListConsumption] = relations;
                    if (relations == 1) {
                        coefficientMatrix[heatBalanceEquation][superheaterIndexOfListConsumption] = relations * turbineCylinder
                                .parametersInSelection(turbineCylinder.NUMBER_OF_SELECTIONS + 1).getEnthalpy();
                    } else {
                        coefficientMatrix[heatBalanceEquation][superheaterIndexOfListConsumption] = relations * this.getEnthalpyOfHeatedMedium();
                    }
                }

                if (element instanceof Separator) {
                    Separator separator = (Separator) element;
                    int indexOfListConsumption = listOfConsumptions.indexOf(separator.getConsumptionOfHeatedMedium());
                    coefficientMatrix[materialBalanceEquationOnHeatedMediumLine][indexOfListConsumption] = relations;
                    coefficientMatrix[heatBalanceEquation][indexOfListConsumption] = relations * separator.getEnthalpyOfHeatedMedium();
                }
            }
        }
        //--------------------------------------------------------------------------------------------------------------

        //--------------------------------Связи с элементами по линии греющего пара-------------------------------------
        for (int j = 0; j < nVerts; j++) {
            int relations = adjMat.get(Graph.HEATING_STEAM)[v][j];
            int superheaterIndexOfListConsumption = listOfConsumptions.indexOf(this.getConsumptionOfHeatingSteam());
            if (relations == -1 || relations == 1) {
                Element element = vertexList.get(j).element;

                if (element instanceof TurbineCylinder) {
                    coefficientMatrix[materialBalanceEquationOnSteamDrainLine][superheaterIndexOfListConsumption] = relations;
                    coefficientMatrix[heatBalanceEquation][superheaterIndexOfListConsumption] = relations * this.getEnthalpyOfHeatingSteam() * this.getCoefficient();
                }
            }
        }
        //--------------------------------------------------------------------------------------------------------------

        //--------------------------------Связи с элементами по линии дренажа греющего пара-----------------------------
        for (int j = 0; j < nVerts; j++) {
            int relations = adjMat.get(Graph.STEAM_DRAIN)[v][j];
            int superheaterIndexOfListConsumption = listOfConsumptions.indexOf(this.getConsumptionOfSteamDrain());
            if (relations == -1 || relations == 1) {
                Element element = vertexList.get(j).element;

                if (element instanceof Heater) {
                    coefficientMatrix[materialBalanceEquationOnSteamDrainLine][superheaterIndexOfListConsumption] = relations;
                    coefficientMatrix[heatBalanceEquation][superheaterIndexOfListConsumption] = relations * this.getEnthalpyOfSteamDrain() * this.getCoefficient();
                }

                if (element instanceof Deaerator) {
                    coefficientMatrix[materialBalanceEquationOnSteamDrainLine][superheaterIndexOfListConsumption] = relations;
                    coefficientMatrix[heatBalanceEquation][superheaterIndexOfListConsumption] = relations * this.getEnthalpyOfSteamDrain() * this.getCoefficient();
                }
            }
        }
        //--------------------------------------------------------------------------------------------------------------
    }

    @Override
    public void describe() {
        super.describe();
        System.out.println("-----------------------------Характеристики подогревателя---------------------------------------------------------");
        System.out.println("Гидравлическое сопротивление от отбора до подогревателя: " + hydraulicResistanceFromSelectionToHeater + " ,МПа");
        System.out.println("Гидравлическое сопротивление в подогревателе: " + hydraulicResistanceInHeater + " ,МПа");
        System.out.println("Недогрев дренажа (температурный напор между обогреваемой средой на входе и дренажом на выходе): " + underheatingOfSteamDrain + " ,℃");
        System.out.println("Недогрев обогреваемой среды на выходе до температуры в подогревателе: " + underheatingOfHeatedMedium + " ,℃");
        System.out.println("-----------------------------Характеристики греющего пара---------------------------------------------------------");
        System.out.println("Давление греющего пара на входе в подогреватель: " + pressureOfHeatingSteam + " ,МПа");
        System.out.println("Температура греющего пара на входе в подогреватель: " + temperatureOfHeatingSteam + " ,℃");
        System.out.println("Энтальпия греющего пара на входе в подогреватель: " + enthalpyOfHeatingSteam + " ,кДж/кг");
        System.out.println("Расход греющего пара: " + consumptionOfHeatingSteam.consumptionValue + " ,кг/c");
        System.out.println("-----------------------------Характеристики дренажа пара----------------------------------------------------------");
        System.out.println("Давление дренажа пара на выходе из подогревателя: " + pressureOfSteamDrain + " ,МПа");
        System.out.println("Температура дренажа пара на выходе из подогревателя: " + temperatureOfSteamDrain + " ,℃");
        System.out.println("Энтальпия дренажа пара на выходе из подогревателя: " + enthalpyOfSteamDrain + " ,кДж/кг");
        System.out.println("Расход дренажа пара на выходе из подогревателя: " + consumptionOfSteamDrain.consumptionValue + " ,кг/c");
        System.out.println("-----------------------------Характеристики обогреваемой среды на выходе------------------------------------------");
        System.out.println("Давление обогреваемой среды на выходе из подогревателя: " + pressureOfHeatedMedium + " ,МПа");
        System.out.println("Температура обогреваемой среды на выходе из подогревателя: " + temperatureOfHeatedMedium + " ,℃");
        System.out.println("Энтальпия обогреваемой среды на выходе из подогревателя: " + enthalpyOfHeatedMedium + " ,кДж/кг");
        System.out.println("Расход обогреваемой среды на выходе из подогревателя: " + consumptionOfHeatedMedium.consumptionValue + " ,кг/c");
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        System.out.println();
    }
}
