package com.castsoftware.jira.pojo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.castsoftware.jira.util.Constants;

/**
 * The Class ActionPlanViolation is used to store every violation included in
 * the action plan. It will provide and easy way to interact with every
 * violation
 * 
 * @author FME
 * @version 1.1
 */
public class ActionPlanViolation {
  /** The priority. */
  private int    priority;

  /** The action date. */
  private String actionDate;

  /** The action date. */
  private String firstSnapshotDate;

  /** The actionDef. */
  private String actionDef;

  /** The objectFullName. */
  private long   objectId;

  /** The objectFullName. */
  private String objectFullName;

  /** The metricShortDescription. */
  private String metricShortDescription;

  private int    metricId;

  /** The reason. */
  private String reason;

  /** The metricLongDescription. */
  private String metricLongDescription;

  /** The remediation. */
  private String remediation;

  /** The reference. */
  private String reference;

  /** The violation_example. */
  private String violationExample;

  /** The remediation_example. */
  private String remediationExample;

  /** The output. */
  private String output;

  /** The totales. */
  private String totales;

  String         sourcePath;
  int            lineStart;
  int            LineEnd;
  String         sourceCode;
  String         techCriteria;
  String         businessCriteria;
  int            violationStatus;

  /**
   * Instantiates a new action plan violation.
   * 
   * @param priority
   *          the priority
   * @param actionDate
   *          the action date
   * @param actionDef
   *          the action def
   * @param objectFullName
   *          the object full name
   * @param metricShortDescription
   *          the metric short description
   * @param reason
   *          the reason
   * @param metricLongDescription
   *          the metric long description
   * @param remediation
   *          the remediation
   * @param reference
   *          the reference
   * @param violationExample
   *          the violation example
   * @param remediationExample
   *          the remediation example
   * @param output
   *          the output
   * @param totals
   *          the totals
   */
  public ActionPlanViolation(long object_id, String tag, int priority, String actionDate, String firstSnapshotDate, String actionDef,
      String objectFullName, int metricId, String metricShortDescription, String reason, String metricLongDescription,
      String remediation, String reference, String violationExample, String remediationExample, String output,
      String totales, String sourcePath, int lineStart, int lineEnd, String sourceCode, String techCriteria,
      String businessCriteria, int violationStatus)
  {

    setObjectId(object_id);

    if (tag != null)
    {
      switch (tag) {
      case "low":
        priority = 4;
        break;
      case "moderate":
        priority = 3;
        break;
      case "high":
        priority = 2;
        break;
      case "extreme":
        priority = 1;
        break;

      }
    }
    setPriority(priority);
    setFirstSnapshotDate(firstSnapshotDate);
    setActionDate(actionDate);
    setActionDef(actionDef);
    setObjectFullName(objectFullName);
    setMetricId(metricId);
    setMetricShortDescription(metricShortDescription);
    setReason(reason);
    setMetricLongDescription(metricLongDescription);
    setRemediation(remediation);
    setReference(reference);
    setViolationExample(violationExample);
    setRemediationExample(remediationExample);
    setOutput(output);
    setTotales(totales);
    setSourcePath(sourcePath);
    setLineStart(lineStart);
    setLineEnd(lineEnd);
    setTechCriteria(techCriteria);
    setBusinessCriteria(businessCriteria);
    setViolationStatus(violationStatus);
    setSourceCode(sourceCode);

    String source = extractSourceCode();
    setSourceCode(source);
  }

  private String extractSourceCode()
  {
    int srcStartLine = this.getLineStart();
    int srcEndLine = this.getLineEnd();
    String crlf = System.getProperty("line.separator");
    String src = getSourceCode();

    StringBuffer rslt = new StringBuffer().append("\n");
    String[] lines = src.split(crlf);
    int cLine = 0;
    for (String ln : lines)
    {
      cLine++;
      if (cLine < srcStartLine)
        continue;
      else if (cLine > srcEndLine)
        break;

      ln = ln.replace("\b", "");
      ln = ln.replace("\f", "");
      ln = ln.replace("\t", "");
      ln = ln.replace("\"", "");
      ln = ln.replace("\\", "");
      rslt.append(ln).append(crlf);
    }

    return rslt.toString();
  }

  /**
   * Gets the action defined by user.
   * 
   * @return the action defined by user
   */
  public String getActionDef()
  {
    if (actionDef == null)
    {
      actionDef = Constants.FIELD_VALUE_WHEN_IS_NULL;
    }
    return actionDef;
  }

