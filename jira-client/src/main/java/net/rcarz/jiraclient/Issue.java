/**
 * jira-client - a simple JIRA REST client
 * Copyright (c) 2013 Bob Carroll (bob.carroll@alum.rit.edu)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/* Update MMA 2025-05-19: use of Jackson for JSON handling */

package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Represents a JIRA issue.
 */
public class Issue extends Resource {

    private final JsonNodeFactory FACTORY = JsonNodeFactory.instance;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Used to chain fields to a create action.
     */
    public static final class FluentCreate {

        Map<String, Object> fields = new HashMap<>();
        RestClient restclient;
        JsonNode createMeta;

        private FluentCreate(RestClient restClient, JsonNode createMeta) {
            this.restclient = restClient;
            this.createMeta = createMeta;
        }

        /**
         * Executes the create action (issue includes all fields).
         *
         * @throws JiraException when the create fails
         */
        public Issue execute() throws JiraException {
            return executeCreate(null);
        }

        /**
         * Executes the create action and specify which fields to retrieve.
         *
         * @param includedFields Specifies which issue fields will be included
         * in the result.
         * <br>Some examples how this parameter works:
         * <ul>
         * <li>*all - include all fields</li>
         * <li>*navigable - include just navigable fields</li>
         * <li>summary,comment - include just the summary and comments</li>
         * <li>*all,-comment - include all fields</li>
         * </ul>
         *
         * @throws JiraException when the create fails
         */
        public Issue execute(String includedFields) throws JiraException {
            return executeCreate(includedFields);
        }

        /**
         * Executes the create action and specify which fields to retrieve.
         *
         * @param includedFields Specifies which issue fields will be included
         * in the result.
         * <br>Some examples how this parameter works:
         * <ul>
         * <li>*all - include all fields</li>
         * <li>*navigable - include just navigable fields</li>
         * <li>summary,comment - include just the summary and comments</li>
         * <li>*all,-comment - include all fields</li>
         * </ul>
         *
         * @throws JiraException when the create fails
         */
        private Issue executeCreate(String includedFields) throws JiraException {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode fieldMap = mapper.createObjectNode();

            if (fields.isEmpty()) {
                throw new JiraException("No fields were given for create");
            }

            for (Map.Entry<String, Object> ent : fields.entrySet()) {
                JsonNode newVal = (JsonNode) Field.toJson(ent.getKey(), ent.getValue(), createMeta);
                fieldMap.set(ent.getKey(), newVal);
            }

            ObjectNode req = mapper.createObjectNode();
            req.set("fields", fieldMap);

            JsonNode result;

            try {
                result = restclient.post(getRestUri(null), req);
	        } catch (Exception ex)  {
	                throw new JiraException("Failed to create issue", ex);
	        }

            if (result == null || !result.isObject() || !result.has("key") || !result.get("key").isTextual()) {
                throw new JiraException("Unexpected result on create issue");
            }

            String issueKey = result.get("key").asText();
            if (includedFields != null) {
                return Issue.get(restclient, issueKey, includedFields);
            } else {
                return Issue.get(restclient, issueKey);
            }
        }

        /**
         * Appends a field to the update action.
         *
         * @param name Name of the field
         * @param value New field value
         *
         * @return the current fluent update instance
         */
        public FluentCreate field(String name, Object value) {
            fields.put(name, value);
            return this;
        }
    }

    /**
     * Used to {@link #create() create} remote links. Provide at least the {@link #url(String)} or
     * {@link #globalId(String) global id} and the {@link #title(String) title}.
     */
    public static final class FluentRemoteLink {

        final private RestClient restclient;
        final private String key;
        final private ObjectNode request;
        final private ObjectNode object;

        private static final ObjectMapper mapper = new ObjectMapper();

        private FluentRemoteLink(final RestClient restclient, String key) {
            this.restclient = restclient;
            this.key = key;
            request = mapper.createObjectNode();
            object = mapper.createObjectNode();
        }

        /**
         * A globally unique identifier which uniquely identifies the remote application and the remote object within
         * the remote system. The maximum length is 255 characters. This call sets also the {@link #url(String) url}.
         *
         * @param globalId the global id
         * @return this instance
         */
        public FluentRemoteLink globalId(final String globalId) {
            request.put("globalId", globalId);
            url(globalId);
            return this;
        }

        /**
         * A hyperlink to the object in the remote system.
         * @param url A hyperlink to the object in the remote system.
         * @return this instance
         */
        public FluentRemoteLink url(final String url) {
            object.put("url", url);
            return this;
        }

        /**
         * The title of the remote object.
         * @param title The title of the remote object.
         * @return this instance
         */
        public FluentRemoteLink title(final String title) {
            object.put("title", title);
            return this;
        }

        /**
         * Provide an icon for the remote link.
         * @param url A 16x16 icon representing the type of the object in the remote system.
         * @param title Text for the tooltip of the main icon describing the type of the object in the remote system.
         * @return this instance
         */
        public FluentRemoteLink icon(final String url, final String title) {
            ObjectNode icon = object.objectNode();
            icon.put("url16x16", url);
            icon.put("title", title);
            object.set("icon", icon);
            return this;
        }

