package cochrane_andersengman;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import sun.rmi.rmic.Main;

import javax.xml.transform.Result;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class GetInfo {

    // Takes in URL, creates document
    public Document urlToDocument(String url, Connection.Response response) throws IOException {
        String userAgent = userAgentGenerator();

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("cookie", response.cookies().toString());
        httpget.setHeader(HttpHeaders.USER_AGENT, userAgent);

        HttpResponse httpresponse = httpclient.execute(httpget);
        HttpEntity responseEntity = httpresponse.getEntity();
        String content = EntityUtils.toString(responseEntity);

        Document pageDoc = Jsoup.parse(content);

        return pageDoc;
    }

    // Creates Session Cookies
    public Connection.Response cookieGenerator() throws IOException {
        String loginPageURL = "https://www.cochranelibrary.com/c/portal/login";

        System.out.println("Creating Cookies to Establish Cochrane Library Session.");

        Connection.Response initialPing = Jsoup.connect(loginPageURL)
                .method(Connection.Method.GET)
                .execute();

        Connection.Response secondaryPing = Jsoup.connect(loginPageURL)
                .cookies(initialPing.cookies())
                .method(Connection.Method.POST)
                .execute();

        Connection.Response fullSessionCookies = Jsoup.connect(loginPageURL)
                .cookies(secondaryPing.cookies())
                .execute();

        System.out.println("Cookies Created, Now Preparing for Page Scraping.");
        System.out.println();

        return fullSessionCookies;
    }

    // Chooses a user agent profile for get requests
    public String userAgentGenerator() {
        String[] userAgentChoices = {"Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0)",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 13_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.1 Mobile/15E148 Safari/604.1",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36 OPR/38.0.2220.41",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36"};

        int randomNum = (int )(Math.random() * 3 + 0);

        String userClientChoice = userAgentChoices[randomNum];

        return userClientChoice;
    }

    // extracts urls from pagination section of Topic Pages
    public String[] extractURLSFromPage(String url, String attributeType, String urlStub) throws MalformedURLException, IOException {

        Document document = Jsoup.parse(new URL(url).openStream(), "ISO-8859-1", url);

        Elements testEl = document.getElementsByAttributeValueStarting(attributeType, urlStub);

        String[] elementsSplit = testEl.toString().split("</a>");

        return elementsSplit;
    }


    // grabs topic titles and urls for user topic selection
    public ArrayList<TopicObject> menuInitialization() throws MalformedURLException, IOException {
        String url = "https://www.cochranelibrary.com/cdsr/reviews/topics";
        String attributeType = "href";
        String urlStub = "https://www.cochranelibrary.com/en/search?";

        String[] elementsSplit = extractURLSFromPage(url, attributeType, urlStub);

        ArrayList<TopicObject> extractedURLs = new ArrayList<TopicObject>();

        for (String element : elementsSplit) {
            String urlString = element.substring(element.indexOf("=\"") + 2, element.lastIndexOf("Topics\">") + 6);
            String topicString = element.substring(element.lastIndexOf("link\">") + 6, element.lastIndexOf("</button>"));
            String unescapedURLString = Parser.unescapeEntities(urlString, true);
            String unescapedTopicString = Parser.unescapeEntities(topicString, true);
            URL unescapedURL = new URL(unescapedURLString);

            TopicObject fullTopicObject = new TopicObject(unescapedTopicString, unescapedURL);

            extractedURLs.add(fullTopicObject);
        }

        System.out.println();
        System.out.println("Cochrane Library Scraper:");
        System.out.println("Study Topics Listed Below");
        System.out.println();

        return extractedURLs;
    }

    public ArrayList<String> pullURLsFromPagination(String urlString) throws MalformedURLException, IOException, InterruptedException {
        ArrayList<String> paginationURLList = new ArrayList<>();

        System.out.println("Preparing for Page Capture.");

        String userAgent = userAgentGenerator();

        Document tempDoc = Jsoup.connect(urlString).userAgent(userAgent).execute().parse();

        String tempDocTitle = tempDoc.title();

        Elements pageElements = tempDoc.getElementsByClass("pagination-page-list-item");

        for(Element element : pageElements) {
            String nextPage = element.select("a[href]").attr("href").toString();

            nextPage = nextPage.replace("/en", "");

            paginationURLList.add(nextPage);
        }

        System.out.println();

        return paginationURLList;
    }

    public ArrayList<ResultsObject> captureAllStudiesOnSinglePage(Elements authorList, Elements resultsList, Elements dateList, String topicString) throws MalformedURLException {
        ArrayList<ResultsObject> allStudiesOnSinglePageArray = new ArrayList<ResultsObject>();

        for (int i = 0; i < authorList.size(); i++) {
            String authorString = authorList.get(i).toString();
            String trimmedAuthor = authorString.substring(authorString.indexOf("<div>") + 5, authorString.indexOf("</div>"));
            trimmedAuthor = trimmedAuthor.trim();

            String titleString = resultsList.get(i).toString();
            String trimmedTitle = titleString.substring(titleString.lastIndexOf("\">") + 2, titleString.lastIndexOf("</a>"));

            String dateString = dateList.get(i).toString();
            dateString = dateString.substring(dateString.indexOf("<div>") + 5, dateString.indexOf("</div>"));
            dateString = dateString.trim();
            dateString = dateString.replaceAll(" ", "-");

            String studyURLString = titleString.substring(titleString.lastIndexOf("href="), titleString.lastIndexOf("\">"));
            studyURLString = studyURLString.replace("href=\"", "https://www.cochranelibrary.com");
            URL studyURL = new URL(studyURLString);

            ResultsObject foundResults = new ResultsObject(studyURL, topicString, trimmedTitle, trimmedAuthor, dateString);
            allStudiesOnSinglePageArray.add(foundResults);
        }

        return allStudiesOnSinglePageArray;
    }

    public ArrayList<ResultsObject> extractInformationFromURLDocument(Document targetDoc, TopicObject topicObject) throws MalformedURLException {
        ArrayList<ResultsObject> studyInformationList = new ArrayList<ResultsObject>();

        Elements authorList = targetDoc.getElementsByClass("search-result-authors");
        Elements resultsList = targetDoc.getElementsByClass("result-title");
        Elements dateList = targetDoc.getElementsByClass("search-result-date");
        String topicString = topicObject.getTopicName();

        studyInformationList = captureAllStudiesOnSinglePage(authorList, resultsList, dateList, topicString);

        return studyInformationList;
    }

    public ArrayList<ResultsObject> mergeResultsObjectsLists(ArrayList<ResultsObject> primaryList, ArrayList<ResultsObject> secondaryList) {
        for (ResultsObject firstPageObj : secondaryList) {
            primaryList.add(firstPageObj);
        }

        return primaryList;
    }

    public ArrayList<ResultsObject> captureAllStudiesOfSpecificTopic(ArrayList<String> urlArray, TopicObject topicObject, Connection.Response cookieResponse) throws IOException, InterruptedException {
        ArrayList<ResultsObject> resultsArray = new ArrayList<ResultsObject>();
        ArrayList<ResultsObject> temporaryArray = new ArrayList<ResultsObject>();
        MainControl tempControl = new MainControl();
        int iterator = 1;

        System.out.println("Page Capture Initialized.");

        for (String url : urlArray) {
            System.out.println("Capturing Page " + iterator);
            // Take url, make document
            Document tempDocument = urlToDocument(url, cookieResponse);
            // Take doc, retrieve data
            temporaryArray = extractInformationFromURLDocument(tempDocument, topicObject);
            // Add Results to Array that will be returned
            mergeResultsObjectsLists(resultsArray, temporaryArray);
            System.out.println("Page " + iterator + " has been captured.");
            iterator++;
        }

        System.out.println();
        System.out.println("All pages related to " + topicObject.getTopicName() + " have been captured.");
        System.out.println();

        return resultsArray;
    }
}
