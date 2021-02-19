package MCC.CoapResource;

import MCC.DataListener;
import MCC.SmartObject;
import MCC.EmulatedResource.Sensor.TemperatureSensor;
import MCC.Utils.CoreInterfaces;
import MCC.Utils.SenMLPack;
import MCC.Utils.SenMLRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class TemperatureResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(TemperatureResource.class);

    private static final String OBJECT_TITLE = "TemperatureSensor";

    private static final Number SENSOR_VERSION = 0.1;

    private String UNIT = "Cel";

    private TemperatureSensor temperatureSensor;

    private ObjectMapper objectMapper;

    private Double updatedTemperatureValue;

    private String deviceId;

    public TemperatureResource(String deviceId, String name, TemperatureSensor temperatureRawSensor) {

        super(name);

        if(temperatureRawSensor != null && deviceId != null){

            this.deviceId = deviceId;

            this.temperatureSensor = temperatureRawSensor;

            //Jackson Object Mapper + Ignore Null Fields in order to properly generate the SenML Payload
            this.objectMapper = new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true); // enable observing
            setObserveType(CoAP.Type.CON); // configure the notification type to CONs

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", temperatureRawSensor.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_S.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));
        }
        else
            logger.error("Error -> NULL Raw Reference !");

        this.temperatureSensor.addDataListener(new DataListener<Double>() {
            @Override
            public void onDataChanged(SmartObject<Double> resource, Double updatedValue) {
                updatedTemperatureValue = updatedValue;
                changed();
            }
        });

    }

    private Optional<String> getJsonSenmlResponse(){

        try{

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord senMLRecord = new SenMLRecord();
            senMLRecord.setBn(String.format("%s:%s", this.deviceId, this.getName()));
            senMLRecord.setBver(SENSOR_VERSION);
            senMLRecord.setU(UNIT);
            senMLRecord.setV(updatedTemperatureValue);
            senMLRecord.setT(System.currentTimeMillis());

            senMLPack.add(senMLRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        }catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        // the Max-Age value should match the update interval
        exchange.setMaxAge(TemperatureSensor.UPDATE_PERIOD);

        //If the request specify the MediaType as JSON or JSON+SenML
        if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON ||
                exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON){

            Optional<String> senmlPayload = getJsonSenmlResponse();

            if(senmlPayload.isPresent())
                exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
            else
                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
        else
            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(updatedTemperatureValue), MediaTypeRegistry.TEXT_PLAIN);

    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        try{
            if(exchange.getRequestPayload() != null){
                Double submittedValue = Double.parseDouble(new String(exchange.getRequestPayload()));
                logger.info("Submitted value: {}", submittedValue);

                this.updatedTemperatureValue = submittedValue;
                this.temperatureSensor.updateValue(updatedTemperatureValue);

                logger.info("Resource Status Updated: {}", this.updatedTemperatureValue);
                exchange.respond(CoAP.ResponseCode.CHANGED);

            }else
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

        }catch(Exception e){
            logger.error("Error Handling PUT -> {}", e.getLocalizedMessage());
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }
}