        /**
         * The status in the remote system.
         * @param resolved if {@code true} the link to the issue will be in a strike through font.
         * @param title Text for the tooltip of the main icon describing the type of the object in the remote system.
         * @param iconUrl Text for the tooltip of the main icon describing the type of the object in the remote system.
         * @param statusUrl A hyperlink for the tooltip of the the status icon.
         * @return this instance
         */
        public FluentRemoteLink status(final boolean resolved, final String iconUrl, final String title, final String statusUrl) {
            ObjectNode status = object.objectNode();
            status.put("resolved", resolved);

            ObjectNode icon = object.objectNode();
            icon.put("title", title);

            if (iconUrl != null) {
                icon.put("url16x16", iconUrl);
            }
            if (statusUrl != null) {
                icon.put("link", statusUrl);
            }

            status.set("icon", icon);
            object.set("status", status);

            return this;
        }

        /**
         * Textual summary of the remote object.
         * @param summary Textual summary of the remote object.
         * @return this instance
         */
        public FluentRemoteLink summary(final String summary) {
            object.put("summary", summary);
            return this;
        }

        /**
         * Relationship between the remote object and the JIRA issue. This can be a verb or a noun.
         * It is used to group together links in the UI.
         * @param relationship Relationship between the remote object and the JIRA issue.
         * @return this instance
         */
        public FluentRemoteLink relationship(final String relationship) {
            request.put("relationship", relationship);
            return this;
        }

        /**
         * The application for this remote link. Links are grouped on the type and name in the UI. The name-spaced
         * type of the application. It is not displayed to the user. Renderering plugins can register to render a
         * certain type of application.
         * @param type The name-spaced type of the application.
         * @param name The human-readable name of the remote application instance that stores the remote object.
         * @return this instance
         */
        public FluentRemoteLink application(final String type, final String name) {
            ObjectNode application = object.objectNode();

            if (type != null) {
                application.put("type", type);
            }
            application.put("name", name);
            request.set("application", application);

            return this;
        }

        /**
         * Creates or updates the remote link if a {@link #globalId(String) global id} is given and there is already
         * a remote link for the specified global id.
         * @throws JiraException when the remote link creation fails
         */
        public void create() throws JiraException {
            try {
                request.set("object", object);
                restclient.post(getRestUri(key) + "/remotelink", request);
            } catch (Exception ex) {
                throw new JiraException("Failed add remote link to issue " + key, ex);
            }
        }
    }

    /**
     * count issues with the given query.
     *
     * @param restClient REST client instance
     *
     * @param jql JQL statement
     *
     * @return the count
     *
     * @throws JiraException when the search fails
     */
    public static int count(RestClient restClient, String jql) throws JiraException {
        JsonNode result;

        try {
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("jql", jql);
            queryParams.put("maxResults", "1");
            URI searchUri = restClient.buildURI(getBaseUri() + "search", queryParams);
            result = restClient.get(searchUri);
        } catch (Exception ex) {
            throw new JiraException("Failed to search issues", ex);
        }

        if (result == null || !result.isObject()) {
            throw new JiraException("JSON payload is malformed");
        }

        JsonNode totalNode = result.get("total");
        if (totalNode == null || !totalNode.isInt()) {
            throw new JiraException("JSON payload missing or invalid 'total' field");
        }

        return totalNode.intValue();
    }

    /**
     * Used to chain fields to an update action.
     */
    public final class FluentUpdate {

        private final Map<String, Object> fields = new HashMap<>();
        private final Map<String, List<ObjectNode>> fieldOpers = new HashMap<>();
        private final JsonNode editmeta;

        private FluentUpdate(JsonNode editmeta) {
            this.editmeta = editmeta;
        }

        /**
         * Executes the update action.
         *
         * @throws JiraException when the update fails
         */
        public void execute() throws JiraException {
            ObjectNode fieldMap = FACTORY.objectNode();
            ObjectNode updateMap = FACTORY.objectNode();

            if (fields.isEmpty() && fieldOpers.isEmpty())
                throw new JiraException("No fields were given for update");

            for (Map.Entry<String, Object> ent : fields.entrySet()) {
                Object newVal = Field.toJson(ent.getKey(), ent.getValue(), editmeta);
                fieldMap.set(ent.getKey(), convertToJsonNode(newVal));
            }

            for (Map.Entry<String, List<ObjectNode>> ent : fieldOpers.entrySet()) {
                Object newVal = Field.toJson(ent.getKey(), ent.getValue(), editmeta);
                updateMap.set(ent.getKey(), convertToJsonNode(newVal));
            }

            ObjectNode req = FACTORY.objectNode();

            if (!fieldMap.isEmpty())
                req.set("fields", fieldMap);

            if (!updateMap.isEmpty())
                req.set("update", updateMap);

            try {
                restclient.put(getRestUri(key), req);
            } catch (Exception ex) {
                throw new JiraException("Failed to update issue " + key, ex);
            }
        }

        /**
         * Appends a field to the update action.
         *
         * @param name Name of the field
         * @param value New field value
         *
         * @return the current fluent update instance
         */
        public FluentUpdate field(String name, Object value) {
            fields.put(name, value);
            return this;
        }

        private FluentUpdate fieldOperation(String oper, String name, Object value) {
            if (!fieldOpers.containsKey(name))
                fieldOpers.put(name, new ArrayList<>());

            // Wrap the value in a Jackson-compatible format using ObjectNode
            ObjectNode operationNode = JsonNodeFactory.instance.objectNode();
            JsonNode valueNode = convertToJsonNode(value); // uses Jackson
            operationNode.set(oper, valueNode);

            fieldOpers.get(name).add(operationNode);

            return this;
        }

