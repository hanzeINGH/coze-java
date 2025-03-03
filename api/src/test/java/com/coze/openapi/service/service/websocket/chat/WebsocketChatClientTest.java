package com.coze.openapi.service.service.websocket.chat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.coze.openapi.client.websocket.event.downstream.ConversationAudioDeltaEvent;
import com.coze.openapi.service.utils.Utils;

import okhttp3.OkHttpClient;
import okhttp3.WebSocket;

class WebsocketChatClientTest {

  @Mock private OkHttpClient mockOkHttpClient;

  @Mock private WebSocket mockWebSocket;

  @Mock private WebsocketChatCallbackHandler mockCallbackHandler;

  private WebsocketChatClient client;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(mockOkHttpClient.newWebSocket(any(), any())).thenReturn(mockWebSocket);

    WebsocketChatCreateReq req = new WebsocketChatCreateReq("test-bot-id", mockCallbackHandler);
    client = new WebsocketChatClient(mockOkHttpClient, "ws://test.com", req);
  }

  @Test
  void testHandleAudioDeltaEvent() {
    // 准备模拟的音频数据
    String audioData = "base64EncodedAudioData";
    ConversationAudioDeltaEvent event =
        ConversationAudioDeltaEvent.builder()
            //                .data(Message.builder().audio(audioData).build())
            .build();

    // 模拟 WebSocket 接收到消息
    String eventJson = Utils.toJson(event);
    client.handleEvent(mockWebSocket, eventJson);

    // 验证回调处理器被正确调用
    verify(mockCallbackHandler)
        .onConversationAudioDelta(eq(client), any(ConversationAudioDeltaEvent.class));
  }

  @Test
  void testSendEvent() {
    // 捕获发送到 WebSocket 的数据
    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

    // 调用方法发送音频数据
    client.inputAudioBufferAppend("test-audio-data");

    // 验证 WebSocket.send() 被调用，并捕获发送的消息
    verify(mockWebSocket).send(messageCaptor.capture());

    // 验证发送的消息格式正确
    String sentMessage = messageCaptor.getValue();
    //        assertThat(sentMessage).contains(EventType.INPUT_AUDIO_BUFFER_APPEND);
  }

  @Test
  void testClose() {
    client.close();

    // 验证 WebSocket 被正确关闭
    verify(mockWebSocket).close(eq(1000), isNull());
  }

  @Test
  void testHandleUnknownEvent() {
    String unknownEventJson =
        "{\"event_type\":\"conversation.audio.delta\",\"id\":\"cd471e11-9a77-4f89-942d-da4650563fe1\",\"data\":{\"id\":\"7476315154641125428\",\"conversation_id\":\"7476315134009507875\",\"bot_id\":\"7433626593467514921\",\"role\":\"assistant\",\"type\":\"answer\",\"content\":\"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FQANABIAFAAPABQADgASABgAFAAWABcAFQAaABsAGwAdACQAIgAiACYAJAAnACUAKwAkACUAJQArAC0AMQAxACsALQAvADYAMAAtADMAMQAxADgAOgA1ADoANgA3ADQAPgBBAD0APwA9AEAAOgA9AD4AQgBDAEUASQBMAE4ATwBIAEoASgBLAE8ATgBLAFAATwBOAEsASQBBAEAARABDAEIARABAAEYATgBQAE4ATQBRAE0ASABMAEoATQBLAEoAUQBKAEcASgBMAEcARQA/AEcARAA/AD4APgA7AEMAQQA0AD4AOgA/AEIARwBDAEAARgBGAEsATQBGAEcASABEAEcARABCAEIAPwA9AEAAQABCADsANwA4ADgAPAA9AD0APAA/ADwAPgBBADgAMQA6ADoAOQA3ADUANQAxAC0AKgAqAC0AKQAnACUAIgAjACkAJQAlACIAIAAlACEAHwAfACQAIgAfACAAHQAaABYAFgAVABIADwAPAA4ACgADAAgADAAPABAAEwASABAAEgAQABUAEgARABAADwAPAAgADAAHAAcABAAIAAYABgAAAAAAAAAAAP///f8AAP//AwADAAEA+//8//n/9P/6//b/+f/4//X/9v/x//P/+P/1//f/+v/6//z//P/3//3/+//6//3/+//2//b/+f/7//n/+v/7//b/9v/0//X/9P/0//X/8//0//f/+f/5//X/9P/0//b/+P/0//T/9v/3//X/9P/v/+v/6//w/+3/7f/v/+j/8P/t/+z/7f/u/+7/6f/k/+//7P/r/+j/6v/w/+L/4P/j/9z/5P/j/9n/1v/S/9H/zv/P/8z/yP/K/8f/wv/M/8v/yf/L/8//0P/L/8n/yv/N/9P/0P/P/8v/zP/V/8z/xP/L/8z/x//K/8n/yv/J/8j/y//J/8z/y//N/8z/x//M/87/y//O/8//0v/T/9r/1v/N/9b/1f/Q/9H/0v/R/8//zf/R/83/yv/M/8//0f/N/9P/0f/P/9H/1f/V/9H/2P/U/9f/1v/b/9z/2P/V/9b/1f/S/9b/0f/O/87/0P/R/9f/2P/f/+H/3P/g/+H/2//Z/93/4//h/9z/3P/d/+b/5f/i/93/3v/a/93/4f/d/9//3//d/9//4//e/+D/5f/m/+b/5f/q/+z/5v/k/+n/6f/l/+H/5P/n/+v/6P/m/+r/5//u//D/6P/y//D/7//s//P/9f/z//T/9f/z/+z/7//x//n/+P/w//n/9//4//z/AAD7//n////+//r/9f8AAAAA+//7/wEAAAD/////AAAGAAIABwADAAUABQAGAAUABgAHAAoADwAPABEAEQAPAA8AEAAUABIADQAOABcAGQAXABcAFAAYABkAGAAZABcAHAAcACAAIAAgACIAIgAiACEAIAAmAB8AHgAlACUAKwAlACYAJQAnACkAKQArACcAJQAoADMALAAuACwAKQArACsAKQA0ADEAKwArACEALAAtAC0AKAAqACYAKwAvAC0AKwAvACgAKgArACYAKwAqAC4ALQAuADIAKgAtACoAKQAqADAAMgAvACwALgAnAC0ALQAtACsAMQAoACoA\",\"content_type\":\"audio\",\"chat_id\":\"7476308073020276770\",\"section_id\":\"7476315134009507875\"},\"detail\":{\"logid\":\"02174071526740100000000000000000000ffff0a807486d6926f\"}}\n";
    client.handleEvent(mockWebSocket, unknownEventJson);

    // 验证未知事件类型被正确处理
    verifyNoInteractions(mockCallbackHandler);
  }

  @Test
  void testHandleInvalidJson() {
    String invalidJson = "invalid json";
  }
}
