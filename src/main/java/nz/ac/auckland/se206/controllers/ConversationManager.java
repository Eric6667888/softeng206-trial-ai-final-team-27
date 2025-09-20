package nz.ac.auckland.se206.controllers;

import java.util.HashMap;
import java.util.Map;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest.Model;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;

public class ConversationManager {
  private static ConversationManager instance;
  // Store conversations in maps
  private Map<String, ChatCompletionRequest> chatRequests = new HashMap<>();
  private Map<String, StringBuilder> chatHistories = new HashMap<>();
  private Map<String, Boolean> introductionStatus = new HashMap<>();

  public static ConversationManager getInstance() {
    if (instance == null) {
      instance = new ConversationManager();
    }
    return instance;
  }

  // Get the conversation
  public ChatCompletionRequest getChatRequest(String profession) throws ApiProxyException {
    if (!chatRequests.containsKey(profession)) {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      ChatCompletionRequest request =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.5)
              .setModel(Model.GPT_4_1_MINI)
              .setMaxTokens(100);

      chatRequests.put(profession, request);
      chatHistories.put(profession, new StringBuilder());
      introductionStatus.put(profession, false);
    }
    return chatRequests.get(profession);
  }

  public String getChatHistory(String profession) {
    return chatHistories.getOrDefault(profession, new StringBuilder()).toString();
  }

  public void appendToHistory(String profession, String message) {
    chatHistories.computeIfAbsent(profession, k -> new StringBuilder()).append(message);
  }

  public boolean hasIntroduced(String profession) {
    return introductionStatus.getOrDefault(profession, false);
  }

  public void markAsIntroduced(String profession) {
    introductionStatus.put(profession, true);
  }
}