        /**
         *  Adds a field value to the existing value set.
         *
         *  @param name Name of the field
         *  @param value Field value to append
         *
         *  @return the current fluent update instance
         */
        public FluentUpdate fieldAdd(String name, Object value) {
            return fieldOperation("add", name, value);
        }

        /**
         *  Removes a field value from the existing value set.
         *
         *  @param name Name of the field
         *  @param value Field value to remove
         *
         *  @return the current fluent update instance
         */
        public FluentUpdate fieldRemove(String name, Object value) {
            return fieldOperation("remove", name, value);
        }

    }

    /**
     * Used to chain fields to a transition action.
     */
    public final class FluentTransition {

        Map<String, Object> fields = new HashMap<>();
        List<Transition> transitions;

        private FluentTransition(List<Transition> transitions) {
            this.transitions = transitions;
        }

        private Transition getTransition(String id, boolean isName) throws JiraException {
            Transition result = null;

            for (Transition transition : transitions) {
                if((isName && id.equals(transition.getName())
                || (!isName && id.equals(transition.getId())))){
                    result = transition;
                }
            }

            if (result == null) {
                final String allTransitionNames = Arrays.toString(transitions.toArray());
                throw new JiraException("Transition '" + id + "' was not found. Known transitions are:" + allTransitionNames);
            }

            return result;
        }

        private void realExecute(Transition trans) throws JiraException {

            if (trans == null || trans.getFields() == null)
                throw new JiraException("Transition is missing fields");

            ObjectNode req = JsonNodeFactory.instance.objectNode();

            if (!fields.isEmpty()) {
                ObjectNode fieldMap = JsonNodeFactory.instance.objectNode();
                for (Map.Entry<String, Object> ent : fields.entrySet()) {
                    JsonNode valueNode = convertToJsonNode(ent.getValue());
                    fieldMap.set(ent.getKey(), valueNode);
                }
                req.set("fields", fieldMap);
            }

            ObjectNode transitionNode = JsonNodeFactory.instance.objectNode();
            transitionNode.put("id", Field.getString(trans.getId()));
            req.set("transition", transitionNode);

            try {
                restclient.post(getRestUri(key) + "/transitions", req);
            } catch (Exception ex) {
                throw new JiraException("Failed to transition issue " + key, ex);
            }
        }

        /**
         * Executes the transition action.
         *
         * @param id Internal transition ID
         *
         * @throws JiraException when the transition fails
         */
        public void execute(int id) throws JiraException {
            realExecute(getTransition(Integer.toString(id), false));
        }

        /**
         * Executes the transition action.
         *
         * @param transition Transition
         *
         * @throws JiraException when the transition fails
         */
        public void execute(Transition transition) throws JiraException {
            realExecute(transition);
        }

        /**
         * Executes the transition action.
         *
         * @param name Transition name
         *
         * @throws JiraException when the transition fails
         */
        public void execute(String name) throws JiraException {
            realExecute(getTransition(name, true));
        }

        /**
         * Appends a field to the transition action.
         *
         * @param name Name of the field
         * @param value New field value
         *
         * @return the current fluent transition instance
         */
        public FluentTransition field(String name, Object value) {
            fields.put(name, value);
            return this;
        }
    }

	/**
     * Iterates over all issues in the query by getting the next page of
     * issues when the iterator reaches the last of the current page.
     */
    private static class IssueIterator implements Iterator<Issue> {
    	private Iterator<Issue> currentPage;
		private final RestClient restclient;
		private Issue nextIssue;
        private Integer maxResults = -1;
		private final String jql;
		private final String includedFields;
		private final String expandFields;
		private Integer startAt;
		private List<Issue> issues;
		private int total;
		
        public IssueIterator(RestClient restclient, String jql,
                String includedFields, String expandFields, Integer maxResults, Integer startAt) {
			this.restclient = restclient;
			this.jql = jql;
			this.includedFields = includedFields;
			this.expandFields = expandFields;
			this.maxResults = maxResults;
			this.startAt = startAt;
        }
    	
//		@Override
		public boolean hasNext() {
			if (nextIssue != null) {
				return true;
			}
			try {
				nextIssue = getNextIssue();
			} catch (JiraException e) {
				throw new RuntimeException(e);
			}
			return nextIssue != null;
		}

//		@Override
		public Issue next() {
			if (! hasNext()) {
				throw new NoSuchElementException();
			}
			Issue result = nextIssue;
			nextIssue = null;
			return result;
		}

//		@Override
		public void remove() {
			throw new UnsupportedOperationException("Method remove() not support for class " + this.getClass().getName());
		}

		/**
		 * Gets the next issue, returning null if none more available
		 * Will ask the next set of issues from the server if the end of the current list of issues is reached.
		 * 
		 * @return the next issue, null if none more available
		 * @throws JiraException
		 */
		private Issue getNextIssue() throws JiraException {
			// first call
			if (currentPage == null) {
				currentPage = getNextIssues().iterator();
                if (currentPage.hasNext()) {
                    return currentPage.next();
                } else {
                    return null;
                }
            }
			
			// check if we need to get the next set of issues
			if (! currentPage.hasNext()) {
				currentPage = getNextIssues().iterator();
			}

			// return the next item if available
			if (currentPage.hasNext()) {
				return currentPage.next();
			} else {
				return null;
			}
		}

