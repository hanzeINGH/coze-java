package com.coze.openapi.client.websocket.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChatUpdateEventData {

  @JsonProperty("input_audio")
  private InputAudio inputAudio;

  @JsonProperty("output_audio")
  private OutputAudio outputAudio;

  @JsonProperty("chat_config")
  private ChatConfig chatConfig;
}
