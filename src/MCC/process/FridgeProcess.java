package MCC.process;

import MCC.DataListener;
import MCC.SmartObject;
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

public class FridgeProcess extends CoapServer {
    private final static Logger logger = LoggerFactory.getLogger(FridgeProcess.class);

    public FridgeProcess() {
        super();
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());
        this.add(createFridgeResource(deviceId));
    }

    private CoapResource createFridgeResource(String deviceId){
        CoapResource fridgeRootResource = new CoapResource("fridge");

        EnergySensor fridgeEnergySensor = new EnergySensor();
        TemperatureSensor fridgeTemperatureSensor = new TemperatureSensor(fridgeRootResource.getName());

        EnergyResource fridgeEnergyResource = new EnergyResource(deviceId, "energy", fridgeEnergySensor);
        TemperatureResource fridgeTemperatureResource = new TemperatureResource(deviceId, "temperature", fridgeTemperatureSensor);

        fridgeRootResource.add(fridgeEnergyResource);
        fridgeRootResource.add(fridgeTemperatureResource);

        return fridgeRootResource;
    }

    public static void main(String[] args) {
        FridgeProcess fridgeProcess = new FridgeProcess();
        fridgeProcess.start();
        //logger.info("Coap Server Started! Available resources: ");

        fridgeProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });
    }

}
