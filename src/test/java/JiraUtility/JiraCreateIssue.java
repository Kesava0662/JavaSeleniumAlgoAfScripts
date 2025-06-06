package JiraUtility;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;

@SuppressWarnings("all")
@Retention(RetentionPolicy.RUNTIME)
public @interface JiraCreateIssue {
	boolean isCreateIssue();
}
