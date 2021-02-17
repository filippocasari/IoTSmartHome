package MCC.EmulatedResource.Sensor;

import MCC.DataListener;
import MCC.SmartObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class EnergySensor extends SmartObject<Double> {
    private static Logger logger = LoggerFactory.getLogger(EnergySensor.class);

    /** ENERGY RANGE VALUE & VARIATION **/
    private static final double MIN_ENERGY_VALUE = 1.0; //kWh - kilowatt-hour
    private static final double MAX_ENERGY_VALUE = 3.0;
    private static final double MIN_ENERGY_VARIATION = 0.1;
    private static final double MAX_ENERGY_VARIATION = 0.5;

    /** TIME CONSTRAINTS **/
    private static final long UPDATE_PERIOD = 2000;
    private static final long TASK_DELAY_TIME = 2000;

    /** LABEL **/
    private static final String RESOURCE_TYPE = "sensor.energy";
    private static final String LOG_DISPLAY_NAME = "EnergySensor";

    private Double updatedValue;
    private Random random;
    private Timer updateTimer = null;
    private boolean isActive = true;

    public EnergySensor() {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        init();
    }

    private void init(){
        try{
            this.random = new Random(System.currentTimeMillis());
            this.updatedValue = MIN_ENERGY_VALUE + this.random.nextDouble()*(MAX_ENERGY_VALUE - MIN_ENERGY_VALUE);
            startPeriodicEventValueUpdateTask();
        }catch (Exception e){
            logger.error("Error initializing the IoT Resource ! Msg: {}", e.getLocalizedMessage());
        }
    }

    private void startPeriodicEventValueUpdateTask(){
        try{
            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);
            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(isActive){
                        double variation = (MIN_ENERGY_VARIATION + MAX_ENERGY_VARIATION *random.nextDouble()) * (random.nextDouble() > 0.5 ? 1.0 : -1.0);
                        updatedValue = updatedValue + variation;
                    }
                    else { updatedValue = 0.0; }
                    notifyUpdate(updatedValue);
                }
            }, TASK_DELAY_TIME, UPDATE_PERIOD);

        }catch (Exception e){
            logger.error("Error executing periodic resource value ! Msg: {}", e.getLocalizedMessage());
        }

    }

    @Override
    public Double loadUpdatedValue() {
        return this.updatedValue;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public static void main(String[] args) {

        EnergySensor rawResource = new EnergySensor();
        rawResource.setActive(false);
        logger.info("New {} resource created!\t\t\t\tId: {}\t\t{} Starting Value: {}",
                rawResource.getType(),
                rawResource.getId(),
                LOG_DISPLAY_NAME,
                rawResource.loadUpdatedValue());

        rawResource.addDataListener(new DataListener<Double>() {
            @Override
            public void onDataChanged(SmartObject<Double> resource, Double updatedValue) {

                if(resource != null && updatedValue != null)
                    logger.info("Device: {} \tNew Value Received: {}", resource.getId(), updatedValue);
                else
                    logger.error("onDataChanged Callback -> Null Resource or Updated Value !");
            }
        });

    }
}