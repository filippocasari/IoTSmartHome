package MCC.EmulatedResource.Sensor;

import MCC.DataListener;
import MCC.SmartObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

public class MovementSensor extends SmartObject<Boolean> {
    /** LABEL **/
    private static Logger logger = LoggerFactory.getLogger(MCC.EmulatedResource.Actuator.SwitchActuator.class);
    private static final String LOG_DISPLAY_NAME = "MovementSensor";
    private static final String RESOURCE_TYPE = "sensor.movement";

    private Boolean isActive;

    public MovementSensor() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        this.isActive = true;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
        notifyUpdate(isActive);
    }

    @Override
    public Boolean loadUpdatedValue() {
        return this.isActive;
    }

    public static void main(String[] args) {

        MovementSensor moveResource = new MovementSensor();
        logger.info("New {} Resource Created with Id: {} ! {} New Value: {}",
                moveResource.getType(),
                moveResource.getId(),
                LOG_DISPLAY_NAME,
                moveResource.loadUpdatedValue());

        moveResource.addDataListener(new DataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {

                if(resource != null && updatedValue != null)
                    logger.info("Device: {} -> New Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }
}