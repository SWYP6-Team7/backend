package swyp.swyp6_team7.global.email.template;

import java.util.List;
import java.util.Map;

public interface EmailMessage {

    String getEmail();

    Template getTemplateName();

    String getTitle();

    Map<String, Object> getContext();

    List<String> getRecipients();

    // Companion object equivalent in Java
    public static final String FROM_EMAIL = "noreply@moing.io";
    public static final String FROM_NAME = "모잉";
    public static final String TITLE_PREFIX = "[모잉]";
}