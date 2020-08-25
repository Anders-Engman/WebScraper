package cochrane_andersengman;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class MainControl {

    public static void writeResultsToTextFile(ArrayList<ResultsObject> exportList) throws IOException {
        FileWriter writer = new FileWriter("cochrane_reviews.txt");

        for (ResultsObject resultsObject: exportList) {
            writer.write(resultsObject.getStudyURL() + " | " + resultsObject.getTopic() + "|" + System.lineSeparator());
            writer.write(resultsObject.getStudyTitle() + " | " + System.lineSeparator());
            writer.write(resultsObject.getAuthorship() + " | " + resultsObject.getDate() + System.lineSeparator());
            writer.write(String.format("%n"));
        }
        writer.close();
    }

    public static void printMenuItems(ArrayList<TopicObject> topicObjects) {
        int iterator = 1;

        for (TopicObject topic : topicObjects) {
            System.out.println(iterator + " - " + topic.getTopicName());
            iterator++;
        }

        System.out.println();
    }

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        GetInfo newInfo = new GetInfo();
        ArrayList<TopicObject> menuObjectList = newInfo.menuInitialization();
        ArrayList<ResultsObject> specificTopicList = new ArrayList<ResultsObject>();
        Scanner scanner = new Scanner(System.in);

        printMenuItems(menuObjectList);

        System.out.println("Please input the corresponding number of the topic you would like to ");
        System.out.println("scrape all related studies of:");
        int userChoice = scanner.nextInt();

        scanner.close();

        TopicObject userChoiceObject = menuObjectList.get(userChoice - 1);

        System.out.println("You opted to scrape articles related to " + userChoiceObject.getTopicName());
        System.out.println();

        ArrayList<String> urlArray = new ArrayList<String>();

        Connection.Response cookieResponse = newInfo.cookieGenerator();

        String targetURL = userChoiceObject.getUrl().toString();

        Document targetDoc = newInfo.urlToDocument(targetURL, cookieResponse);

        urlArray = newInfo.pullURLsFromPagination(targetURL);

        specificTopicList = newInfo.captureAllStudiesOfSpecificTopic(urlArray, userChoiceObject, cookieResponse);

        writeResultsToTextFile(specificTopicList);

        System.out.println("Info Exported to cochrane_reviews.txt");
    }
}
