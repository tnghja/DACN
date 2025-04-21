package com.ecommerce.inventory.model.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "eventType" // Field in JSON to determine subtype
)
//@JsonSubTypes({
//        @JsonSubTypes.Type(value = OrderCreatedEvent.class, name = "orderCreated"),
//        @JsonSubTypes.Type(value = OrderStatusEvent.class, name = "orderStatus")
//})
public interface Event {
    // Common methods (optional)
    String eventType();
}