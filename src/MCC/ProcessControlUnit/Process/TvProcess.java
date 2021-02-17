package MCC.ProcessControlUnit.Process;

import MCC.DataListener;
import MCC.SmartObject;
import MCC.CoapResource.EnergyResource;
import MCC.CoapResource.SwitchResource;
import MCC.EmulatedResource.Actuator.SwitchActuator;
import MCC.EmulatedResource.Sensor.EnergySensor;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class TvProcess extends CoapServer {
    private final static Logger logger = LoggerFactory.getLogger(TvProcess.class);
    public TvProcess() {
        super();
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());

        EnergySensor tvEnergySensor = new EnergySensor();
        SwitchActuator tvSwitchActuator = new SwitchActuator();

        EnergyResource tvEnergyResource = new EnergyResource(deviceId, "energy", tvEnergySensor);
        SwitchResource tvSwitchResource = new SwitchResource(deviceId, "switch", tvSwitchActuator);

        if(!tvSwitchResource.getOn()){
            tvEnergyResource.setUpdatedEnergyValue(0.0);

        }

        tvSwitchActuator.addDataListener(new DataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {
                logger.info("[TV-BEHAVIOUR] -> Updated Switch Value: {}", updatedValue);
                logger.info("[TV-BEHAVIOUR] -> Updating energy sensor configuration ...");
                tvEnergySensor.setActive(updatedValue);
            }
        });

        this.add(tvEnergyResource);
        this.add(tvSwitchResource);
    }

}