		/**
		 * Execute the query to get the next set of issues.
		 * Also sets the startAt, maxMresults, total and issues fields,
		 * so that the SearchResult can access them.
		 * 
		 * @return the next set of issues.
		 * @throws JiraException
		 */
		private List<Issue> getNextIssues() throws JiraException {
			if (issues == null) {
				startAt = 0;
			} else {
				startAt = startAt + issues.size();
			}

            JsonNode result = null;

	        try {
	            URI searchUri = createSearchURI(restclient, jql, includedFields,
						expandFields, maxResults, startAt);
	            result = restclient.get(searchUri);
	        } catch (Exception ex) {
	            throw new JiraException("Failed to search issues", ex);
	        }

	        if (result == null || !result.isObject()) {
	            throw new JiraException("JSON payload is malformed");
	        }

            // Extract values from result
            this.startAt = Field.getInteger(result.get("startAt"));
            this.maxResults = Field.getInteger(result.get("maxResults"));
            this.total = Field.getInteger(result.get("total"));

            JsonNode issuesNode = result.get("issues");
            this.issues = Field.getResourceArray(Issue.class, issuesNode, restclient);

	    	return issues;
		}
    }
    
    /**
     * Issue search results structure.
     */
    public static class SearchResult {
        public int start = 0;
        public int max = 0;
        public int total = 0;
        public List<Issue> issues = null;
		private final RestClient restclient;
		private final String jql;
		private final String includedFields;
		private final String expandFields;
		private Integer startAt;

        public SearchResult(RestClient restclient, String jql,
	            String includedFields, String expandFields, Integer maxResults, Integer startAt) throws JiraException {
			this.restclient = restclient;
			this.jql = jql;
			this.includedFields = includedFields;
			this.expandFields = expandFields;
			initSearchResult(maxResults, start);
        }
        
        private void initSearchResult(Integer maxResults, Integer start) {
            IssueIterator issueIterator = new IssueIterator(restclient, jql, includedFields, expandFields, maxResults, startAt);
        	this.max = issueIterator.maxResults;
        	this.start = issueIterator.startAt;
        	this.issues = issueIterator.issues;
        	this.total = issueIterator.total;
		}
    }

    public static final class NewAttachment {

        private final String filename;
        private final Object content;

        public NewAttachment(File content) {
            this(content.getName(), content);
        }

        public NewAttachment(String filename, File content) {
            this.filename = requireFilename(filename);
            this.content = requireContent(content);
        }

        public NewAttachment(String filename, InputStream content) {
            this.filename = requireFilename(filename);
            this.content = requireContent(content);
        }

        public NewAttachment(String filename, byte[] content) {
            this.filename = requireFilename(filename);
            this.content = requireContent(content);
        }

        String getFilename() {
            return filename;
        }

        Object getContent() {
            return content;
        }

        private static String requireFilename(String filename) {
            if (filename == null) {
                throw new NullPointerException("filename may not be null");
            }
            if (filename.isEmpty()) {
                throw new IllegalArgumentException("filename may not be empty");
            }
            return filename;
        }

        private static Object requireContent(Object content) {
            if (content == null) {
                throw new NullPointerException("content may not be null");
            }
            return content;
        }

    }

    private String key = null;
    private JsonNode fields = null;

    /* system fields */
    private User assignee = null;
    private List<Attachment> attachments = null;
    private ChangeLog changeLog = null;
    private List<Comment> comments = null;
    private List<Component> components = null;
    private String description = null;
    private Date dueDate = null;
    private List<Version> fixVersions = null;
    private List<IssueLink> issueLinks = null;
    private IssueType issueType = null;
    private List<String> labels = null;
    private Issue parent = null;
    private Priority priority = null;
    private Project project = null;
    private User reporter = null;
    private Resolution resolution = null;
    private Date resolutionDate = null;
    private Status status = null;
    private List<Issue> subtasks = null;
    private String summary = null;
    private TimeTracking timeTracking = null;
    private List<Version> versions = null;
    private Votes votes = null;
    private Watches watches = null;
    private List<WorkLog> workLogs = null;
    private Integer timeEstimate = null;
    private Integer timeSpent = null;
    private Date createdDate = null;
    private Date updatedDate = null;

