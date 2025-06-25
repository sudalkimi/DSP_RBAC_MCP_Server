package com.alkimi.dsp.mcp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Model for campaign creation request
 */
public class CampaignRequest {
    private String name;
    private Integer advertiserId;
    private List<Integer> sspIds;
    private String timezone;
    private String startDate;
    private String endDate;
    private Integer totalBudget;
    private String pacing;
    private String biddingStrategy;
    private Integer floorPrice;
    private Integer maxBidPrice;
    private List<Map<String, Integer>> iabCategory;
    private List<String> languages;
    private List<String> domainAllowList;
    private List<String> countries;
    private List<String> devices;
    private List<String> browsers;
    private List<String> platforms;
    private List<LineItemRequest> lineItems;
    private String businessGoals;
    private String product;
    private Integer impressionsGoal;
    private List<Map<String, Integer>> contextual;
    private String description;
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getAdvertiserId() {
        return advertiserId;
    }
    
    public void setAdvertiserId(Integer advertiserId) {
        this.advertiserId = advertiserId;
    }
    
    public List<Integer> getSspIds() {
        return sspIds;
    }
    
    public void setSspIds(List<Integer> sspIds) {
        this.sspIds = sspIds;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getEndDate() {
        return endDate;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public Integer getTotalBudget() {
        return totalBudget;
    }
    
    public void setTotalBudget(Integer totalBudget) {
        this.totalBudget = totalBudget;
    }
    
    public String getPacing() {
        return pacing;
    }
    
    public void setPacing(String pacing) {
        this.pacing = pacing;
    }
    
    public String getBiddingStrategy() {
        return biddingStrategy;
    }
    
    public void setBiddingStrategy(String biddingStrategy) {
        this.biddingStrategy = biddingStrategy;
    }
    
    public Integer getFloorPrice() {
        return floorPrice;
    }
    
    public void setFloorPrice(Integer floorPrice) {
        this.floorPrice = floorPrice;
    }
    
    public Integer getMaxBidPrice() {
        return maxBidPrice;
    }
    
    public void setMaxBidPrice(Integer maxBidPrice) {
        this.maxBidPrice = maxBidPrice;
    }
    
    public List<Map<String, Integer>> getIabCategory() {
        return iabCategory;
    }
    
    public void setIabCategory(List<Map<String, Integer>> iabCategory) {
        this.iabCategory = iabCategory;
    }
    
    public List<String> getLanguages() {
        return languages;
    }
    
    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }
    
    public List<String> getDomainAllowList() {
        return domainAllowList;
    }
    
    public void setDomainAllowList(List<String> domainAllowList) {
        this.domainAllowList = domainAllowList;
    }
    
    public List<String> getCountries() {
        return countries;
    }
    
    public void setCountries(List<String> countries) {
        this.countries = countries;
    }
    
    public List<String> getDevices() {
        return devices;
    }
    
    public void setDevices(List<String> devices) {
        this.devices = devices;
    }
    
    public List<String> getBrowsers() {
        return browsers;
    }
    
    public void setBrowsers(List<String> browsers) {
        this.browsers = browsers;
    }
    
    public List<String> getPlatforms() {
        return platforms;
    }
    
    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }
    
    public List<LineItemRequest> getLineItems() {
        return lineItems;
    }
    
    public void setLineItems(List<LineItemRequest> lineItems) {
        this.lineItems = lineItems;
    }
    
    public String getBusinessGoals() {
        return businessGoals;
    }
    
    public void setBusinessGoals(String businessGoals) {
        this.businessGoals = businessGoals;
    }
    
    public String getProduct() {
        return product;
    }
    
    public void setProduct(String product) {
        this.product = product;
    }
    
    public Integer getImpressionsGoal() {
        return impressionsGoal;
    }
    
    public void setImpressionsGoal(Integer impressionsGoal) {
        this.impressionsGoal = impressionsGoal;
    }
    
    public List<Map<String, Integer>> getContextual() {
        return contextual;
    }
    
    public void setContextual(List<Map<String, Integer>> contextual) {
        this.contextual = contextual;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}