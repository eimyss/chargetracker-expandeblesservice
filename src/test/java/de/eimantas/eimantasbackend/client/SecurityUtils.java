package de.eimantas.eimantasbackend.client;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SecurityUtils {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

  public static String getJsonString() throws IOException {

    logger.info("starting request");
    HttpClient client = new DefaultHttpClient();
    HttpPost request = new HttpPost("https://security.eimantas-services.de/auth/realms/expenses/protocol/openid-connect/token");
    List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    pairs.add(new BasicNameValuePair("client_id", "expenses-app"));
    pairs.add(new BasicNameValuePair("username", "test"));
    pairs.add(new BasicNameValuePair("password", "test"));
    pairs.add(new BasicNameValuePair("grant_type", "password"));
    request.setEntity(new UrlEncodedFormEntity(pairs));
    HttpResponse resp = client.execute(request);

    String response = IOUtils.toString(resp.getEntity().getContent());

    logger.info("entity content:" + response);

    return response;
  }


  public static String getOnlyToken() {

    JSONObject parser = null;
    try {
      parser = new JSONObject(getJsonString());
      return parser.getString("access_token");
    } catch (Exception e) {
      e.printStackTrace();
      return "error";
    }

  }

  public static Iterator getTokenInfo() throws IOException, JSONException {

    JSONObject parser = new JSONObject(getJsonString());
    return parser.keys();

  }

  public static String getValueFromToken(String name) throws IOException, JSONException {

    JSONObject parser = new JSONObject(getJsonString());
    return parser.getString(name);

  }

}
