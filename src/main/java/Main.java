import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Main {

    static class HH {
        List<Job> items;

        public List<Job> getItems() {
            return items;
        }

        public void setItems(List<Job> items) {
            this.items = items;
        }
    }

    static class Job {
        String name;
        String id;

        public void setId(String id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

    }

    public static void main(String[] args) {
        TelegramBot bot = new TelegramBot(System.getenv("BOT_TOKEN"));
        bot.setUpdatesListener(e  -> {
            System.out.println(e);
            e.forEach(it -> {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.hh.ru/vacancies?text=" + it.message().text() + "&area=2"))
                        .build();
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    //System.out.println(response.body());
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    String body = response.body();
                    HH hh = mapper.readValue(body, HH.class);
                    hh.items.subList(0,5).forEach(job -> {
                        bot.execute(new SendMessage(it.message().chat().id(), "Вакансия: " + job.name +
                                "\nhttp://hh.ru/vacancy/" + job.id));
                        System.out.println(job.id + " " + job.name);
                    });
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                }

            });

                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            });
    }
}