    /**
     * Creates an issue from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Issue(RestClient restclient, JsonNode json) {
        super(restclient);

        if (json != null)
            deserialize(json);
    }

    private void deserialize(JsonNode json) {
        id = Field.getString(json.get("id"));
        self = Field.getString(json.get("self"));
        key = Field.getString(json.get("key"));

        fields = json.get("fields");
        if (fields == null || !fields.isObject()) {
            throw new IllegalArgumentException("Missing or invalid 'fields' node");
        }

        assignee = Field.getResource(User.class, fields.get(Field.ASSIGNEE), restclient);
        attachments = Field.getResourceArray(Attachment.class, fields.get(Field.ATTACHMENT), restclient);
        changeLog = Field.getResource(ChangeLog.class, json.get(Field.CHANGE_LOG), restclient);
        comments = Field.getComments(fields.get(Field.COMMENT), restclient);
        components = Field.getResourceArray(Component.class, fields.get(Field.COMPONENTS), restclient);
        description = Field.getString(fields.get(Field.DESCRIPTION));
        dueDate = Field.getDate(fields.get(Field.DUE_DATE));
        fixVersions = Field.getResourceArray(Version.class, fields.get(Field.FIX_VERSIONS), restclient);
        issueLinks = Field.getResourceArray(IssueLink.class, fields.get(Field.ISSUE_LINKS), restclient);
        issueType = Field.getResource(IssueType.class, fields.get(Field.ISSUE_TYPE), restclient);
        labels = Field.getStringArray(fields.get(Field.LABELS));
        parent = Field.getResource(Issue.class, fields.get(Field.PARENT), restclient);
        priority = Field.getResource(Priority.class, fields.get(Field.PRIORITY), restclient);
        project = Field.getResource(Project.class, fields.get(Field.PROJECT), restclient);
        reporter = Field.getResource(User.class, fields.get(Field.REPORTER), restclient);
        resolution = Field.getResource(Resolution.class, fields.get(Field.RESOLUTION), restclient);
        resolutionDate = Field.getDateTime(fields.get(Field.RESOLUTION_DATE));
        status = Field.getResource(Status.class, fields.get(Field.STATUS), restclient);
        subtasks = Field.getResourceArray(Issue.class, fields.get(Field.SUBTASKS), restclient);
        summary = Field.getString(fields.get(Field.SUMMARY));
        timeTracking = Field.getTimeTracking(fields.get(Field.TIME_TRACKING));
        versions = Field.getResourceArray(Version.class, fields.get(Field.VERSIONS), restclient);
        votes = Field.getResource(Votes.class, fields.get(Field.VOTES), restclient);
        watches = Field.getResource(Watches.class, fields.get(Field.WATCHES), restclient);
        workLogs = Field.getWorkLogs(fields.get(Field.WORKLOG), restclient);
        timeEstimate = Field.getInteger(fields.get(Field.TIME_ESTIMATE));
        timeSpent = Field.getInteger(fields.get(Field.TIME_SPENT));
        createdDate = Field.getDateTime(fields.get(Field.CREATED_DATE));
        updatedDate = Field.getDateTime(fields.get(Field.UPDATED_DATE));
    }

    private static String getRestUri(String key) {
        return getBaseUri() + "issue/" + (key != null ? key : "");
    }

    public static JsonNode getCreateMetadata(
        RestClient restclient, String project, String issueType) throws JiraException {

        JsonNode result;

        try {
        	Map<String, String> params = new HashMap<String, String>();
        	params.put("expand", "projects.issuetypes.fields");
        	params.put("projectIds", project);
        	params.put("issuetypeNames", issueType);
            URI createuri = restclient.buildURI(
                getBaseUri() + "issue/createmeta",
                params);
            result = restclient.get(createuri);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve issue metadata", ex);
        }

        if (result == null || !result.isObject())
            throw new JiraException("JSON payload is malformed");

        JsonNode projectsNode = result.get("projects");

        if (projectsNode == null || !projectsNode.isArray())
            throw new JiraException("Create metadata is malformed");

        List<Project> projects = Field.getResourceArray(Project.class, projectsNode, restclient);

        if (projects.isEmpty() || projects.get(0).getIssueTypes().isEmpty())
            throw new JiraException("Project '"+ project + "'  or issue type '" + issueType + 
                    "' missing from create metadata. Do you have enough permissions?");

        return projects.get(0).getIssueTypes().get(0).getFields();
    }

    private JsonNode getEditMetadata() throws JiraException {
        JsonNode result;

        try {
            result = restclient.get(getRestUri(key) + "/editmeta");
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve issue metadata", ex);
        }

        if (result == null || !result.isObject())
            throw new JiraException("JSON payload is malformed");

        JsonNode fieldsNode = result.get("fields");

        if (fieldsNode == null || !fieldsNode.isObject())
            throw new JiraException("Edit metadata is malformed");

        return fieldsNode;
    }

    public List<Transition> getTransitions() throws JiraException {
        JsonNode result;

        try {
        	Map<String, String> params = new HashMap<>();
        	params.put("expand", "transitions.fields");
            URI transuri = restclient.buildURI(getRestUri(key) + "/transitions",params);
            result = restclient.get(transuri);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve transitions", ex);
        }

        if (result == null || !result.has("transitions") || !result.get("transitions").isArray())
            throw new JiraException("Transition metadata is missing.");

        List<Transition> trans = new ArrayList<>();
        for (JsonNode node : result.get("transitions")) {
            trans.add(new Transition(restclient, node));
        }

        return trans;
    }

    /**
     * Adds an attachment to this issue.
     *
     * @param file java.io.File
     *
     * @throws JiraException when the attachment creation fails
     */
    public void addAttachment(File file) throws JiraException {
        try {
            restclient.post(getRestUri(key) + "/attachments", file);
        } catch (Exception ex) {
            throw new JiraException("Failed add attachment to issue " + key, ex);
        }
    }
    
    /**
     * Adds a remote link to this issue.
     *
     * @param url Url of the remote link
     * @param title Title of the remote link
     * @param summary Summary of the remote link
     *
     * @throws JiraException when the link creation fails
     * @see #remoteLink()
     */
    public void addRemoteLink(String url, String title, String summary) throws JiraException {
        remoteLink().url(url).title(title).summary(summary).create();
    }

