package org.zharov.thermal_circuit_simulation;

import org.zharov.thermal_circuit_simulation.Elements.*;
import org.zharov.thermal_circuit_simulation.Elements.Ejectors.MainEjectorWithCooler;
import org.zharov.thermal_circuit_simulation.Elements.Ejectors.SealEjectorWithCooler;
import org.zharov.thermal_circuit_simulation.Elements.Seals.TurbineShaftSeals;
import org.zharov.thermal_circuit_simulation.Elements.Seals.ValveStemSeals;
import org.zharov.thermal_circuit_simulation.TCSimulation.Graph;
import org.zharov.thermal_circuit_simulation.TCSimulation.TCSimulation;
import org.zharov.thermal_circuit_simulation.TCSimulation.Vertex;

import java.util.HashMap;
import java.util.Map;

public class Example {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Graph graph = new Example().graphCompilation();
        // Создание экземпляра класса TCSimulation. В конструктор
        // в качестве параметров передаем:
        // calculationType - тип расчета:
        // FOR_A_GIVEN_POWER - по заданной мощности
        // FOR_A_GIVEN_CONSUMPTION - по заданному расходу
        // powerOrConsumptionValue - значение мощности в МВт или значение расхода в кг/с
        // в зависимости от типа расчёта
        // mechanicalEfficiencyOfTurbogenerator - механический КПД турбогенератора
        // graph - граф, содержащий всю информацию о тепловой схеме: связи между элементами и параметры среды
        TCSimulation simulation = new TCSimulation(TCSimulation.FOR_A_GIVEN_POWER,
                1004, 0.988, 0.99, graph);
        simulation.start(); // запуск рачета
        simulation.getGraph().describe();   // получение графа и вывод результатов расчета в консоль
        // при помощи метода describe().
        simulation.getThermalEfficiencyIndicators().describe(); // получение показателей тепловой экономичности
        // и вывод их в консоль при помощи метода describe().
        long finishTime = System.currentTimeMillis();
        System.out.println((finishTime - startTime) + " ms");

    }

    public Graph graphCompilation() {
        Graph graph = new Graph();  // Создание экземпляра класса Graph

        //---------------ПГ
        Vertex pg = new Vertex(new SteamGenerator("ПГ"));   // Создание вершины (узла) графа
        // в конструктор узла графа передаем элемент SteamGenerator - парогенератор
        graph.addVertex(pg);    //добавление узла pg в граф

        //---------------ЦСД
        // Создание экземпляра класса TurbineCylinder - цилиндр турбины
        // в конструктор передаем в качестве параметров имя - ЦСД, число отборов - 3
        TurbineCylinder turbineCylinder_csd = new TurbineCylinder("ЦСД", 3);
        // Далее добавляем параметры пара в ЦСД при помощи метода addSelection
        // Параметрами этого метода являются:
        // selectionNumber - номер отбора,
        // причем значение 0 - используется для добавления параметров пара на входе в цилиндр
        // значение numberOfSelection + 2 - для параметров пара на выходе из цилиндра
        // pressure - давление пара, МПа
        // XorT - степень сухости в относительных единицах или температура пара в градусах Цельсия
        // Программа автоматически понимает, какое значение было введено: температура или степень сухости
        turbineCylinder_csd.addSelection(0, 5.88, 0.995);
        turbineCylinder_csd.addSelection(1, 2.982, 0.929);
        turbineCylinder_csd.addSelection(2, 1.92, 0.902);
        turbineCylinder_csd.addSelection(3, 1.203, 0.881);
        turbineCylinder_csd.addSelection(4, 1.203, 0.881);
        // Создаем узел, в конструктор передаем созданный выше экземпляр класса TurbineCylinder
        Vertex csd = new Vertex(turbineCylinder_csd);
        graph.addVertex(csd);   // Добавление узла в граф

        //---------------Сепаратор
        // Создание узла графа. В конструктор передаем экземпляр класса Separator, в конструктор которого
        // передаём параметры:
        // name - имя - Сепаратор
        // hydraulicResistanceFromCylinderToSeparator - гидравлические потери в МПа от цилиндра турбины
        // до сепаратора
        // outletDegreeOfDryness - степень сухости пара на выходе из сепаратора в относительных единицах
        Vertex separator = new Vertex(new Separator("Сепаратор", 0.02, 0.999));
        graph.addVertex(separator); // Добавление узла в граф

        //---------------ПП1
        // Создание узла графа. В конструктор передаем экземпляр класса Superheater, в конструктор которого
        // передаем параметры:
        // name - имя - ПП1
        // heaterNumber - номер подогревателя по ходу пара
        // hydraulicResistanceFromSelectionToHeater - гидравлические потери от отбора до подогревателя в МПа
        // hydraulicResistanceInHeater - гидравлические потери в подогревателе в МПа
        // underheatingOfHeatedMedium - Недогрев обогреваемой среды на выходе до температуры в подогревателе
        // в градуса Цельсия
        Vertex pp1 = new Vertex(new Superheater("ПП1", 1, 0.12,
                0.02, 20));
        graph.addVertex(pp1);   // Добавление узла в граф

        //---------------ПП2
        // Создание узла графа, соответствующего элементу ПП2
        Vertex pp2 = new Vertex(new Superheater("ПП2", 2, 0.2,
                0.02, 22.2));
        graph.addVertex(pp2);   // Добавление узла в граф

        //----------------ЦНД
        // Создание узла графа, соответствующего элементу ЦНД
        TurbineCylinder turbineCylinder_cnd = new TurbineCylinder("ЦНД", 4);
        turbineCylinder_cnd.addSelection(0, 1.118, 250);
        turbineCylinder_cnd.addSelection(1, 0.638, 193);
        turbineCylinder_cnd.addSelection(2, 0.340, 139);
        turbineCylinder_cnd.addSelection(3, 0.092, 0.945);
        turbineCylinder_cnd.addSelection(4, 0.025, 0.902);
        turbineCylinder_cnd.addSelection(5, 0.0039, 0.8755);
        Vertex cnd = new Vertex(turbineCylinder_cnd);
        graph.addVertex(cnd);   // Добавление узла в граф

        //-------------------------Конденсатор
        // Создание узла графа. В конструктор передаем экземпляр класса Condenser.
        Vertex condenser = new Vertex(new Condenser("Конденсатор"));
        graph.addVertex(condenser); // Добавление узла в граф

        //-----------------------Деаэратор
        // Создание узла графа. В конструктор передаем экземпляр класса Deaerator, в конструктор которого
        // передаем параметры:
        // name
        // pressureInHeater - давление в подогревателе в МПа
        Vertex d = new Vertex(new Deaerator("Деаэратор", 0.69));
        graph.addVertex(d); // Добавление узла в граф

        //-------------------------Конденсатный насос 1
        // Создание узла графа. В конструктор передаем экземпляр класса Pump, в конструктор которого
        // передаем параметры:
        // name
        // efficiency - КПД насоса в относительных единицах
        // pumpHead - необходимый напор насоса в МПа
        // isThePumpDriveElectric - Привод насоса электрический?
        // true - да
        // false - нет, привод насоса - турбина
        // pumpDriveEfficiency - КПД привода насоса в относительных единицах
        // (Если используется турбопривод, то имеется в виду механический КПД)
        Vertex kn1 = new Vertex(new Pump("КНI", 0.78, 0.9,
                true, 0.86));
        graph.addVertex(kn1);

        //-------------------------Основной эжектор
        // Создание узла графа. В конструктор передаем экземпляр класса MainEjectorWithCooler,
        // в конструктор которого пердаем параметры:
        // name
        // hydraulicResistanceInCooler - Гидравлическое сопротивление в охладителе пара эжектора в МПа
        // enthalpyIncrease - повышение энтальпии в кДж/кг
        // highPressureFlow - высоконапорный поток пара (на входе в эжектор), кг/с
        Vertex mainEjector = new Vertex(new MainEjectorWithCooler("Основной Эжектор",
                0.15, 2, 1.22));
        graph.addVertex(mainEjector);   // Добавление узла в граф.

        //-------------------------Эжектор уплотнений
        Vertex sealEjector = new Vertex(new SealEjectorWithCooler("Эжектор Уплотнений",
                0.15, 6, 1.06));
        graph.addVertex(sealEjector);

        //-------------------------Конденсатный насос 2
        Vertex kn2 = new Vertex(new Pump("КНII", 0.78, 1.0,
                true, 0.86));
        graph.addVertex(kn2);

        //-------------------------ПНД1
        // Создание узла графа. В конструктор передаем экземпляр класса Heater,
        // в конструктор которого передаем параметры:
        // name
        // heaterNumber - Номер подогревателя по ходу воды
        // hydraulicResistanceInHeater - Гидравлическое сопротивление в подогревателе в МПа
        // underheatingOfHeatedMedium - Недогрев обогреваемой среды на выходе до температуры в подогревателе,
        // в градусах Цельсия
        Vertex pnd1 = new Vertex(new Heater("ПНД1", 1, 0.15, 2.5));
        graph.addVertex(pnd1);  // Добавление узла в граф

        //---------------------ДН1
        Vertex dn1 = new Vertex(new Pump("ДН1", 0.76, 2, true, 0.86));
        graph.addVertex(dn1);

        //-------------------------См1
        Vertex sm1 = new Vertex(new MixingPoint("См1"));
        graph.addVertex(sm1);

        //-------------------------ПНД2
        // Создание узла графа. В конструктор передаем экземпляр класса Heater,
        // в конструктор которого передаем параметры:
        // name
        // heaterNumber - Номер подогревателя по ходу воды
        // hydraulicResistanceInHeater - Гидравлическое сопротивление в подогревателе в МПа
        // underheatingOfSteamDrain - Недогрев дренажа
        // (температурный напор между обогреваемой средой на входе и дренажом на выходе)
        // в градусах Цельсия
        // underheatingOfHeatedMedium - Недогрев обогреваемой среды на выходе до температуры в подогревателе,
        // в градусах Цельсия
        Vertex pnd2 = new Vertex( new Heater("ПНД2", 2, 0.15, 5, 3));
        graph.addVertex(pnd2);

        //------------------------ПНД3
        Vertex pnd3 = new Vertex(new Heater("ПНД3", 3, 0.15, 4));
        graph.addVertex(pnd3);

        //------------------------ДН2
        Vertex dn2 = new Vertex(new Pump("ДН2", 0.76, 1.5, true, 0.86));
        graph.addVertex(dn2);

        //------------------------См2
        Vertex sm2 = new Vertex(new MixingPoint("См2"));
        graph.addVertex(sm2);

        //------------------------ПНД4
        Vertex pnd4 = new Vertex(new Heater("ПНД4", 4, 0.15, 4.5, 4));
        graph.addVertex(pnd4);

        //-----------------------ПН
        Vertex pn = new Vertex(new Pump("ПН", 0.89, 8.9, false, 0.96));
        graph.addVertex(pn);

        //-----------------------ПВД5
        Vertex pvd5 = new Vertex(new Heater("ПВД5", 5, 0.4, 5, 5));
        graph.addVertex(pvd5);

        //-----------------------ПВД6
        Vertex pvd6 = new Vertex(new Heater("ПВД6", 6, 0.4, 5, 5.5));
        graph.addVertex(pvd6);

        //-----------------------ПВД7
        Vertex pvd7 = new Vertex(new Heater("ПВД7", 7, 0.4, 5, 6.2));
        graph.addVertex(pvd7);

        //----------------------ТС - теплосеть
        // Создание узла графа. В конструктор передаем экземпляр класса HeatNetwork, в конструктор которого
        // передаем параметры:
        // name
        // inletPressure - Давление на входе в ТС в МПа
        // inletTemperature - Температура на входе в ТС в °С
        // outletPressure - Давление на выходе из ТС в МПа
        // outletTemperature - Температура на выходе из ТС в °С
        // thermalPowerOfHeatNetwork - тепловая мощность ТС в МВт
        Vertex ts = new Vertex(new HeatNetwork("Теплосеть", 1, 150, 1.6, 60, 120));
        graph.addVertex(ts);    // Добавление узла в граф

        //----------------------Т1
        Vertex t1 = new Vertex(new Heater("Т1", 1, 0.2, 4));
        graph.addVertex(t1);

        //----------------------Т2
        Vertex t2 = new Vertex(new Heater("Т2", 2, 0.2, 4.6));
        graph.addVertex(t2);

        //----------------------Т3
        Vertex t3 = new Vertex(new Heater("Т3", 3, 0.2, 8.8));
        graph.addVertex(t3);

        //---------------------ТП
        // Создание узла графа. В конструктор передаем экземпляр класса TurboDrive, в конструктор которого
        // передаем параметры:
        // name
        // relativeInternalEfficiency - относительный внутренний КПД ТП
        // condenserPressure - Давление в конденсаторе ТП
        Vertex turboDrive = new Vertex( new TurboDrive("ТП", 0.73, 0.004));
        graph.addVertex(turboDrive);    // Добавление узла в граф
        //-------------------------------------------

        HashMap<Element, Double> mapForValveStemSeal = new HashMap<>();
        mapForValveStemSeal.put(csd.element, 1.8);
        mapForValveStemSeal.put(separator.element, 1.33);
        mapForValveStemSeal.put(pnd3.element, 0.37);
        mapForValveStemSeal.put(sealEjector.element, 0.1);
        Vertex valveStemSeal = new Vertex(new ValveStemSeals("Уплотнение штоков клапанов ЦСД", mapForValveStemSeal,turbineCylinder_csd));
        graph.addVertex(valveStemSeal);

        HashMap<Element, Double> mapForTurbineShaftSealsForCSD = new HashMap<>();
        mapForTurbineShaftSealsForCSD.put(csd.element, 2.4);
        mapForTurbineShaftSealsForCSD.put(pnd4.element, 1.38);
        mapForTurbineShaftSealsForCSD.put(pnd1.element, 0.9);
        mapForTurbineShaftSealsForCSD.put(sealEjector.element, 0.15);
        Vertex turbineShaftSealForCSD = new Vertex(new TurbineShaftSeals("Уплотнение вала ЦСД", mapForTurbineShaftSealsForCSD, turbineCylinder_csd));
        graph.addVertex(turbineShaftSealForCSD);

        HashMap<Element, Double> mapForTurbineShaftSealsForCND = new HashMap<>();
        mapForTurbineShaftSealsForCND.put(cnd.element, 1.48);
        mapForTurbineShaftSealsForCND.put(d.element, 2.56);
        mapForTurbineShaftSealsForCND.put(sealEjector.element, 1.08);
        Vertex turbineShaftSealForCND = new Vertex(new TurbineShaftSeals("Уплотнение вала ЦНД", mapForTurbineShaftSealsForCND, turbineCylinder_cnd));
        graph.addVertex(turbineShaftSealForCND);

        // Добавление ребер (связей) в граф при помощи метода addEdge,
        // в параметры метода передаем:
        // mediumType - тип среды:
        // FEED_WATER - питательная среда
        // NETWORK_WATER - сетевая вода
        // HEATING_STEAM - греющий пар
        // STEAM_DRAIN - дренаж пара
        // SUPERHEATED_STEAM - перегретый пар
        // MECHANICAL_COMMUNICATION - механическая связь
        // initialVertex - начальный узел (начало связи)
        // finalVertex - конечный узел (конец связи)
        graph.addEdge(Graph.HEATING_STEAM, pg, csd);
        graph.addEdge(Graph.FEED_WATER, pvd7, pg);

        graph.addEdge(Graph.HEATING_STEAM, csd, separator);
        graph.addEdge(Graph.HEATING_STEAM, csd, 0, pp2);
        graph.addEdge(Graph.HEATING_STEAM, csd, 1, pp1);
        graph.addEdge(Graph.HEATING_STEAM, csd, 1, pvd7);
        graph.addEdge(Graph.HEATING_STEAM, csd, 2, pvd6);
        graph.addEdge(Graph.HEATING_STEAM, csd, 3, pvd5);
        graph.addEdge(Graph.HEATING_STEAM, csd, 3, d);
        graph.addEdge(Graph.HEATING_STEAM, csd, valveStemSeal);

        graph.addEdge(Graph.HEATING_STEAM, valveStemSeal, separator);
        graph.addEdge(Graph.HEATING_STEAM, valveStemSeal, pnd3);
        graph.addEdge(Graph.HEATING_STEAM, valveStemSeal, sealEjector);

        graph.addEdge(Graph.HEATING_STEAM, csd, turbineShaftSealForCSD);

        graph.addEdge(Graph.HEATING_STEAM, turbineShaftSealForCSD, pnd4);
        graph.addEdge(Graph.HEATING_STEAM, turbineShaftSealForCSD, pnd1);
        graph.addEdge(Graph.HEATING_STEAM, turbineShaftSealForCSD, sealEjector);

        graph.addEdge(Graph.SUPERHEATED_STEAM, separator, pp1);
        graph.addEdge(Graph.STEAM_DRAIN, separator, d);

        graph.addEdge(Graph.SUPERHEATED_STEAM, pp1, pp2);
        graph.addEdge(Graph.STEAM_DRAIN, pp1, pvd6);


        graph.addEdge(Graph.SUPERHEATED_STEAM, pp2, cnd);
        graph.addEdge(Graph.STEAM_DRAIN, pp2, pvd7);

        graph.addEdge(Graph.HEATING_STEAM, cnd, 0, turboDrive);
        graph.addEdge(Graph.HEATING_STEAM, cnd, condenser);
        graph.addEdge(Graph.HEATING_STEAM, cnd, 1, pnd4);
        graph.addEdge(Graph.HEATING_STEAM, cnd, 2, pnd3);
        graph.addEdge(Graph.HEATING_STEAM, cnd, 3, pnd2);
        graph.addEdge(Graph.HEATING_STEAM, cnd, 4, pnd1);
        graph.addEdge(Graph.HEATING_STEAM, cnd, 1, t3);
        graph.addEdge(Graph.HEATING_STEAM, cnd, 2, t2);
        graph.addEdge(Graph.HEATING_STEAM, cnd, 3, t1);

        graph.addEdge(Graph.HEATING_STEAM, turbineShaftSealForCND, cnd);
        graph.addEdge(Graph.HEATING_STEAM, turbineShaftSealForCND, sealEjector);
        graph.addEdge(Graph.HEATING_STEAM, d, turbineShaftSealForCND);

        graph.addEdge(Graph.FEED_WATER, condenser, kn1);
        graph.addEdge(Graph.FEED_WATER, kn1, mainEjector);

        graph.addEdge(Graph.STEAM_DRAIN, mainEjector, condenser);
        graph.addEdge(Graph.HEATING_STEAM, d, mainEjector);
        graph.addEdge(Graph.FEED_WATER, mainEjector, sealEjector);

        graph.addEdge(Graph.STEAM_DRAIN, sealEjector, condenser);
        graph.addEdge(Graph.HEATING_STEAM, d, sealEjector);
        graph.addEdge(Graph.HEATING_STEAM, valveStemSeal, sealEjector);
        graph.addEdge(Graph.HEATING_STEAM, turbineShaftSealForCSD, sealEjector);
        graph.addEdge(Graph.HEATING_STEAM, turbineShaftSealForCND, sealEjector);
        graph.addEdge(Graph.FEED_WATER, sealEjector, kn2);

        graph.addEdge(Graph.FEED_WATER, kn2, pnd1);

        graph.addEdge(Graph.FEED_WATER, pnd1, sm1);
        /*theGraph.addEdge(TCSimulation.FEED_WATER, pnd1, n);
        theGraph.addEdge(TCSimulation.FEED_WATER, n, pnd2);*/
        graph.addEdge(Graph.FEED_WATER, sm1, pnd2);
        graph.addEdge(Graph.STEAM_DRAIN, pnd1, dn1);
        graph.addEdge(Graph.STEAM_DRAIN, dn1, sm1);

        graph.addEdge(Graph.FEED_WATER, pnd2, pnd3);
        graph.addEdge(Graph.STEAM_DRAIN, pnd2, pnd1);

        graph.addEdge(Graph.FEED_WATER, pnd3, sm2);
        graph.addEdge(Graph.FEED_WATER, sm2, pnd4);
        graph.addEdge(Graph.STEAM_DRAIN, pnd3, dn2);
        graph.addEdge(Graph.STEAM_DRAIN, dn2, sm2);

        graph.addEdge(Graph.FEED_WATER, pnd4, d);
        graph.addEdge(Graph.STEAM_DRAIN, pnd4, pnd3);

        graph.addEdge(Graph.FEED_WATER, d, pn);

        graph.addEdge(Graph.FEED_WATER, pn, pvd5);

        graph.addEdge(Graph.FEED_WATER, pvd5, pvd6);
        graph.addEdge(Graph.STEAM_DRAIN, pvd5, pnd4);

        graph.addEdge(Graph.FEED_WATER, pvd6, pvd7);
        /*theGraph.addEdge(TCSimulation.STEAM_DRAIN, pvd6, pvd5);*/
        graph.addEdge(Graph.STEAM_DRAIN, pvd6, d);

        graph.addEdge(Graph.STEAM_DRAIN, pvd7, pvd6);

        graph.addEdge(Graph.NETWORK_WATER, t3, ts);
        graph.addEdge(Graph.STEAM_DRAIN, t3, t2);
        graph.addEdge(Graph.NETWORK_WATER, ts, t1);
        graph.addEdge(Graph.NETWORK_WATER, t1, t2);
        graph.addEdge(Graph.STEAM_DRAIN, t1, condenser);
        graph.addEdge(Graph.NETWORK_WATER, t2, t3);
        graph.addEdge(Graph.STEAM_DRAIN, t2, t1);

        graph.addEdge(Graph.STEAM_DRAIN, turboDrive, condenser);
        graph.addEdge(Graph.MECHANICAL_COMMUNICATION, turboDrive, pn);
        return graph;
    }
}
