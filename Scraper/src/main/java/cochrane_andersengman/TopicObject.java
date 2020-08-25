package cochrane_andersengman;

import java.net.URL;

public class TopicObject {
    private String topicName;
    private URL url;

    public TopicObject(String topicName, URL url) {
        this.topicName = topicName;
        this.url = url;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getTopicName() {
        return topicName;
    }

    public URL getUrl() {
        return url;
    }
}
