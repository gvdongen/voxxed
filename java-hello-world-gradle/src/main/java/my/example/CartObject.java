package my.example;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.restate.sdk.JsonSerdes;
import dev.restate.sdk.ObjectContext;
import dev.restate.sdk.annotation.Handler;
import dev.restate.sdk.annotation.VirtualObject;
import dev.restate.sdk.common.StateKey;
import dev.restate.sdk.serde.jackson.JacksonSerdes;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@VirtualObject
public class CartObject {

    StateKey<Set<String>> TICKETS =
            StateKey.of("tickets",
                    JacksonSerdes.of(new TypeReference<Set<String>>() {}));

    @Handler
    public boolean addTicket(ObjectContext ctx, String ticketId){

        var success = TicketObjectClient.fromContext(ctx, ticketId)
                .reserve().await();

        if(success){
            var tickets = ctx.get(TICKETS).orElse(new HashSet<>());
            tickets.add(ticketId);
            ctx.set(TICKETS, tickets);

            CartObjectClient.fromContext(ctx, ctx.key())
                    .send(Duration.ofMinutes(15))
                    .expireTicket(ticketId);
        }

        return success;
    }

    @Handler
    public void expireTicket(ObjectContext ctx, String ticketId){

    }

    @Handler
    public boolean checkout(ObjectContext ctx){
        var tickets = ctx.get(TICKETS).orElse(new HashSet<>());

        if(tickets.isEmpty()){
            return false;
        }

        var payID = ctx.random().nextUUID().toString();
        boolean success = ctx.run("pay", JsonSerdes.BOOLEAN,
                () -> pay(payID, tickets.size()*40));

        if(success){
            for (String ticket : tickets) {
                TicketObjectClient.fromContext(ctx, ticket)
                        .send()
                        .markAsSold();
            }

            ctx.clear(TICKETS);
            return true;
        }

        return false;
    }

    private boolean pay(String idempotencyKey, double totalPrice){
        System.out.println("Paying tickets for " + idempotencyKey + " and price " + totalPrice);
        return true;
    }
}
