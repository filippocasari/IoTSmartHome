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
    public  double ENERGY_VALUE;
    private static double MIN_ENERGY_VALUE; //Wh - Watt-hour
    private static double MAX_ENERGY_VALUE;
    private static final double MIN_ENERGY_VARIATION = 0.1;
    private static final double MAX_ENERGY_VARIATION = 0.5;

    /** TIME CONSTRAINTS **/
    private static final long UPDATE_PERIOD = 20000;
    private static final long TASK_DELAY_TIME = 20000;

    /** LABEL **/
    private static final String RESOURCE_TYPE = "sensor.energy";
    private static final String LOG_DISPLAY_NAME = "EnergySensor";

    public Double updatedValue;
    private Random random;
    private Timer updateTimer = null;
    private boolean isActive = true;

    public EnergySensor(String device) {
        super(UUID.randomUUID().toString(), RESOURCE_TYPE);
        init(device);
    }

    public void setUpdatedValue(Double updatedValue) {
        this.updatedValue = updatedValue;
    }

    private void init(String type){
        try{
            if(type.contentEquals("lights")){
                ENERGY_VALUE = 3;
                MIN_ENERGY_VALUE = ENERGY_VALUE - 2;
                MAX_ENERGY_VALUE = ENERGY_VALUE + 2;

            }else if (type.contentEquals("TV")){
                ENERGY_VALUE = 55;
                MIN_ENERGY_VALUE = ENERGY_VALUE - 3;
                MAX_ENERGY_VALUE = ENERGY_VALUE + 3;

            }else if (type.contentEquals("fridge")){
                ENERGY_VALUE = 140;
                MIN_ENERGY_VALUE = ENERGY_VALUE - 5;
                MAX_ENERGY_VALUE = ENERGY_VALUE + 5;

            }else if(type.contentEquals("washer")){
                ENERGY_VALUE = 95;
                MIN_ENERGY_VALUE = ENERGY_VALUE - 3;
                MAX_ENERGY_VALUE = ENERGY_VALUE + 3;

            }
            this.random = new Random(System.currentTimeMillis());
            this.updatedValue = MIN_ENERGY_VALUE + this.random.nextDouble()*(MAX_ENERGY_VALUE - MIN_ENERGY_VALUE);
            startPeriodicEventValueUpdateTask();
        }catch (Exception e){
            logger.error("Error initializing the IoT Resource ! Msg: {}", e.getLocalizedMessage());
        }
    }

    public void startPeriodicEventValueUpdateTask(){
        try{
            logger.info("Starting periodic Update Task with Period: {} ms", UPDATE_PERIOD);
            this.updateTimer = new Timer();
            this.updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(isActive){
                        double variation = (MIN_ENERGY_VARIATION + MAX_ENERGY_VARIATION * random.nextDouble()) * (random.nextDouble() > 0.5 ? 1.0 : -1.0);
                        updatedValue = updatedValue + variation;
                    }
                    else {
                        updatedValue = 0.0;
                    }
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
        startPeriodicEventValueUpdateTask();
    }

    public static void main(String[] args) {

        EnergySensor rawResource = new EnergySensor("lights");
        rawResource.setActive(true);
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
