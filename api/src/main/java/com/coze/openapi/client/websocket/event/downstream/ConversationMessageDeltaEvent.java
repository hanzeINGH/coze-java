package com.coze.openapi.client.websocket.event.downstream;

import com.coze.openapi.client.connversations.message.model.Message;
import com.coze.openapi.client.websocket.common.BaseEvent;
import com.coze.openapi.client.websocket.event.EventType;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
// 增量消息事件
// event_type: conversation.message.delta
public class ConversationMessageDeltaEvent extends BaseEvent {
  @JsonProperty("event_type")
  @Builder.Default
  private final String eventType = EventType.CONVERSATION_MESSAGE_DELTA;

  @JsonProperty("data")
  private Message data;
}
