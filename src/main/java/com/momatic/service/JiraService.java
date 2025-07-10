package com.momatic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class JiraService {

    @Value("${jira.base-url}")   private String baseUrl;
    @Value("${jira.email}")      private String email;
    @Value("${jira.api-token}")  private String apiToken;
    @Value("${jira.project-key}")private String projectKey;
    @Value("${jira.issue-type}") private String issueType;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public void createIssue(String summary, String description) {

        String url = baseUrl + "/rest/api/3/issue";
        String auth = Credentials.basic(email, apiToken);

        ObjectNode body = mapper.createObjectNode();
        ObjectNode fields = body.putObject("fields");
        fields.putObject("project").put("key", projectKey);
        fields.put("summary", summary);
        fields.putObject("issuetype").put("name", issueType);

        ObjectNode desc = mapper.createObjectNode();
        desc.put("type", "doc");
        desc.put("version", 1);
        ObjectNode paragraph = desc.putArray("content")
                .addObject()
                .put("type", "paragraph");
        paragraph.putArray("content")
                .addObject()
                .put("type", "text")
                .put("text", description);
        fields.set("description", desc);

        try {
            Request req = new Request.Builder()
                    .url(url)
                    .header("Authorization", auth)
                    .post(RequestBody.create(
                            mapper.writeValueAsBytes(body),
                            MediaType.parse("application/json")))
                    .build();

            try (Response res = client.newCall(req).execute()) {
                log.info("Jira status={} body={}",
                        res.code(),
                        res.body() != null ? res.body().string() : "null");
            }
        } catch (Exception e) {           // JsonProcessingException 포함
            log.error("Jira createIssue error", e);
        }
    }
}