  /**
   * Sets the action defined by user.
   * 
   * @param actionDef
   *          the new defined by user
   */
  public void setActionDef(String actionDef)
  {
    if (actionDef != null)
      this.actionDef = actionDef.replaceAll("[\"]", "");
  }

  /**
   * Gets the object full name.
   * 
   * @return the object full name
   */
  public String getObjectFullName()
  {
    if (objectFullName == null)
    {
      objectFullName = Constants.FIELD_VALUE_WHEN_IS_NULL;
    }
    return objectFullName;
  }

  /**
   * Sets the object full name.
   * 
   * @param objectFullName
   *          the new object full name
   */
  public void setObjectFullName(String objectFullName)
  {
    if (objectFullName != null)
      this.objectFullName = objectFullName.replaceAll("[\"]", "");
  }

  public long getObjectId()
  {
    return objectId;
  }

  public void setObjectId(long objectId)
  {
    this.objectId = objectId;
  }

  /**
   * Gets the metric short description.
   * 
   * @return the metric short description
   */
  public String getMetricShortDescription()
  {
    if (metricShortDescription == null)
    {
      metricShortDescription = Constants.FIELD_VALUE_WHEN_IS_NULL;
    }
    return metricShortDescription;
  }

  /**
   * Sets the metric short description.
   * 
   * @param metricShortDescription
   *          the new metric short description
   */
  public void setMetricShortDescription(String metricShortDescription)
  {
    if (metricShortDescription != null)
      this.metricShortDescription = metricShortDescription.replaceAll("[\"]", "");
  }

  /**
   * Gets the fields concatenated.
   * 
   * @return the fields concatenated
   * @throws ParseException
   */
  public String getFieldsConcatenated() throws ParseException
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    Date snapshotDate = sdf.parse(this.actionDate);
    Date cutDate = sdf.parse("2017-05-02 00:00:00.000");

