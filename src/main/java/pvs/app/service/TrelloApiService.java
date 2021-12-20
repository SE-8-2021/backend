package pvs.app.service;

import com.google.gson.Gson;
import kong.unirest.CookieSpecs;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import pvs.app.service.data.TrelloData;

import java.util.Objects;

@Service
@SuppressWarnings("squid:S1192")
public class TrelloApiService {
    private final String trelloApiKey, trelloApiToken;
    private final String trelloApiBaseUrl;

    public TrelloApiService() {
        this.trelloApiKey = System.getenv("PVS_TRELLO_KEY");
        this.trelloApiToken = System.getenv("PVS_TRELLO_TOKEN");
        this.trelloApiBaseUrl = "https://api.trello.com/1/";
        Unirest.config().cookieSpec(CookieSpecs.IGNORE_COOKIES);
    }

    public String getBoardsFromTrello() {
        HttpResponse<String> response = Unirest.get(trelloApiBaseUrl+ "members/me/boards?fields=name,url&key=" + trelloApiKey + "&token=" + trelloApiToken)
                .header("Accept", "application/json")
                .asString();

        return response.getBody();
    }

    public String getBoard(String url) {
        JSONArray jsonArray = new JSONArray(getBoardsFromTrello());
        String id = "";
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (Objects.equals(jsonObject.getString("url"), url)) {
                id = jsonObject.getString("id");
                break;
            }
        }
        return id;
    }

    public String getDataOfBoard(String ID, String dataCategory) {
        HttpResponse<String> response = Unirest.get(trelloApiBaseUrl+ "boards/" + ID + "/" + dataCategory + "?&key=" + trelloApiKey + "&token=" + trelloApiToken)
                .header("Accept", "application/json")
                .asString();

        return response.getBody();
    }

    public String getDataOfList(String ID, String dataCategory) {
        HttpResponse<String> response = Unirest.get(trelloApiBaseUrl + "lists/" + ID + "/" + dataCategory + "?&key=" + trelloApiKey + "&token=" + trelloApiToken)
                .header("Accept", "application/json")
                .asString();

        return response.getBody();
    }

    public String generate_data(String url) {
        String id = getBoard(url);
        JSONArray listsOfBoard = new JSONArray(getDataOfBoard(id, "lists"));
        TrelloData trelloData = new TrelloData();
        for (int i = 0; i < listsOfBoard.length(); i++) {
            JSONObject list = listsOfBoard.getJSONObject(i);
            JSONArray cardsInList = new JSONArray(getDataOfList(list.getString("id"), "cards"));
            String label = cardsInList.length() + "/" + cardsInList.length();
            TrelloData.Lane lane = trelloData.createLane(list.getString("id"), list.getString("name"), label, 280);
            for (int j = 0; j < cardsInList.length(); j++) {
                JSONObject cardJsonObject = cardsInList.getJSONObject(j);
                TrelloData.Card card = new TrelloData.Card(cardJsonObject.getString("id"), cardJsonObject.getString("name"), "", cardJsonObject.getString("desc"));
                lane.addCard(card);
            }
            trelloData.addLane(lane);
        }
        Gson gson = new Gson();
        return gson.toJson(trelloData);
    }

    public String getAvatarURL() {
        HttpResponse<String> response = Unirest.get(trelloApiBaseUrl+ "members/me/?fields=avatarUrl&key=" + trelloApiKey + "&token=" + trelloApiToken)
                .header("Accept", "application/json")
                .asString();
        JSONObject jsonObject = new JSONObject(response.getBody());
        return jsonObject.getString("avatarUrl");
    }
}