    /**
     * Adds a remote link to this issue. At least set the
     * {@link FluentRemoteLink#url(String) url} or
     * {@link FluentRemoteLink#globalId(String) globalId} and
     * {@link FluentRemoteLink#title(String) title} before
     * {@link FluentRemoteLink#create() creating} the link.
     *
     * @return a fluent remote link instance
     */
    public FluentRemoteLink remoteLink() {
        return new FluentRemoteLink(restclient, getKey());
    }

    /**
     * Adds an attachments to this issue.
     *
     * @param attachments  the attachments to add
     *
     * @throws JiraException when the attachments creation fails
     */
    public void addAttachments(NewAttachment... attachments) throws JiraException {
        if (attachments == null) {
            throw new NullPointerException("attachments may not be null");
        }
        if (attachments.length == 0) {
            return;
        }
        try {
            restclient.post(getRestUri(key) + "/attachments", attachments);
        } catch (Exception ex) {
            throw new JiraException("Failed add attachment to issue " + key, ex);
        }
    }

    /**
	 * Removes an attachments.
	 *
	 * @param attachmentId attachment id to remove
	 *
	 * @throws JiraException when the attachment removal fails
	 */
	public void removeAttachment(String attachmentId) throws JiraException {
	
	    if (attachmentId == null) {
	        throw new NullPointerException("attachmentId may not be null");
	    }
	
	    try {
	        restclient.delete(getBaseUri() + "attachment/" + attachmentId);
	    } catch (Exception ex) {
	        throw new JiraException("Failed remove attachment " + attachmentId, ex);
	    }
	}

    /**
     * Adds a comment to this issue.
     *
     * @param body Comment text
     *
     * @throws JiraException when the comment creation fails
     */
    public void addComment(String body) throws JiraException {
        addComment(body, null, null);
    }

    /**
     * Adds a comment to this issue with limited visibility.
     *
     * @param body Comment text
     * @param visType Target audience type (role or group)
     * @param visName Name of the role or group to limit visibility to
     *
     * @throws JiraException when the comment creation fails
     */
    public void addComment(String body, String visType, String visName)
        throws JiraException {

        ObjectNode req = JsonNodeFactory.instance.objectNode();
        req.put("body", body);

        if (visType != null && visName != null) {
            ObjectNode vis = JsonNodeFactory.instance.objectNode();
            vis.put("type", visType);
            vis.put("value", visName);

            req.set("visibility", vis);
        }

        try {
            restclient.post(getRestUri(key) + "/comment", req);
        } catch (Exception ex) {
            throw new JiraException("Failed add comment to issue " + key, ex);
        }
    }

    /**
     * Links this issue with another issue.
     *
     * @param issue Other issue key
     * @param type Link type name
     *
     * @throws JiraException when the link creation fails
     */
    public void link(String issue, String type) throws JiraException {
        link(issue, type, null, null, null);
    }

    /**
     * Links this issue with another issue and adds a comment.
     *
     * @param issue Other issue key
     * @param type Link type name
     * @param body Comment text
     *
     * @throws JiraException when the link creation fails
     */
    public void link(String issue, String type, String body) throws JiraException {
        link(issue, type, body, null, null);
    }

    /**
     * Links this issue with another issue and adds a comment with limited visibility.
     *
     * @param issue Other issue key
     * @param type Link type name
     * @param body Comment text
     * @param visType Target audience type (role or group)
     * @param visName Name of the role or group to limit visibility to
     *
     * @throws JiraException when the link creation fails
     */
    public void link(String issue, String type, String body, String visType, String visName)
        throws JiraException {

        ObjectNode req = JsonNodeFactory.instance.objectNode();

        ObjectNode t = JsonNodeFactory.instance.objectNode();
        t.put("name", type);
        req.set("type", t);

        ObjectNode inward = JsonNodeFactory.instance.objectNode();
        inward.put("key", key);
        req.set("inwardIssue", inward);

        ObjectNode outward = JsonNodeFactory.instance.objectNode();
        outward.put("key", issue);
        req.set("outwardIssue", outward);

        if (body != null) {
            ObjectNode comment = JsonNodeFactory.instance.objectNode();
            comment.put("body", body);

            if (visType != null && visName != null) {
                ObjectNode vis = JsonNodeFactory.instance.objectNode();
                vis.put("type", visType);
                vis.put("value", visName);

                comment.set("visibility", vis);
            }

            req.set("comment", comment);
        }

        try {
            restclient.post(getBaseUri() + "issueLink", req);
        } catch (Exception ex) {
            throw new JiraException("Failed to link issue " + key + " with issue " + issue, ex);
        }
    }

    /**
     * Creates a new JIRA issue.
     *
     * @param restclient REST client instance
     * @param project Key of the project to create the issue in
     * @param issueType Name of the issue type to create
     *
     * @return a fluent create instance
     *
     * @throws JiraException when the client fails to retrieve issue metadata
     */
    public static FluentCreate create(RestClient restclient, String project, String issueType)
        throws JiraException {

        FluentCreate fc = new FluentCreate(
            restclient,
            getCreateMetadata(restclient, project, issueType));

        return fc
            .field(Field.PROJECT, project)
            .field(Field.ISSUE_TYPE, issueType);
    }

    /**
     * Creates a new sub-task.
     *
     * @return a fluent create instance
     *
     * @throws JiraException when the client fails to retrieve issue metadata
     */
    public FluentCreate createSubtask() throws JiraException {
        return Issue.create(restclient, getProject().getKey(), "Sub-task")
                .field(Field.PARENT, getKey());
    }

