package my.example;

import dev.restate.sdk.http.vertx.RestateHttpEndpointBuilder;

public class AppMain {

    public static void main(String[] args) {
        RestateHttpEndpointBuilder.builder()
                .bind(new TicketObject())
                .bind(new CartObject())
                .buildAndListen();
    }
}
