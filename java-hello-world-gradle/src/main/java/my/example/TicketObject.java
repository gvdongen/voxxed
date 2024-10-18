package my.example;

import dev.restate.sdk.JsonSerdes;
import dev.restate.sdk.ObjectContext;
import dev.restate.sdk.annotation.Handler;
import dev.restate.sdk.annotation.VirtualObject;
import dev.restate.sdk.common.StateKey;

@VirtualObject
public class TicketObject {

    StateKey<String> STATUS =
            StateKey.of("status", JsonSerdes.STRING);

    @Handler
    public boolean reserve(ObjectContext ctx){
        var status = ctx.get(STATUS).orElse("Available");

        if(status.equals("Available")){
            ctx.set(STATUS, "Reserved");
            return true;
        }

        return false;
    }


    @Handler
    public void unreserve(ObjectContext ctx){
        var status = ctx.get(STATUS).orElse("Available");

        if(!status.equals("Sold")){
            ctx.set(STATUS, "Available");
        }
    }


    @Handler
    public void markAsSold(ObjectContext ctx){
        var status = ctx.get(STATUS).orElse("Available");

        if(status.equals("Reserved")){
            ctx.set(STATUS, "Sold");
        }
    }

}
