package de.cspenler.swarm.us;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.camel.core.CamelCoreFraction;

import javax.ws.rs.core.MediaType;

public class UserService {

    public static void main(String[] args) throws Exception {
        new Swarm().fraction(new CamelCoreFraction().addRouteBuilder("user-service", new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                getContext().setTracing(true);

                restConfiguration().component("undertow")
                        .contextPath("rest")
                        .host("localhost")
                        .port(8181)
                        .bindingMode(RestBindingMode.json);
                rest("/hello")
                        .get("/{name}").to("direct:get-user")
                        .consumes(MediaType.APPLICATION_JSON)
                        .produces(MediaType.APPLICATION_JSON);
                from("direct:hello").transform(simple("Hello ${header.name}"));
                from("direct:get-user").process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        User user = new User();
                        user.setFirstName(exchange.getIn().getHeader("name", String.class));
                        exchange.getIn().setBody(user);
                    }
                });
            }
        }))
        .start().deploy();
    }

}
