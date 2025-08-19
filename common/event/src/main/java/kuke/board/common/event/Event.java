package kuke.board.common.event;

import kuke.board.common.dataserializer.DataSerializer;
import lombok.Getter;

// <T extends Class> : 타입 경계
// 제네릭 타입 매개변수 T는 특정 클랙스나 인터페이스를 상속받은(혹은 구현한) 타입만 올 수 있음.
@Getter
public class Event<T extends EventPayload> {
    private Long eventId;
    private EventType type;
    private T payload;

    public static Event<EventPayload> of(Long eventId, EventType type, EventPayload payload) {
        Event<EventPayload> event = new Event<>();
        event.eventId = eventId;
        event.type = type;
        event.payload = payload;
        return event;
    }

    public String toJson() {
        return DataSerializer.serialize(this);
    }

    public static Event<EventPayload> fromJson(String json) {
        EventRaw eventRaw = DataSerializer.deserialize(json, EventRaw.class);
        if (eventRaw == null) {
            return null;
        }
        Event<EventPayload> event = new Event<>();
        event.eventId = eventRaw.getEventId();
        event.type = EventType.from(eventRaw.getType());
        event.payload = DataSerializer.deserialize(eventRaw.getPayload(), event.type.getPayloadClass());
        return event;
    }

    @Getter
    private static class EventRaw {
        private Long eventId;
        private String type;
        private Object payload;
    }
}
