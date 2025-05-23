/* Update MMA 2025-05-20: use of Jackson for JSON handling */

package net.rcarz.jiraclient;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class IssueHistory extends Resource implements Serializable {

    private static final long serialVersionUID = 1L;
    private User user;
    private ArrayList<IssueHistoryItem> changes;
    private Date created;

    /**
     * Creates an issue history record from a JSON payload.
     *
     * @param restClient REST client instance
     * @param json JSON payload
     */
    protected IssueHistory(RestClient restClient, JsonNode json) {
        super(restClient);

        if (json != null) {
            deserialize(restClient,json);
        }
    }

    public IssueHistory(IssueHistory record, ArrayList<IssueHistoryItem> changes) {
        super(record.restclient);
        user = record.user;
        id = record.id;
        self = record.self;
        created = record.created;
        this.changes = changes;
    }

    private void deserialize(RestClient restclient, JsonNode json) {
        self = Field.getString(json.get("self"));
        id = Field.getString(json.get("id"));
        user = new User(restclient,json.get("author"));
        created = Field.getDateTime(json.get("created"));

        JsonNode itemsNode = json.get("items");
        changes = new ArrayList<>();
        if (itemsNode != null && itemsNode.isArray()) {
            for (JsonNode itemNode : itemsNode) {
                changes.add(new IssueHistoryItem(restclient, itemNode));
            }
        }
    }

    public User getUser() {
        return user;
    }

    public ArrayList<IssueHistoryItem> getChanges() {
        return changes;
    }

    public Date getCreated() {
        return created;
    }

}
