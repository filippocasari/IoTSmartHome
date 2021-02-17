package MCC.process;

import MCC.coap.EnergyResource;
import MCC.coap.SwitchResource;
import MCC.coap.TemperatureResource;
import MCC.resource.actuator.SwitchActuator;
import MCC.resource.sensor.EnergySensor;
import MCC.resource.sensor.TemperatureSensor;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ThermostatProcess extends CoapServer {

    private final static Logger logger = LoggerFactory.getLogger(ThermostatProcess.class);

    public ThermostatProcess() {
        super();
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());
        //TODO ADD();
    }

    private CoapResource createThermostatResource (String deviceID){
        CoapResource thermostatRootResource = new CoapResource("thermostat");

        EnergySensor thEnergySensor = new EnergySensor();
        SwitchActuator thSwitchActuator = new SwitchActuator();
        TemperatureSensor thTemperatureSensor = new TemperatureSensor(thermostatRootResource.getName());

        EnergyResource thEnergyResource = new EnergyResource(deviceID, "energy", thEnergySensor);
        TemperatureResource thTemperatureResource = new TemperatureResource(deviceID, "temperature", thTemperatureSensor);
        SwitchResource thSwitchResource= new SwitchResource(deviceID, "switch", thSwitchActuator);

        return thermostatRootResource;
    }

    public static void main(String[] args) {

        ThermostatProcess smartObjectProcess = new ThermostatProcess();
        smartObjectProcess.start();

        logger.info("Coap Server Started! Available resources: ");

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
