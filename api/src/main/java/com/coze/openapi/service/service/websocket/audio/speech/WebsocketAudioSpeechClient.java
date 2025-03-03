package com.coze.openapi.service.service.websocket.audio.speech;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.coze.openapi.client.websocket.event.EventType;
import com.coze.openapi.client.websocket.event.downstream.*;
import com.coze.openapi.client.websocket.event.model.SpeechUpdateEventData;
import com.coze.openapi.client.websocket.event.upstream.*;
import com.coze.openapi.service.service.websocket.common.BaseWebsocketClient;
import com.coze.openapi.service.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.WebSocket;

public class WebsocketAudioSpeechClient extends BaseWebsocketClient {
  private final ObjectMapper objectMapper = Utils.getMapper();
  private final WebsocketAudioSpeechCallbackHandler handler;
  private static final String uri = "/v1/audio/speech";
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  protected WebsocketAudioSpeechClient(
      OkHttpClient client, String wsHost, WebsocketAudioSpeechCreateReq req) {
    super(client, buildUrl(wsHost, uri), req.getCallbackHandler());
    this.handler = req.getCallbackHandler();
  }

  protected static String buildUrl(String wsHost, String uri) {
    return String.format("%s%s", wsHost, uri);
  }

  // 发送语音配置更新事件
  public void speechUpdate(SpeechUpdateEventData data) {
    this.sendEvent(SpeechUpdateEvent.builder().data(data).build());
  }

  // 发送文本缓冲区追加事件
  public void inputTextBufferAppend(String data) {
    this.sendEvent(InputTextBufferAppendEvent.of(data));
  }

  // 发送文本缓冲区完成事件
  public void inputTextBufferComplete() {
    this.sendEvent(new InputTextBufferCompleteEvent());
  }

  @Override
  protected void handleEvent(WebSocket ws, String text) {
    try {
      JsonNode jsonNode = objectMapper.readTree(text);
      String eventType = jsonNode.get("event_type").asText();

      switch (eventType) {
        case EventType.SPEECH_CREATED:
          SpeechCreatedEvent speechCreatedEvent =
              objectMapper.treeToValue(jsonNode, SpeechCreatedEvent.class);
          handler.onSpeechCreated(this, speechCreatedEvent);
          break;
        case EventType.SPEECH_UPDATED:
          SpeechUpdatedEvent speechUpdatedEvent =
              objectMapper.treeToValue(jsonNode, SpeechUpdatedEvent.class);
          handler.onSpeechUpdated(this, speechUpdatedEvent);
          break;
        case EventType.SPEECH_AUDIO_UPDATE:
          SpeechAudioUpdateEvent audioUpdateEvent =
              objectMapper.treeToValue(jsonNode, SpeechAudioUpdateEvent.class);
          handler.onSpeechAudioUpdate(this, audioUpdateEvent);
          break;
        case EventType.SPEECH_AUDIO_COMPLETED:
          SpeechAudioCompletedEvent audioCompletedEvent =
              objectMapper.treeToValue(jsonNode, SpeechAudioCompletedEvent.class);
          handler.onSpeechAudioCompleted(this, audioCompletedEvent);
          break;
        case EventType.INPUT_TEXT_BUFFER_COMPLETED:
          InputTextBufferCompletedEvent bufferCompletedEvent =
              objectMapper.treeToValue(jsonNode, InputTextBufferCompletedEvent.class);
          handler.onInputTextBufferCompleted(this, bufferCompletedEvent);
          break;
        case EventType.ERROR:
          ErrorEvent errorEvent = objectMapper.treeToValue(jsonNode, ErrorEvent.class);
          handler.onError(this, errorEvent);
          break;
        default:
          System.out.println("未知事件类型: " + eventType);
      }
    } catch (Exception e) {
      handler.onClientException(this, new RuntimeException(e));
    }
  }

  public void close() {
    this.close();
    executorService.shutdown();
  }
}
