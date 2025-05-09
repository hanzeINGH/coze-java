package com.coze.openapi.client.websocket.event.downstream;

import com.coze.openapi.client.websocket.common.BaseEvent;
import com.coze.openapi.client.websocket.event.EventType;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.SuperBuilder;

@lombok.Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
// 语音转录完成事件
// event_type: conversation.audio_transcript.completed
public class ConversationAudioTranscriptCompletedEvent extends BaseEvent {
  @JsonProperty("event_type")
  @Builder.Default
  private final String eventType = EventType.CONVERSATION_AUDIO_TRANSCRIPT_COMPLETED;

  @JsonProperty("data")
  private ConversationAudioTranscriptUpdateEvent.Data data;

  @lombok.Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Data {
    @JsonProperty("content")
    private String content;
  }
}
