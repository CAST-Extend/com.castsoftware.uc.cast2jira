# Jira Cloud (SAAS) Migration Guide

This document explains how to configure the CAST to Jira connector for Jira Cloud (SAAS) instead of Jira Server/Data Center (on-prem).

## Key Differences

### Authentication

**Jira Server/Data Center (On-Prem):**
- Uses username and password
- Username is your Jira username

**Jira Cloud (SAAS):**
- Uses email address and API Token
- Email is your Atlassian account email
- API Token must be generated (passwords DO NOT work)

### API URL Format

**Jira Server/Data Center (On-Prem):**
```
http://jira.yourcompany.com
```
or
```
https://jira.yourcompany.com
```

**Jira Cloud (SAAS):**
```
https://your-domain.atlassian.net
```

## How to Generate a Jira Cloud API Token

1. Log in to your Atlassian account at https://id.atlassian.com
2. Go to Security → API tokens: https://id.atlassian.com/manage-profile/security/api-tokens
3. Click "Create API token"
4. Give it a descriptive name (e.g., "CAST Jira Connector")
5. Click "Create"
6. **IMPORTANT**: Copy the token immediately - you won't be able to see it again!
7. Store it securely (like a password)

## Command Line Changes

### Before (Jira On-Prem):
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
  -jirarestapiurl http://jira.mycompany.com \
  -jiraprojectname MYPROJ \
  -jirausername john.doe \
  -jirauserpassword MyPassword123 \
  -logpath c:\temp
```

### After (Jira Cloud):
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
  -jirausername john.doe@mycompany.com \
  -jirauserpassword ATATTxxxxxxxxxxxxxxxxxxxxxxxx \
  -logpath c:\temp
```

**Key Changes:**
1. `-jirarestapiurl`: Changed from `http://jira.mycompany.com` to `https://mycompany.atlassian.net`
2. `-jirausername`: Changed from `john.doe` to `john.doe@mycompany.com` (full email address)
3. `-jirauserpassword`: Changed from actual password to API token (format: `ATATTxxxxxxxxx...`)

## Troubleshooting

### Authentication Failed (401 Unauthorized)

**Possible Causes:**
1. Using password instead of API token
2. API token is incorrect or expired
3. Email address is incorrect
4. User doesn't have permission to access the Jira project

**Solution:**
- Generate a new API token
- Verify you're using your full email address
- Verify the email has access to the target Jira project

### Connection Failed

**Possible Causes:**
1. Incorrect Jira Cloud URL format
2. Network/firewall blocking access to atlassian.net

**Solution:**
- Verify URL is `https://your-domain.atlassian.net` (no trailing slash recommended)
- Check network connectivity to Atlassian Cloud
- Verify no proxy/firewall blocking HTTPS to atlassian.net

### Project Not Found

**Possible Causes:**
1. Project key is case-sensitive and doesn't match exactly
2. User doesn't have permission to access the project

**Solution:**
- Verify the exact project key from Jira (including case)
- Verify the user has at least "Browse Projects" permission

## Security Best Practices

1. **Never share your API token** - treat it like a password
2. **Rotate API tokens regularly** - generate new tokens periodically
3. **Use dedicated service accounts** - create a specific Jira user for automation
4. **Revoke unused tokens** - delete API tokens that are no longer needed
5. **Limit permissions** - only grant necessary permissions to the automation user

## API Token Management

- View/Revoke tokens: https://id.atlassian.com/manage-profile/security/api-tokens
- Tokens never expire (unless manually revoked)
- You can have multiple active tokens
- Each token should have a descriptive label

## Additional Resources

- [Atlassian API Tokens Documentation](https://support.atlassian.com/atlassian-account/docs/manage-api-tokens-for-your-atlassian-account/)
- [Jira Cloud REST API Documentation](https://developer.atlassian.com/cloud/jira/platform/rest/v3/intro/)
- [Basic Auth for REST APIs](https://developer.atlassian.com/cloud/jira/platform/basic-auth-for-rest-apis/)