    private static JsonNode realGet(RestClient restclient, String key, Map<String, String> queryParams)
            throws JiraException {

        JsonNode result;

        try {
            URI uri = restclient.buildURI(getBaseUri() + "issue/" + key, queryParams);
            result = restclient.get(uri);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve issue " + key, ex);
        }

        if (result == null || !result.isObject()) {
            throw new JiraException("JSON payload is malformed");
        }

        return result;
    }

    /**
     * Retrieves the given issue record.
     *
     * @param restclient REST client instance
     * @param key Issue key (PROJECT-123)
     *
     * @return an issue instance (issue includes all navigable fields)
     *
     * @throws JiraException when the retrieval fails
     */
    public static Issue get(RestClient restclient, String key)
            throws JiraException {

        return new Issue(restclient, realGet(restclient, key, new HashMap<String, String>()));
    }

    /**
     * Retrieves the given issue record.
     *
     * @param restclient REST client instance
     *
     * @param key Issue key (PROJECT-123)
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     *
     * @return an issue instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Issue get(RestClient restclient, String key, final String includedFields)
            throws JiraException {

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("fields", includedFields);
        return new Issue(restclient, realGet(restclient, key, queryParams));
    }

    /**
     * Retrieves the given issue record.
     *
     * @param restclient REST client instance
     *
     * @param key Issue key (PROJECT-123)
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     * 
     * @param expand fields to expand when obtaining the issue
     *
     * @return an issue instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Issue get(RestClient restclient, String key, final String includedFields,
            final String expand) throws JiraException {

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("fields", includedFields);
	    if (expand != null) {
	        queryParams.put("expand", expand);
	    }
        return new Issue(restclient, realGet(restclient, key, queryParams));
    }

    /**
     * Search for issues with the given query.
     *
     * @param restclient REST client instance
     *
     * @param jql JQL statement
     *
     * @return a search result structure with results (issues include all
     * navigable fields)
     *
     * @throws JiraException when the search fails
     */
    public static SearchResult search(RestClient restclient, String jql)
            throws JiraException {
        return search(restclient, jql, null, null);
    }

    /**
     * Search for issues with the given query and specify which fields to
     * retrieve.
     *
     * @param restclient REST client instance
     *
     * @param jql JQL statement
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     *
     * @return a search result structure with results
     *
     * @throws JiraException when the search fails
     */
    public static SearchResult search(RestClient restclient, String jql, String includedFields, Integer maxResults)
            throws JiraException {
        return search(restclient, jql, includedFields, null, maxResults, null);
    }

    /**
     * Search for issues with the given query and specify which fields to
     * retrieve. If the total results is bigger than the maximum returned
     * results, then further calls can be made using different values for
     * the <code>startAt</code> field to obtain all the results.
     *
     * @param restclient REST client instance
     *
     * @param jql JQL statement
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     * 
     * @param maxResults if non-<code>null</code>, defines the maximum number of
     * results that can be returned 
     * 
     * @param startAt if non-<code>null</code>, defines the first issue to
     * return
     *
     * @param expandFields fields to expand when obtaining the issue
     *
     * @return a search result structure with results
     *
     * @throws JiraException when the search fails
     */
    public static SearchResult search(RestClient restclient, String jql,
            String includedFields, String expandFields, Integer maxResults, Integer startAt)
                    throws JiraException {

        return new SearchResult(restclient, jql, includedFields, expandFields, maxResults, startAt);
    }

	private static JsonNode executeSearch(RestClient restclient, String jql,
			String includedFields, String expandFields, Integer maxResults,
			Integer startAt) throws JiraException {
        JsonNode result;

        try {
            URI searchUri = createSearchURI(restclient, jql, includedFields,
					expandFields, maxResults, startAt);
            result = restclient.get(searchUri);
        } catch (Exception ex) {
            throw new JiraException("Failed to search issues", ex);
        }

        if (result == null || !result.isObject()) {
            throw new JiraException("JSON payload is malformed");
        }

		return result;
	}

	/**
	 * Creates the URI to execute a jql search.
	 * 
	 * @param restclient
	 * @param jql
	 * @param includedFields
	 * @param expandFields
	 * @param maxResults
	 * @param startAt
	 * @return the URI to execute a jql search.
	 * @throws URISyntaxException
	 */
	private static URI createSearchURI(RestClient restclient, String jql,
			String includedFields, String expandFields, Integer maxResults,
			Integer startAt) throws URISyntaxException {
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("jql", jql);
		if(maxResults != null){
		    queryParams.put("maxResults", String.valueOf(maxResults));
		}
		if (includedFields != null) {
		    queryParams.put("fields", includedFields);
		}
		if (expandFields != null) {
		    queryParams.put("expand", expandFields);
		}
		if (startAt != null) {
		    queryParams.put("startAt", String.valueOf(startAt));
		}

        return restclient.buildURI(getBaseUri() + "search", queryParams);
	}

    /**
     * Reloads issue data from the JIRA server (issue includes all navigable
     * fields).
     *
     * @throws JiraException when the retrieval fails
     */
    public void refresh() throws JiraException {
        JsonNode result = realGet(restclient, key, new HashMap<String, String>());
        deserialize(result);
    }

    /**
     * Reloads issue data from the JIRA server and specify which fields to
     * retrieve.
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     *
     * @throws JiraException when the retrieval fails
     */
    public void refresh(final String includedFields) throws JiraException {

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("fields", includedFields);
        JsonNode result = realGet(restclient, key, queryParams);
        deserialize(result);
    }

