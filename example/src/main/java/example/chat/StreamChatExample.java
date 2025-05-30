package example.chat;

import java.util.Collections;

import com.coze.openapi.client.chat.CreateChatReq;
import com.coze.openapi.client.chat.model.ChatEvent;
import com.coze.openapi.client.chat.model.ChatEventType;
import com.coze.openapi.client.connversations.message.model.Message;
import com.coze.openapi.client.connversations.message.model.MessageType;
import com.coze.openapi.service.auth.TokenAuth;
import com.coze.openapi.service.service.CozeAPI;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

/*
 * This example is about how to use the streaming interface to start a chat request
 * and handle chat events
 * */
public class StreamChatExample {

  public static void main(String[] args) {

    // Get an access_token through personal access token or oauth.
    String token = System.getenv("COZE_API_TOKEN");
    String botID = System.getenv("COZE_BOT_ID");
    String userID = System.getenv("USER_ID");

    TokenAuth authCli = new TokenAuth(token);

    // Init the Coze client through the access_token.
    CozeAPI coze =
        new CozeAPI.Builder()
            .baseURL(System.getenv("COZE_API_BASE"))
            .auth(authCli)
            .readTimeout(10000)
            .build();
    ;

    /*
     * Step one, create chat
     * Call the coze.chat().stream() method to create a chat. The create method is a streaming
     * chat and will return a Flowable ChatEvent. Developers should iterate the iterator to get
     * chat event and handle them.
     * */
    CreateChatReq req =
        CreateChatReq.builder()
            .botID(botID)
            .userID(userID)
            .messages(Collections.singletonList(Message.buildUserQuestionText("What can you do?")))
            .build();

    Flowable<ChatEvent> resp = coze.chat().stream(req);
    resp.subscribeOn(Schedulers.io())
        .subscribe(
            event -> {
              if (ChatEventType.CONVERSATION_MESSAGE_DELTA.equals(event.getEvent())) {
                System.out.print(event.getMessage().getContent());
              }
              if (ChatEventType.CONVERSATION_CHAT_COMPLETED.equals(event.getEvent())) {
                if (MessageType.FOLLOW_UP.equals(event.getMessage().getType())) {
                  System.out.println(event.getMessage().getContent());
                } else {
                  System.out.println("Token usage:" + event.getChat().getUsage().getTokenCount());
                }
              }
              if (ChatEventType.DONE.equals(event.getEvent())) {
                coze.shutdownExecutor();
              }
            },
            throwable -> {
              System.err.println("Error occurred: " + throwable.getMessage());
            },
            () -> {
              System.out.println("done");
            });

    // 为了防止程序立即退出，添加一个简单的等待
    try {
      Thread.sleep(5000); // 等待5秒钟
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    coze.shutdownExecutor();
  }
}