    if (snapshotDate.before(cutDate))
    {
      return Integer.toString(this.priority) + this.actionDate + this.actionDef + this.objectFullName
          + this.metricShortDescription;
    } else
    {
      return String.format("%s%d%d", this.firstSnapshotDate, this.objectId, this.metricId);
    }
  }

  /**
   * Gets the priority.
   * 
   * @return the priority
   */
  public int getPriority()
  {
    return priority;
  }

  /**
   * Sets the priority.
   * 
   * @param priority
   *          the new priority
   */
  public void setPriority(int priority)
  {
    this.priority = priority;
  }

  /**
   * Gets the reason.
   * 
   * @return the reason
   */
  public String getReason()
  {
    if (reason == null)
    {
      reason = Constants.FIELD_VALUE_WHEN_IS_NULL;
    }
    return reason;
  }

  /**
   * Sets the reason.
   * 
   * @param reason
   *          the new reason
   */
  public void setReason(String reason)
  {
    if (reason != null)
      this.reason = reason.replaceAll("[\"]", "");
  }

  /**
   * Gets the metric long description.
   * 
   * @return the metric long description
   */
  public String getMetricLongDescription()
  {
    if (metricLongDescription == null)
    {
      metricLongDescription = Constants.FIELD_VALUE_WHEN_IS_NULL;
    }
    return metricLongDescription;
  }

  /**
   * Sets the metric long description.
   * 
   * @param metricLongDescription
   *          the new metric long description
   */
  public void setMetricLongDescription(String metricLongDescription)
  {
    if (metricLongDescription != null)
      this.metricLongDescription = metricLongDescription.replaceAll("[\"]", "");
  }

  /**
   * Gets the remediation.
   * 
   * @return the remediation
   */
  public String getRemediation()
  {
    if (remediation == null)
    {
      remediation = Constants.FIELD_VALUE_WHEN_IS_NULL;
    }
    return remediation;
  }

  /**
   * Sets the remediation.
   * 
   * @param remediation
   *          the new remediation
   */
  public void setRemediation(String remediation)
  {
    if (remediation != null)
      this.remediation = remediation.replaceAll("[\"]", "");
  }

  /**
   * Gets the reference.
   * 
   * @return the reference
   */
  public String getReference()
  {
    if (reference == null)
    {
      reference = Constants.FIELD_VALUE_WHEN_IS_NULL;
    }
    return reference;
  }

  /**
   * Sets the reference.
   * 
   * @param reference
   *          the new reference
   */
  public void setReference(String reference)
  {
    if (reference != null)
      this.reference = reference.replaceAll("[\"]", "");
  }

  /**
   * Gets the violation example.
   * 
   * @return the violation example
   */
  public String getViolationExample()
  {
    if (violationExample == null)
    {
      violationExample = Constants.FIELD_VALUE_WHEN_IS_NULL;
    }
    return violationExample;
  }

  /**
   * Sets the violation example.
   * 
   * @param violationExample
   *          the new violation example
   */
  public void setViolationExample(String violationExample)
  {
    if (violationExample != null)
      this.violationExample = violationExample.replaceAll("[\"]", "");
  }

  /**
   * Gets the output.
   * 
   * @return the output
   */
  public String getOutput()
  {
    if (output == null)
    {
      output = Constants.FIELD_VALUE_WHEN_IS_NULL;
    }
    return output;
  }

  /**
   * Sets the output.
   * 
   * @param output
   *          the new output
   */
  public void setOutput(String output)
  {
    if (output != null)
      this.output = output.replaceAll("[\"]", "");
  }

  /**
   * Gets the remediation example.
   * 
   * @return the remediation example
   */
  public String getRemediationExample()
  {
    if (remediationExample == null)
    {
      remediationExample = Constants.FIELD_VALUE_WHEN_IS_NULL;
    }
    return remediationExample;
  }

  /**
   * Sets the remediation example.
   * 
   * @param remediationExample
   *          the new remediation example
   */
  public void setRemediationExample(String remediationExample)
  {
    if (remediationExample != null)
      this.remediationExample = remediationExample.replaceAll("[\"]", "");
  }

  /**
   * Gets the totales.
   * 
   * @return the totales
   */
  public String getTotales()
  {
    if (totales == null)
    {
      totales = Constants.FIELD_VALUE_WHEN_IS_NULL;
    }
    return totales;
  }

  /**
   * Sets the totales.
   * 
   * @param totales
   *          the new totales
   */
  public void setTotales(String totales)
  {
    if (totales != null)
      this.totales = totales.replaceAll("[\"]", "");
  }

  /**
   * Gets the action date.
   * 
   * @return the actionDate
   */
  public String getActionDate()
  {
    if (actionDate == null)
    {
      actionDate = Constants.FIELD_VALUE_WHEN_IS_NULL;
    }
    return actionDate;
  }

  /**
   * Sets the action date.
   * 
   * @param actionDate
   *          the actionDate to set
   */
  public void setActionDate(String actionDate)
  {
    this.actionDate = actionDate;
  }

  public String getSourcePath()
  {
    return sourcePath;
  }

  public void setSourcePath(String sourcePath)
  {
    if (sourcePath != null)
      this.sourcePath = sourcePath.replaceAll("[\"]", "");
  }

  public int getLineStart()
  {
    return lineStart;
  }

  public void setLineStart(int lineStart)
  {
    this.lineStart = lineStart;
  }

  public int getLineEnd()
  {
    return LineEnd;
  }

  public void setLineEnd(int lineEnd)
  {
    LineEnd = lineEnd;
  }

  public String getSourceCode()
  {
    int maxChar = 28672;
    int maxLength = (sourceCode.length() < maxChar) ? sourceCode.length() : maxChar;
    return sourceCode.substring(0, maxLength) + (maxLength == maxChar ? "..." : "");
  }

  public void setSourceCode(String sourceCode)
  {
    if (sourceCode != null)
      this.sourceCode = sourceCode.replaceAll("[\"]", "");
  }

  public String getTechCriteria()
  {
    return techCriteria;
  }

  public void setTechCriteria(String techCriteria)
  {
    if (techCriteria != null)
      this.techCriteria = techCriteria.replaceAll("[\"]", "");
  }

  public String getBusinessCriteria()
  {
    return businessCriteria;
  }

  public void setBusinessCriteria(String businessCriteria)
  {
    if (businessCriteria != null)
      this.businessCriteria = businessCriteria.replaceAll("[\"]", "");
  }

  public int getViolationStatus()
  {
    return violationStatus;
  }

  public void setViolationStatus(int violationStatus)
  {
    this.violationStatus = violationStatus;
  }

  public String getFirstSnapshotDate()
  {
    return firstSnapshotDate;
  }

  public void setFirstSnapshotDate(String firstSnapshotDate)
  {
    this.firstSnapshotDate = firstSnapshotDate;
  }

  public int getMetricId()
  {
    return metricId;
  }

  public void setMetricId(int metricId)
  {
    this.metricId = metricId;
  }
}