    /**
     * Gets an arbitrary field by its name.
     *
     * @param name Name of the field to retrieve
     *
     * @return the field value or null if not found
     */
    public Object getField(String name) {

        return fields != null ? fields.get(name) : null;
    }

    /**
     * Begins a transition field chain.
     *
     * @return a fluent transition instance
     *
     * @throws JiraException when the client fails to retrieve issue metadata
     */
    public FluentTransition transition() throws JiraException {
        return new FluentTransition(getTransitions());
    }

    /**
     * Begins an update field chain.
     *
     * @return a fluent update instance
     *
     * @throws JiraException when the client fails to retrieve issue metadata
     */
    public FluentUpdate update() throws JiraException {
        return new FluentUpdate(getEditMetadata());
    }

    /**
     * Casts a vote in favour of an issue.
     *
     * @throws JiraException when the voting fails
     */
    public void vote() throws JiraException {

        try {
            restclient.post(getRestUri(key) + "/votes");
        } catch (Exception ex) {
            throw new JiraException("Failed to vote on issue " + key, ex);
        }
    }

    /**
     * Removes the current user's vote from the issue.
     *
     * @throws JiraException when the voting fails
     */
    public void unvote() throws JiraException {

        try {
            restclient.delete(getRestUri(key) + "/votes");
        } catch (Exception ex) {
            throw new JiraException("Failed to unvote on issue " + key, ex);
        }
    }

    /**
     * Adds a watcher to the issue.
     *
     * @param username Username of the watcher to add
     *
     * @throws JiraException when the operation fails
     */
    public void addWatcher(String username) throws JiraException {

        try {
            URI uri = restclient.buildURI(getRestUri(key) + "/watchers");
            restclient.post(uri, username);
        } catch (Exception ex) {
            throw new JiraException(
                "Failed to add watcher (" + username + ") to issue " + key, ex
            );
        }
    }

    /**
     * Removes a watcher to the issue.
     *
     * @param username Username of the watcher to remove
     *
     * @throws JiraException when the operation fails
     */
    public void deleteWatcher(String username) throws JiraException {

        try {
            Map<String, String> connectionParams = new HashMap<>();
            connectionParams.put("username", username);
            URI uri = restclient.buildURI(
                getRestUri(key) + "/watchers", connectionParams);
            restclient.delete(uri);
        } catch (Exception ex) {
            throw new JiraException(
                "Failed to remove watch (" + username + ") from issue " + key, ex
            );
        }
    }

    @Override
    public String toString() {
        return getKey();
    }

    public ChangeLog getChangeLog() {
        return changeLog;
    }

    public String getKey() {
        return key;
    }

    public User getAssignee() {
        return assignee;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<Component> getComponents() {
        return components;
    }

    public String getDescription() {
        return description;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public List<Version> getFixVersions() {
        return fixVersions;
    }

    public List<IssueLink> getIssueLinks() {
        return issueLinks;
    }

    public IssueType getIssueType() {
        return issueType;
    }

    public List<String> getLabels() {
        return labels;
    }

    public Issue getParent() {
        return parent;
    }

    public Priority getPriority() {
        return priority;
    }

    public Project getProject() {
        return project;
    }

    public User getReporter() {
        return reporter;
    }
    
    public List<RemoteLink> getRemoteLinks() throws JiraException {
        JsonNode json;
        try {
            URI uri = restclient.buildURI(getRestUri(key) + "/remotelink");
            json = restclient.get(uri);

            if (!json.isArray()) {
                throw new JiraException("Remote links payload is malformed");
            }
        } catch (Exception ex) {
            throw new JiraException("Failed to get remote links for issue "
                    + key, ex);
        }

        return Field.getRemoteLinks(json, restclient);
    }

    public Resolution getResolution() {
        return resolution;
    }

    public Date getResolutionDate() {
        return resolutionDate;
    }

    public Status getStatus() {
        return status;
    }

    public List<Issue> getSubtasks() {
        return subtasks;
    }

    public String getSummary() {
        return summary;
    }

    public TimeTracking getTimeTracking() {
        return timeTracking;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public Votes getVotes() {
        return votes;
    }

    public Watches getWatches() {
        return watches;
    }

    public List<WorkLog> getWorkLogs() {
        return workLogs;
    }

    public List<WorkLog> getAllWorkLogs() throws JiraException {
        JsonNode json;
        try {
            URI uri = restclient.buildURI(getRestUri(key) + "/worklog");
            json = restclient.get(uri);

            if (!json.isObject()) {
                throw new JiraException("Worklog payload is malformed");
            }
        } catch (Exception ex) {
            throw new JiraException("Failed to get worklog for issue "
                    + key, ex);
        }

        return Field.getWorkLogs(json, restclient);
    }

    public Integer getTimeSpent() {
        return timeSpent;
    }

    public Integer getTimeEstimate() {
        return timeEstimate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    // Helper to convert Object to JsonNode if needed
    private JsonNode convertToJsonNode(Object obj) {
        if (obj == null) {
            return FACTORY.nullNode();
        }
        if (obj instanceof JsonNode) {
            return (JsonNode) obj;
        }
        // For other types, use ObjectMapper to convert to JsonNode
        return mapper.valueToTree(obj);
    }

}

