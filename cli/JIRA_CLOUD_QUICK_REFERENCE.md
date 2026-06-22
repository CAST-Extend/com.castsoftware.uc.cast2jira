# Jira Cloud Quick Reference

## Authentication Parameters

| Parameter | Jira On-Prem | Jira Cloud (SAAS) |
|-----------|--------------|-------------------|
| `-jirarestapiurl` | `http://jira.company.com` | `https://company.atlassian.net` |
| `-jirausername` | `john.doe` | `john.doe@company.com` *(email)* |
| `-jirauserpassword` | `YourPassword123` | `ATATTxxxxxxxxxx` *(API token)* |

## How to Get an API Token

1. Visit: https://id.atlassian.com/manage-profile/security/api-tokens
2. Click "Create API token"
3. Give it a name (e.g., "CAST Jira Connector")
4. Copy the token immediately (you won't see it again!)
5. Use this token as the `-jirauserpassword` value

## Example Command for Jira Cloud

```bash
java -jar CastJiraConnector.jar \
  -applicationname MyApp \
  -castusername operator \
  -castuserpassword CastAIP \
  -databaseprovider CSS \
  -databasehost localhost \
  -databasename postgres \
  -databaseport 2278 \
  -databaseschema demo_central \
  -jirarestapiurl https://mycompany.atlassian.net \
  -jiraprojectname MYPROJ \
  -jirausername john.doe@company.com \
  -jirauserpassword ATATTxxxxxxxxxxxxxxxxxx \
  -logpath c:\temp
```

## Common Issues

### 401 Unauthorized Error
- ✗ Using password instead of API token
- ✓ Use API token from https://id.atlassian.com/manage-profile/security/api-tokens

### Authentication Failed
- ✗ Username is not an email address  
- ✓ Use full email: `user@company.com`

### URL Format Error
- ✗ `http://mycompany.atlassian.net` (wrong protocol)
- ✗ `https://jira.mycompany.com` (on-prem URL for cloud)
- ✓ `https://mycompany.atlassian.net` (correct format)

## Migration Checklist

- [ ] Generate Jira Cloud API token
- [ ] Update `-jirarestapiurl` to `https://yourcompany.atlassian.net`
- [ ] Update `-jirausername` to email address
- [ ] Update `-jirauserpassword` to API token
- [ ] Test connection with a single issue first
- [ ] Verify user has permissions in Jira Cloud project

## Need Help?

See the full migration guide: [JIRA_CLOUD_MIGRATION.md](JIRA_CLOUD_MIGRATION.md)
