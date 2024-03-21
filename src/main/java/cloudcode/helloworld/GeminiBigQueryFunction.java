/* Copyright 2022 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
*/
package cloudcode.helloworld;
import java.io.BufferedWriter;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Blob;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.generativeai.preview.ContentMaker;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.preview.PartMaker;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseStream;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.stream.Collectors;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * This class is a Cloud Function that uses the Vertex AI Generative AI API to generate text responses to user prompts.
 * The function takes a JSON object as input, which contains the prompt string and the maximum number of responses to generate.
 * The function then uses the Vertex AI Generative AI API to generate the responses.
 * The function finally returns the list of generated text responses.
 */
 
public class GeminiBigQueryFunction implements HttpFunction {
    private static final Gson gson = new Gson();

  public void service(final HttpRequest request, final HttpResponse response) throws Exception {
    final BufferedWriter writer = response.getWriter();
   // Get the request body as a JSON object.
    JsonObject requestJson = new Gson().fromJson(request.getReader(), JsonObject.class);
	JsonArray calls_array = requestJson.getAsJsonArray("calls");
	JsonArray calls = (JsonArray) calls_array.get(0);
	String baseline_url = calls.get(0).toString().replace("\"", "");
    String test_url = calls.get(1).toString().replace("\"", "");
    String prompt_string = calls.get(2).toString().replace("\"", "");
	String raw_result = validate(baseline_url, test_url, prompt_string);
    raw_result = raw_result.replace("\n","");
    String trimmed = raw_result.trim();
    List<String> result_list = Arrays.asList(trimmed);
    Map<String, List<String>> stringMap = new LinkedHashMap<>();
    stringMap.put("replies", result_list);
    // Serialization
    String return_value = gson.toJson(stringMap);
    writer.write(return_value);
  }

/*
 * This function validates the input prompt string and then uses the model to generate the responses.
 * The function first checks if the prompt string is empty or null. If it is, the function returns an error message.
 * Otherwise, the function uses the model to generate the responses. The function then returns the list of generated text responses.
 * 
 */
public String validate(String baseline_url, String test_url, String prompt_string) throws IOException{
  String res = "";
    try (VertexAI vertexAi = new VertexAI("YOUR_PROJECT", "us-central1"); ) {
      GenerationConfig generationConfig =
          GenerationConfig.newBuilder()
              .setMaxOutputTokens(2048)
              .setTemperature(0.4F)
              .setTopK(32)
              .setTopP(1)
              .build();			  
	GenerativeModel model = new GenerativeModel("gemini-pro-vision", generationConfig, vertexAi);
    String context = prompt_string;    
    Content content = ContentMaker.fromMultiModalData(
     context,
     PartMaker.fromMimeTypeAndData("image/png", readImageFile(baseline_url)),
	 PartMaker.fromMimeTypeAndData("image/png", readImageFile(test_url))
    );
	GenerateContentResponse response = model.generateContent(content);
     res = ResponseHandler.getText(response);
  }catch(Exception e){
	System.out.println(e);
  }
  return res;
}

  // Reads the image data from the given URL.
  public static byte[] readImageFile(String url) throws IOException {
    URL urlObj = new URL(url);
    HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
    connection.setRequestMethod("GET");
    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      InputStream inputStream = connection.getInputStream();
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      return outputStream.toByteArray();
    } else {
      throw new RuntimeException("Error fetching file: " + responseCode);
    }
  } 
}
