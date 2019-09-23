package io.fairspace.saturn.webdav;

import io.fairspace.saturn.events.EventService;
import io.fairspace.saturn.events.FileSystemEvent;
import io.milton.http.Request;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class WebdavEventEmitter implements Consumer<Request> {
    private static Map<Request.Method, FileSystemEvent.FileEventType> httpMethodToEventTypeMap = Map.of(
            Request.Method.MKCOL, FileSystemEvent.FileEventType.CREATE_DIRECTORY,
            Request.Method.PROPFIND, FileSystemEvent.FileEventType.LIST,
            Request.Method.COPY, FileSystemEvent.FileEventType.COPY,
            Request.Method.MOVE, FileSystemEvent.FileEventType.MOVE,
            Request.Method.DELETE, FileSystemEvent.FileEventType.DELETE,
            Request.Method.PUT, FileSystemEvent.FileEventType.WRITE_FILE,
            Request.Method.GET, FileSystemEvent.FileEventType.READ_FILE
    );
    private EventService eventService;

    public WebdavEventEmitter(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public void accept(Request request) {
        if(!httpMethodToEventTypeMap.containsKey(request.getMethod())) {
            log.debug("No event to emit for http method {}", request.getMethod());
            return;
        }

        FileSystemEvent.FileSystemEventBuilder builder = FileSystemEvent.builder()
                .eventType(httpMethodToEventTypeMap.get(request.getMethod()))
                .path(request.getAbsolutePath());


        // For copy and move operations, the destination of the operation is relevant as well
        if(request.getMethod() == Request.Method.COPY || request.getMethod() == Request.Method.MOVE) {
            builder.destination(request.getDestinationHeader());
        }

        eventService.emitEvent(builder.build());
    }
}
