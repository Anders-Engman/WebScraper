package cochrane_andersengman;

import java.net.URL;

public class ResultsObject {
    private URL studyURL;
    private String topic;
    private String studyTitle;
    private String authorship;
    private String date;

    public ResultsObject(URL studyURL, String topic, String studyTitle, String authorship, String date) {
        this.studyURL = studyURL;
        this.topic = topic;
        this.studyTitle = studyTitle;
        this.authorship = authorship;
        this.date = date;
    }

    public void setStudyURL(URL studyURL) {
        this.studyURL = studyURL;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setStudyTitle(String studyTitle) {
        this.studyTitle = studyTitle;
    }

    public void setAuthorship(String authorship) {
        this.authorship = authorship;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public URL getStudyURL() {
        return studyURL;
    }

    public String getTopic() {
        return topic;
    }

    public String getStudyTitle() {
        return studyTitle;
    }

    public String getAuthorship() {
        return authorship;
    }

    public String getDate() {
        return date;
    }
}
