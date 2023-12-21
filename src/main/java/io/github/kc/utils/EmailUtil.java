package io.github.kc.utils;

import io.github.kc.codequality.ApplicationConstants;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class EmailUtil {

    private String host;
    private int port;
    private String userName;
    private String password;

    public EmailUtil(String host, int port, String userName, String password){
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    private EmailUtil(){

    }


    public void sendReportEmailTo(String body, String toEmail, String ccEmail, String subject)
            throws EmailException
    {
        Email email = new SimpleEmail();
        email.setHostName(host);
        email.setAuthentication(userName, password);
        email.setSmtpPort(port);
        email.setFrom("toil-reduction@kc.com");
        email.setSubject(subject);
        email.setContent(body, "text/html; charset=utf-8");
        for(String toEmailEntry : toEmail.split(",")){
            if(! toEmailEntry.equals("")) {
                email.addTo(toEmailEntry);
            }
        }
        for(String ccEmailEntry : ccEmail.split(",")) {
            if (!ccEmailEntry.equals("")) {
                email.addCc(ccEmailEntry);
            }
        }
        email.send();
    }
}
