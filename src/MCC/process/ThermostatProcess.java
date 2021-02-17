package MCC.process;

import MCC.DataListener;
import MCC.SmartObject;
import MCC.coap.EnergyConsumptionResource;
import MCC.coap.SwitchActuatorResource;
import MCC.coap.TemperatureResource;
import MCC.coap.ThermostatConfigParameterResource;
import MCC.coap.model.ThermostatConfigurationModel;
import MCC.resource.ThermostatConfigurationParameter;
import MCC.resource.actuator.SwitchActuator;
import MCC.resource.sensor.EnergySensor;
import MCC.resource.sensor.TemperatureSensor;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ThermostatProcess extends CoapServer {

    private final static Logger logger = LoggerFactory.getLogger(ThermostatProcess.class);

    public ThermostatProcess() {
        super();

        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());

        TemperatureSensor temperatureSensor = new TemperatureSensor();
        SwitchActuator switchActuator = new SwitchActuator();
        EnergySensor energySensor = new EnergySensor();

        TemperatureResource temperatureResource = new TemperatureResource(deviceId, "temperature", temperatureSensor);
        SwitchActuatorResource switchResource = new SwitchActuatorResource(deviceId, "switch", switchActuator);
        EnergyConsumptionResource energyConsumptionResource = new EnergyConsumptionResource(deviceId, "energy", energySensor);

        this.add(temperatureResource);
        this.add(switchResource);
        this.add(energyConsumptionResource);

    }


    public static void main(String[] args) {

        ThermostatProcess smartObjectProcess = new ThermostatProcess();
        smartObjectProcess.start();

        logger.info("Coap Server Started ! Available resources: ");

        smartObjectProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });

    }

}
