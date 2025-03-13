package com.ecommerce.notification_service.model.mapper;

import com.ecommerce.notification_service.model.entity.Notification;
import com.ecommerce.notification_service.model.entity.request.NotificationRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.2 (Oracle Corporation)"
)
@Component
public class NotificationMapperImpl extends NotificationMapper {

    @Override
    public Notification toEntity(NotificationRequest request) {
        if ( request == null ) {
            return null;
        }

        Notification notification = new Notification();

        notification.setMessage( request.getMessage() );
        notification.setType( request.getType() );

        notification.setRead( false );

        setUser( notification, request );

        return notification;
    }
}
