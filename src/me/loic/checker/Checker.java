package me.loic.checker;

import me.loic.checker.utils.DiscordHook;
import me.loic.checker.utils.DiscordHook.EmbedObject;
import me.loic.checker.utils.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Checker {

    //https://www.amazon.fr/JBL-Enceinte-Bluetooth-Rechargeable-Compatible/dp/B07SVH63PX/ref=sr_1_3?__mk_fr_FR=%C3%85M%C3%85%C5%BD%C3%95%C3%91&keywords=flip+5&qid=1575287949&sr=8-3

    private String amazonUrl, discordWebhookUrl;
    private double requestedPrice;
    private double lastPrice;
    private long delay;

    public static void main(String[] args) {
        new Checker();
    }

    public Checker() {
        Scanner scanner = new Scanner(System.in);

        //link
        String input;
        do {
            Logger.log("Item link:");
            input = scanner.next();
            if (!input.startsWith("https://www.amazon.")) {
                Logger.log("Please enter a valid Amazon link");
            } else {
                amazonUrl = input;
            }
        } while (amazonUrl == null);

        //price
        double price;
        do {
            Logger.log("Requested price: ");
            while (!scanner.hasNextDouble()) {
                Logger.log(scanner.next() + " is not a valid number.");
            }
            price = scanner.nextDouble();
        } while (price < 0);
        requestedPrice = price;

        //delay
        int minutes;
        do {
            Logger.log("Delay between requests (in minutes): ");
            while (!scanner.hasNextInt()) {
                Logger.log(scanner.next() + " is not a valid number.");
            }
            minutes = scanner.nextInt();
        } while (minutes < 0);
        delay = TimeUnit.MINUTES.toMillis(minutes);

        //use of discord webhook
        Boolean useDiscord = null;
        do {
            Logger.log("Would you like to be notified by a Discord WebHook ? (y/n): ");
            input = scanner.next();
            while (!Arrays.asList("y", "n").contains(input.toLowerCase())) {
                Logger.log("Please answer by \'y\' or \'n\'");
            }
            useDiscord = (input.equalsIgnoreCase("y") ? true : false);
        } while (useDiscord == null);

        if (useDiscord) {
            //discord webhook
            do {
                Logger.log("Discord WebHook link:");
                input = scanner.next();
                if(!input.startsWith("https://discordapp.com/api/webhooks/")) {
                    Logger.log("Please enter a valid Discord WebHook link");
                } else {
                    discordWebhookUrl = input;
                }
            } while (discordWebhookUrl == null);
        }

        Logger.log("Starting scan on " + amazonUrl + " with a request every " + minutes + " minutes");

        //request loop
        while (true) {
            try {
                doRequest();
                Thread.sleep(delay);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void doRequest() throws IOException {
        Logger.log("Sending request...");
        Document document = Jsoup.connect(amazonUrl).userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36").get();
        Element title = document.select("#productTitle").first();
        Element price = document.select("#priceblock_ourprice").first();
        Logger.log("Found Price: " + price.text());

        Double formattedPrice = Double.valueOf(price.text().replace(" €", "").replace(",", "."));
        if (formattedPrice < requestedPrice && formattedPrice != lastPrice) {
            Logger.log("Price changed for " + title.text() + ": " + price.text());
            Logger.log("URL: " + amazonUrl);
            if (discordWebhookUrl != null) {
                DiscordHook discordHook = new DiscordHook(discordWebhookUrl);
                EmbedObject embedObject = new EmbedObject();
                embedObject.setColor(Color.green);
                embedObject.setTitle("Item price changed");
                embedObject.addField("Item", title.text(), false);
                embedObject.addField("New price", price.text(), false);
                embedObject.addField("Link", amazonUrl, false);
                discordHook.addEmbed(embedObject);
                discordHook.execute();
            }
        }
        lastPrice = formattedPrice;
    }
}
