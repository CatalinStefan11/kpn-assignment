package com.kpn.datastreaming.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class Outage {

  private String endDate;
  private String title;
  private String postalCodes;
  private String status;
  private String startDate;
  private String description;
  @JsonIgnore
  private Type type;

  public Outage() {
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getPostalCodes() {
    return postalCodes;
  }

  public void setPostalCodes(String postalCodes) {
    this.postalCodes = postalCodes;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "Outage{" +
        "endDate='" + endDate + '\'' +
        ", title='" + title + '\'' +
        ", postalCodes='" + postalCodes + '\'' +
        ", status='" + status + '\'' +
        ", startDate='" + startDate + '\'' +
        ", description='" + description + '\'' +
        ", type=" + type +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Outage)) return false;
    Outage outage = (Outage) o;
    return Objects.equals(endDate, outage.endDate) &&
        Objects.equals(title, outage.title) && 
        Objects.equals(postalCodes, outage.postalCodes) && 
        Objects.equals(status, outage.status) && 
        Objects.equals(startDate, outage.startDate) && 
        Objects.equals(description, outage.description) && 
        type == outage.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(endDate, title, postalCodes, status, startDate, description, type);
  }

  public enum Type {
    BUSINESS("business_outages.json"),
    CUSTOMER("customer_outages.json");

    private String outputName;

    Type(String outputName) {
      this.outputName = outputName;
    }

    public String getOutputName() {
      return outputName;
    }
  }
}
