package MCC.coap;

import MCC.DataListener;
import MCC.SmartObject;
import MCC.resource.actuator.SwitchActuator;
import MCC.utils.CoreInterfaces;
import MCC.utils.SenMLPack;
import MCC.utils.SenMLRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SwitchResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(EnergyResource.class);

    private static final String OBJECT_TITLE = "SwitchActuator";

    private static final Number SENSOR_VERSION = 0.1;

    private ObjectMapper objectMapper;

    private SwitchActuator switchActuator;

    private Boolean isOn = true;

    private String deviceId;

    public SwitchResource(String deviceId, String name, SwitchActuator switchActuator) {
        super(name);

        if(switchActuator != null && deviceId != null){

            this.deviceId = deviceId;

            this.switchActuator = switchActuator;

            //Jackson Object Mapper + Ignore Null Fields in order to properly generate the SenML Payload
            this.objectMapper = new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true); // enable observing
            setObserveType(CoAP.Type.CON); // configure the notification type to CONs

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", switchActuator.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_A.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));

            switchActuator.addDataListener(new DataListener<Boolean>() {
                @Override
                public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {
                    logger.info("Resource Notification Callback ! New Value: {}", updatedValue);
                    isOn = updatedValue;
                    changed();
                }
            });
        }
        else
            logger.error("Error -> NULL Raw Reference !");

    }

    private Optional<String> getJsonSenmlResponse(){

        try{

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord senMLRecord = new SenMLRecord();
            senMLRecord.setBn(String.format("%s:%s", this.deviceId, this.getName()));
            senMLRecord.setBver(SENSOR_VERSION);
            senMLRecord.setVb(isOn);
            senMLRecord.setT(System.currentTimeMillis());

            senMLPack.add(senMLRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        }catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {

        //If the request specify the MediaType as JSON or JSON+SenML
        if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON ||
                exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON){

            Optional<String> senmlPayload = getJsonSenmlResponse();

            if(senmlPayload.isPresent())
                exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
            else
                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
        //Otherwise respond with the default textplain payload
        else
            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(isOn), MediaTypeRegistry.TEXT_PLAIN);


    }

    public Boolean getOn() {
        return isOn;
    }

    public void setOn(Boolean on) {
        isOn = on;
    }

    @Override
    public void handlePOST(CoapExchange exchange) {

        try{
            //Empty request
            if(exchange.getRequestPayload() == null){

                //Update internal status
                this.isOn = !isOn;
                this.switchActuator.setActive(isOn);
                logger.info("Resource Status Updated: {}", this.isOn);
                exchange.respond(CoAP.ResponseCode.CHANGED);

            }
            else
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

        }catch (Exception e){
            logger.error("Error Handling POST -> {}", e.getLocalizedMessage());
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public void handlePUT(CoapExchange exchange) {

        try{

            //If the request body is available
            if(exchange.getRequestPayload() != null){

                boolean submittedValue = Boolean.parseBoolean(new String(exchange.getRequestPayload()));

                logger.info("Submitted value: {}", submittedValue);

                //Update internal status
                this.isOn = submittedValue;
                this.switchActuator.setActive(this.isOn);


                logger.info("Resource Status Updated: {}", this.isOn);

                exchange.respond(CoAP.ResponseCode.CHANGED);
            }
            else
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST);

        }catch (Exception e){
            logger.error("Error Handling POST -> {}", e.getLocalizedMessage());
            exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }

    }
}
