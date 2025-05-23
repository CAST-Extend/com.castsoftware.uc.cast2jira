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

import java.lang.Iterable;
import java.lang.UnsupportedOperationException;
import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Utility functions for translating between JSON and fields.
 */
public final class Field {

    /**
     * Field metadata structure.
     */
    public static final class Meta {
        public boolean required;
        public String type;
        public String items;
        public String name;
        public String system;
        public String custom;
        public int customId;
    }

    /**
     * Field update operation.
     */
    public static final class Operation {
        public String name;
        public Object value;

        /**
         * Initialises a new update operation.
         *
         * @param name Operation name
         * @param value Field value
         */
        public Operation(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

    /**
     * Allowed value types.
     */
    public enum ValueType {
        KEY("key"), NAME("name"), ID_NUMBER("id"), VALUE("value");
        private String typeName;

        private ValueType(String typeName) {
            this.typeName = typeName;
        }

        @Override
        public String toString() {
            return typeName;
        }
    };

    /**
     * Value and value type pair.
     */
    public static final class ValueTuple {
        public final String type;
        public final JsonNode value;
        private static final ObjectMapper mapper = new ObjectMapper();

        /**
         * Initialises the value tuple.
         *
         * @param type
         * @param value
         */
        public ValueTuple(String type, JsonNode value) {
            this.type = type;
            this.value = (value != null ? value : NullNode.instance);
        }

        /**
         * Initialises the value tuple.
         *
         * @param type
         * @param value
         */
        public ValueTuple(ValueType type, Object value) {
            this(type.toString(), (value != null ? mapper.valueToTree(value) : NullNode.instance));
        }
    }

    public static final String ASSIGNEE = "assignee";
    public static final String ATTACHMENT = "attachment";
    public static final String CHANGE_LOG = "changelog";
    public static final String CHANGE_LOG_ENTRIES = "histories";
    public static final String CHANGE_LOG_ITEMS = "items";
    public static final String COMMENT = "comment";
    public static final String COMPONENTS = "components";
    public static final String DESCRIPTION = "description";
    public static final String DUE_DATE = "duedate";
    public static final String FIX_VERSIONS = "fixVersions";
    public static final String ISSUE_LINKS = "issuelinks";
    public static final String ISSUE_TYPE = "issuetype";
    public static final String LABELS = "labels";
    public static final String PARENT = "parent";
    public static final String PRIORITY = "priority";
    public static final String PROJECT = "project";
    public static final String REPORTER = "reporter";
    public static final String RESOLUTION = "resolution";
    public static final String RESOLUTION_DATE = "resolutiondate";
    public static final String STATUS = "status";
    public static final String SUBTASKS = "subtasks";
    public static final String SUMMARY = "summary";
    public static final String TIME_TRACKING = "timetracking";
    public static final String VERSIONS = "versions";
    public static final String VOTES = "votes";
    public static final String WATCHES = "watches";
    public static final String WORKLOG = "worklog";
    public static final String TIME_ESTIMATE = "timeestimate";
    public static final String TIME_SPENT = "timespent";
    public static final String CREATED_DATE = "created";
    public static final String UPDATED_DATE = "updated";
    public static final String TRANSITION_TO_STATUS = "to";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static final ObjectMapper mapper = new ObjectMapper();

    private Field() { }

    /**
     * Gets a boolean value from the given object.
     *
     * @param b a Boolean representation
     *
     * @return a boolean primitive or false if b isn't a Boolean instance
     */
    public static boolean getBoolean(Object b) {
        if (b instanceof Boolean) {
            return (Boolean) b;
        } else if (b instanceof JsonNode) {
            JsonNode node = (JsonNode) b;
            if (node.isBoolean()) {
                return node.booleanValue();
            } else if (node.isTextual()) {
                return Boolean.parseBoolean(node.asText());
            }
        } else if (b instanceof String) {
            return Boolean.parseBoolean((String) b);
        }

        return false;
    }

    /**
     * Gets a list of comments from the given object.
     *
     * @param c a JsonNode instance
     * @param restClient REST client instance
     *
     * @return a list of comments found in c
     */
    public static List<Comment> getComments(Object c, RestClient restClient) {
        List<Comment> results = new ArrayList<>();

        if (c instanceof JsonNode) {
            JsonNode node = (JsonNode) c;
            if (!node.isNull() && node.has("comments")) {
                JsonNode commentsNode = node.get("comments");
                results = getResourceArray(Comment.class, commentsNode, restClient);
            }
        }

        return results;
    }

    /**
     * Gets a list of work logs from the given object.
     *
     * @param c a JsonNode instance
     * @param restClient REST client instance
     *
     * @return a list of work logs found in c
     */
    public static List<WorkLog> getWorkLogs(Object c, RestClient restClient) {
        List<WorkLog> results = new ArrayList<WorkLog>();

        if (c instanceof JsonNode) {
            JsonNode node = (JsonNode) c;
            if (!node.isNull() && node.has("worklogs")) {
                JsonNode worklogsNode = node.get("worklogs");
                results = getResourceArray(WorkLog.class, worklogsNode, restClient);
            }
        }
        return results;
    }
    
    /**
     * Gets a list of remote links from the given object.
     *
     * @param c a JsonNode instance
     * @param restClient REST client instance
     *
     * @return a list of remote links found in c
     */
    public static List<RemoteLink> getRemoteLinks(Object c, RestClient restClient) {
        List<RemoteLink> results = new ArrayList<>();

        if (c instanceof JsonNode) {
            JsonNode node = (JsonNode) c;
            if (node.isArray()) {
                results = getResourceArray(RemoteLink.class, node, restClient);
            }
        }
        return results;
    }

    /**
     * Gets a date from the given object.
     *
     * @param d a representation of a date
     *
     * @return a Date instance or null if d isn't a string or JsonNode
     */
    public static Date getDate(Object d) {
        if (d == null) return null;

        String dateStr = null;

        if (d instanceof String) {
            dateStr = (String) d;
        } else if (d instanceof JsonNode) {
            JsonNode node = (JsonNode) d;
            if (node.isTextual()) {
                dateStr = node.asText();
            }
        }

        if (dateStr != null) {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            return df.parse(dateStr, new ParsePosition(0));
        }

        return null;
    }

    /**
     * Gets a date with a time from the given object.
     *
     * @param d a representation of a date
     *
     * @return a Date instance or null if d isn't a string or JsonNode
     */
    public static Date getDateTime(Object d) {
        if (d == null) return null;

        String dateStr = null;

        if (d instanceof String) {
            dateStr = (String) d;
        } else if (d instanceof JsonNode) {
            JsonNode node = (JsonNode) d;
            if (node.isTextual()) {
                dateStr = node.asText();
            }
        }

        if (dateStr != null) {
            SimpleDateFormat df = new SimpleDateFormat(DATETIME_FORMAT);
            return df.parse(dateStr, new ParsePosition(0));
        }

        return null;
    }

    /**
     * Gets a floating-point number from the given object.
     *
     * @param i a Double representation
     *
     * @return a floating-point number or null if i isn't a Double representation
     */
    public static Double getDouble(Object i) {
        if (i instanceof Double) {
            return (Double) i;
        } else if (i instanceof Number) {
            return ((Number) i).doubleValue();
        } else if (i instanceof String) {
            try {
                return Double.parseDouble((String) i);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (i instanceof JsonNode) {
            JsonNode node = (JsonNode) i;
            if (node.isNumber()) {
                return node.doubleValue();
            } else if (node.isTextual()) {
                try {
                    return Double.parseDouble(node.asText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Gets an integer from the given object.
     *
     * @param i an Integer representation
     *
     * @return an integer primitive or 0 if i isn't an Integer representation
     */
    public static int getInteger(Object i) {
        if (i instanceof Integer) {
            return (Integer) i;
        } else if (i instanceof Number) {
            return ((Number) i).intValue();
        } else if (i instanceof String) {
            try {
                return Integer.parseInt((String) i);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else if (i instanceof JsonNode) {
            JsonNode node = (JsonNode) i;
            if (node.isInt() || node.isNumber()) {
                return node.intValue();
            } else if (node.isTextual()) {
                try {
                    return Integer.parseInt(node.asText());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }

        return 0;
    }

    /**
     * Gets a generic map from the given object.
     *
     * @param keyType Map key data type
     * @param valType Map value data type
     * @param m a JSONObject instance
     *
     * @return a Map instance with all entries found in m
     */
    public static <TK, TV> Map<TK, TV> getMap(
        Class<TK> keyType, Class<TV> valType, Object m) {

        Map<TK, TV> result = new HashMap<>();

        if (m instanceof JsonNode) {
            JsonNode node = (JsonNode) m;
            if (node.isObject()) {
                ObjectMapper mapper = new ObjectMapper();
                Iterator<String> fieldNames = node.fieldNames();

                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode valueNode = node.get(fieldName);
                    try {
                        TK key = mapper.convertValue(fieldName, keyType);
                        TV value = mapper.convertValue(valueNode, valType);

                        if (keyType.isInstance(key) && valType.isInstance(value)) {
                            result.put(key,value);
                        }
                    } catch (IllegalArgumentException e) {
                        // Skip unconvertible entries
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets a JIRA resource from the given object.
     *
     * @param type Resource data type
     * @param r a JsonNode instance
     * @param restClient REST client instance
     *
     * @return a Resource instance or null if r isn't a JsonNode instance
     */
    public static <T extends Resource> T getResource(Class<T> type, Object r, RestClient restClient) {

        if (!(r instanceof JsonNode)) {
            return null;
        }
        JsonNode node = (JsonNode) r;

        if (type == Attachment.class)
            return type.cast(new Attachment(restClient, node));
        else if (type == ChangeLog.class)
            return type.cast(new ChangeLog(restClient, node));
        else if (type == ChangeLogEntry.class)
            return type.cast(new ChangeLogEntry(restClient, node));
        else if (type == ChangeLogItem.class)
            return type.cast(new ChangeLogItem(restClient, node));
        else if (type == Comment.class)
            return type.cast(new Comment(restClient, node));
        else if (type == Component.class)
            return type.cast(new Component(restClient, node));
        else if (type == CustomFieldOption.class)
            return type.cast(new CustomFieldOption(restClient, node));
        else if (type == Issue.class)
            return type.cast(new Issue(restClient, node));
        else if (type == IssueLink.class)
            return type.cast(new IssueLink(restClient, node));
        else if (type == IssueType.class)
            return type.cast(new IssueType(restClient, node));
        else if (type == LinkType.class)
            return type.cast(new LinkType(restClient, node));
        else if (type == Priority.class)
            return type.cast(new Priority(restClient, node));
        else if (type == Project.class)
            return type.cast(new Project(restClient, node));
        else if (type == RemoteLink.class)
            return type.cast(new RemoteLink(restClient, node));
        else if (type == Resolution.class)
            return type.cast(new Resolution(restClient, node));
        else if (type == Status.class)
            return type.cast(new Status(restClient, node));
        else if (type == Transition.class)
            return type.cast(new Transition(restClient, node));
        else if (type == User.class)
            return type.cast(new User(restClient, node));
        else if (type == Version.class)
            return type.cast(new Version(restClient, node));
        else if (type == Votes.class)
            return type.cast(new Votes(restClient, node));
        else if (type == Watches.class)
            return type.cast(new Watches(restClient, node));
        else if (type == WorkLog.class)
            return type.cast(new WorkLog(restClient, node));

        return null;
    }

    /**
     * Gets a string from the given object.
     *
     * @param s a String instance
     *
     * @return a String or null if s isn't a String instance or JsonNode
     */
    public static String getString(Object s) {

        if (s instanceof String) {
            return (String) s;
        } else if (s instanceof JsonNode) {
            JsonNode node = (JsonNode) s;
            if (node.isTextual()) {
                return node.asText();
            }
        }

        return null;
    }

    /**
     * Gets a list of strings from the given object.
     *
     * @param sa a JSONArray instance
     *
     * @return a list of strings found in sa
     */
    public static List<String> getStringArray(Object sa) {
        List<String> results = new ArrayList<>();

        if (sa instanceof JsonNode) {
            JsonNode arrayNode = (JsonNode) sa;
            if (arrayNode.isArray()) {
                for (JsonNode  node : arrayNode) {
                    if (node.isTextual())
                        results.add(node.asText());
                }
            }
        }

        return results;
    }

    /**
     * Gets a list of JIRA resources from the given object.
     *
     * @param type Resource data type
     * @param ra a JSONArray instance
     * @param restClient REST client instance
     *
     * @return a list of Resources found in ra
     */
    public static <T extends Resource> List<T> getResourceArray(
        Class<T> type, Object ra, RestClient restClient) {

        List<T> results = new ArrayList<>();

        if (ra instanceof JsonNode) {
            JsonNode arrayNode = (JsonNode) ra;
            if (arrayNode.isArray()) {
                for (JsonNode node : arrayNode) {
                    T item = getResource(type, node, restClient);
                    if (item != null)
                        results.add(item);
                }
            }
        }

        return results;
    }

    /**
     * Gets a time tracking object from the given object.
     *
     * @param tt a JSONObject instance
     *
     * @return a TimeTracking instance or null if tt isn't a JSONObject instance
     */
    public static TimeTracking getTimeTracking(Object tt) {
        TimeTracking result = null;

        if (tt instanceof JsonNode) {
            JsonNode node = (JsonNode) tt;
            if (!node.isNull()) {
                result = new TimeTracking(node);
            }
        }

        return result;
    }

    /**
     * Extracts field metadata from an editmeta JSON object.
     *
     * @param name Field name
     * @param editMeta Edit metadata JSON object
     *
     * @return a Meta instance with field metadata
     *
     * @throws JiraException when the field is missing or metadata is bad
     */
    public static Meta getFieldMetadata(String name, JsonNode editMeta) throws JiraException {
        if (editMeta == null || editMeta.isNull() || !editMeta.has(name)) {
            throw new JiraException("Field '" + name + "' does not exist or read-only");
        }

        JsonNode f = editMeta.get(name);
        Meta m = new Meta();

        m.required = Field.getBoolean(f.get("required"));
        m.name = Field.getString(f.get("name"));

        if (!f.has("schema"))
            throw new JiraException("Field '" + name + "' is missing schema metadata");

        JsonNode schema = f.get("schema");

        m.type = Field.getString(schema.get("type"));
        m.items = Field.getString(schema.get("items"));
        m.system = Field.getString(schema.get("system"));
        m.custom = Field.getString(schema.get("custom"));
        m.customId = Field.getInteger(schema.get("customId"));

        return m;
    }

    /**
     * Converts the given value to a date.
     *
     * @param value New field value
     *
     * @return a Date instance or null
     */
    public static Date toDate(Object value) {
        if (value instanceof Date || value == null)
            return (Date)value;

        String dateStr = value.toString();
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        if (dateStr.length() > DATE_FORMAT.length()) {
            df = new SimpleDateFormat(DATETIME_FORMAT);
        }
        return df.parse(dateStr, new ParsePosition(0));
    }

    /**
     * Converts an iterable type to a JSON array.
     *
     * @param iter Iterable type containing field values
     * @param type Name of the item type
     * @param custom Name of the custom type
     *
     * @return a JSON-encoded array of items
     */
    public static ArrayNode toArray(Iterable<?> iter, String type, String custom) throws JiraException {
        if (type == null)
            throw new JiraException("Array field metadata is missing item type");

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode results = mapper.createArrayNode();

        for (Object val : iter) {
            Operation oper = null;
            Object realValue;

            if (val instanceof Operation) {
                oper = (Operation) val;
                realValue = oper.value;
            } else
                realValue = val;

            JsonNode realResult = null;

            if (type.equals("component") || type.equals("group") ||
                type.equals("user") || type.equals("version")) {

                ObjectNode itemMap = mapper.createObjectNode();

                if (realValue instanceof ValueTuple) {
                    ValueTuple tuple = (ValueTuple) realValue;
                    itemMap.put(tuple.type, tuple.value.toString());
                } else
                    itemMap.put(ValueType.NAME.toString(), realValue.toString());

                realResult = itemMap;

            } else if (type.equals("string") && custom != null
                    && (custom.equals("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes") ||
                    custom.equals("com.atlassian.jira.plugin.system.customfieldtypes:multiselect"))) {

                ObjectNode valueObj = mapper.createObjectNode();
                valueObj.put(ValueType.VALUE.toString(), realValue.toString());
                realResult = valueObj;

            } else if (type.equals("string"))
                realResult = new TextNode(realValue.toString());

            if (oper != null) {
                ObjectNode operMap = mapper.createObjectNode();
                operMap.set(oper.name, realResult);
                results.add(operMap);
            } else
                results.add(realResult);
        }

        return results;
    }

    /**
     * Converts the given value to a JSON object.
     *
     * @param name Field name
     * @param value New field value
     * @param editMeta Edit metadata JSON object
     *
     * @return a JSON-encoded field value
     *
     * @throws JiraException when a value is bad or field has invalid metadata
     * @throws UnsupportedOperationException when a field type isn't supported
     */
    public static Object toJson(String name, Object value, JsonNode editMeta)
        throws JiraException, UnsupportedOperationException {

        Meta m = getFieldMetadata(name, editMeta);
        if (m.type == null)
            throw new JiraException("Field '" + name + "' is missing metadata type");

        ObjectMapper mapper = new ObjectMapper();

        switch (m.type) {
            case "array":
                if (value == null)
                    value = new ArrayList<>();
                else if (!(value instanceof Iterable))
                    throw new JiraException("Field '" + name + "' expects an Iterable value");

                return toArray((Iterable<?>) value, m.items, m.custom);

            case "date":
                if (value == null)
                    return NullNode.getInstance();

                Date d = toDate(value);
                if (d == null)
                    throw new JiraException("Field '" + name + "' expects a date value or format is invalid");

                SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
                return new TextNode(df.format(d));

            case "datetime":
                if (value == null)
                    return NullNode.getInstance();
                else if (!(value instanceof Timestamp))
                    throw new JiraException("Field '" + name + "' expects a Timestamp value");

                SimpleDateFormat df2 = new SimpleDateFormat(DATETIME_FORMAT);
                return new TextNode(df2.format(value));

            case "issuetype":
            case "priority":
            case "user":
            case "resolution":
                ObjectNode namedJson = mapper.createObjectNode();

                if (value == null)
                    return NullNode.getInstance();
                else if (value instanceof ValueTuple) {
                    ValueTuple tuple = (ValueTuple)value;
                    namedJson.set(tuple.type, mapper.valueToTree(tuple.value));
                } else
                    namedJson.put(ValueType.NAME.toString(), value.toString());

                return namedJson;

            case "project":
            case "issuelink":
                ObjectNode idJson = mapper.createObjectNode();

                if (value == null)
                    return NullNode.getInstance();
                else if (value instanceof ValueTuple) {
                    ValueTuple tuple = (ValueTuple) value;
                    idJson.set(tuple.type, mapper.valueToTree(tuple.value));
                } else
                    idJson.put(ValueType.ID_NUMBER.toString(), value.toString());

                return idJson;

            case "string":
            case "securitylevel":
                if (value == null)
                    return TextNode.valueOf("");
                else if (value instanceof List)
                    return toJsonMap((List<?>)value);
                else if (value instanceof ValueTuple) {
                    ValueTuple tuple = (ValueTuple) value;
                    ObjectNode json = mapper.createObjectNode();
                    json.set(tuple.type, mapper.valueToTree(tuple.value));
                    return json;
                }

                return TextNode.valueOf(value.toString());

            case "timetracking":
                if (value == null)
                    return NullNode.getInstance();
                else if (value instanceof TimeTracking)
                    return ((TimeTracking) value).toJsonNode();
                break;

            case "number":
                if(!(value instanceof Integer) && !(value instanceof Double) && !(value
                        instanceof Float) && !(value instanceof Long) ) {
                    throw new JiraException("Field '" + name + "' expects a Numeric value");
                }
                return mapper.valueToTree(value);
        }

        throw new UnsupportedOperationException(m.type + " is not a supported field type");
    }

    /**
     * Converts the given map to a JSON object.
     *
     * @param list List of values to be converted
     *
     * @return a JSON-encoded map
     */
    public static JsonNode toJsonMap(List<?> list) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();

        for (Object item : list) {
            if (item instanceof ValueTuple) {
                ValueTuple vt = (ValueTuple)item;
                json.put(vt.type, vt.value.toString());
            } else
                json.put(ValueType.VALUE.toString(), item.toString());
        }

        return json;
    }

    /**
     * Create a value tuple with value type of key.
     *
     * @param key The key value
     *
     * @return a value tuple
     */
    public static ValueTuple valueByKey(String key) {
        return new ValueTuple(ValueType.KEY, key);
    }

    /**
     * Create a value tuple with value type of name.
     *
     * @param name The name value
     *
     * @return a value tuple
     */
    public static ValueTuple valueByName(String name) {
        return new ValueTuple(ValueType.NAME, name);
    }

    /**
     * Create a value tuple with value type of ID number.
     *
     * @param id The ID number value
     *
     * @return a value tuple
     */
    public static ValueTuple valueById(String id) {
        return new ValueTuple(ValueType.ID_NUMBER, id);
    }
}

