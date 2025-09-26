package nz.ac.auckland.se206.controllers;

import java.util.HashMap;
import java.util.Map;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest.Model;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;

public class ConversationManager {
  public static ConversationManager getInstance() {
    if (instance == null) {
      instance = new ConversationManager();
    }
    return instance;
  }

  private static ConversationManager instance;
  // Store conversations in maps
  private Map<String, ChatCompletionRequest> chatRequests = new HashMap<>();
  private Map<String, StringBuilder> chatHistories = new HashMap<>();
  private Map<String, Boolean> introductionStatus = new HashMap<>();

  public ChatCompletionRequest getChatRequest(String profession) throws ApiProxyException {
    // Check if chat has been made with character
    if (!chatRequests.containsKey(profession)) {
      // Configure LLM settings for response
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      ChatCompletionRequest request =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.5)
              .setModel(Model.GPT_4_1_MINI)
              .setMaxTokens(100);
      // Get needed data to send to LLM
      chatRequests.put(profession, request);
      chatHistories.put(profession, new StringBuilder());
      introductionStatus.put(profession, false);
    }

    return chatRequests.get(profession);
  }

  // Get cross-chat context message to add before user messages
  public ChatMessage getCrossChatContextMessage(String currentProfession) {
    String crossChatContext = buildCrossChatContext(currentProfession);
    if (!crossChatContext.isEmpty()) {
      return new ChatMessage("system", crossChatContext);
    }
    return null;
  }

  // Build context from all other conversations for cross-chat memory
  private String buildCrossChatContext(String currentProfession) {
    StringBuilder contextBuilder = new StringBuilder();
    // Check if previous context required from other chats, if not append to message context
    if (!chatHistories.isEmpty()) {
      contextBuilder.append("PREVIOUS CONVERSATIONS CONTEXT:\n");
      contextBuilder.append(
          "You have access to information from previous conversations with other characters. ");
      contextBuilder.append(
          "Use this context to reference what happened in other chats when asked.\n\n");
      // Go through map to get chat history
      for (Map.Entry<String, StringBuilder> entry : chatHistories.entrySet()) {
        String profession = entry.getKey();
        String history = entry.getValue().toString();
        // Tell LLM who conversation is with so chat history stored with each character
        if (!profession.equals(currentProfession) && !history.trim().isEmpty()) {
          contextBuilder
              .append("=== CONVERSATION WITH ")
              .append(profession.toUpperCase())
              .append(" ===\n");
          contextBuilder.append(history).append("\n\n");
        }
      }

      contextBuilder.append("END OF PREVIOUS CONVERSATIONS CONTEXT\n");
      contextBuilder.append(
          "You can reference information from these conversations when relevant.\n\n");
    }

    return contextBuilder.toString();
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

  public void clearAllConversations() {
    chatRequests.clear();
    chatHistories.clear();
    introductionStatus.clear();
  }
}
