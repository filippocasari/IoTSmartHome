package MCC.ProcessControlUnit.Process;

import MCC.CoapResource.EnergyResource;
import MCC.CoapResource.SwitchResource;
import MCC.DataListener;
import MCC.EmulatedResource.Actuator.SwitchActuator;
import MCC.EmulatedResource.Sensor.EnergySensor;
import MCC.SmartObject;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class WasherProcess extends CoapServer {
    private final static Logger logger = LoggerFactory.getLogger(WasherProcess.class);

    public WasherProcess() {
        super();
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());

        EnergySensor washerEnergySensor = new EnergySensor("washer");
        SwitchActuator washerSwitchActuator = new SwitchActuator();

        EnergyResource washerEnergyResource = new EnergyResource(deviceId, "energy", washerEnergySensor);
        SwitchResource washerSwitchResource = new SwitchResource(deviceId, "switch", washerSwitchActuator);

        if(!washerSwitchResource.getOn()){
            washerEnergyResource.setUpdatedEnergyValue(0.0);

        }

        washerSwitchActuator.addDataListener(new DataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {
                logger.info("[TV-BEHAVIOUR] -> Updated Switch Value: {}", updatedValue);
                logger.info("[TV-BEHAVIOUR] -> Updating energy sensor configuration ...");
                washerEnergySensor.setActive(updatedValue);
            }
        });

        this.add(washerEnergyResource);
        this.add(washerSwitchResource);
    }
}
