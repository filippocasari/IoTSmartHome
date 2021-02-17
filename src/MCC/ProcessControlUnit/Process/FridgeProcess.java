package MCC.ProcessControlUnit.Process;

import MCC.Coap.EnergyResource;
import MCC.Coap.TemperatureResource;
import MCC.Resource.Sensor.EnergySensor;
import MCC.Resource.Sensor.TemperatureSensor;
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

        EnergySensor fridgeEnergySensor = new EnergySensor();
        TemperatureSensor fridgeTemperatureSensor = new TemperatureSensor("fridge");

        EnergyResource fridgeEnergyResource = new EnergyResource(deviceId, "energy", fridgeEnergySensor);
        TemperatureResource fridgeTemperatureResource = new TemperatureResource(deviceId, "temperature", fridgeTemperatureSensor);

        this.add(fridgeEnergyResource);
        this.add(fridgeTemperatureResource);
    }
}
