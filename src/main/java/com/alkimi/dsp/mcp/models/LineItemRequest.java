package com.alkimi.dsp.mcp.models;

import java.util.List;
import java.util.Map;

/**
 * Model for line item in campaign request
 */
public class LineItemRequest {
    private Integer id;
    private String lineItemName;
    private String description;
    private List<String> channel;
    private Integer budget;
    private List<String> countries;
    private List<String> languages;
    private List<String> devices;
    private List<String> platforms;
    private List<String> browsers;
    private Boolean alkimiProduct;
    private List<String> domainAllowList;
    private String periodFcType;
    private Integer periodFcValue;
    private Integer periodFcLimit;
    private Integer totalFcLimit;
    private String biddingStrategy;
    private Integer floorPrice;
    private Integer maxBidPrice;
    private String startDate;
    private String endDate;
    private List<Integer> creativeIds;
    private String lineItemType;
    private List<Map<String, Integer>> iabCategory;
    private List<Map<String, Integer>> contextual;
    private Integer impressionsGoal;
    private String pacing;
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getLineItemName() {
        return lineItemName;
    }
    
    public void setLineItemName(String lineItemName) {
        this.lineItemName = lineItemName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getChannel() {
        return channel;
    }
    
    public void setChannel(List<String> channel) {
        this.channel = channel;
    }
    
    public Integer getBudget() {
        return budget;
    }
    
    public void setBudget(Integer budget) {
        this.budget = budget;
    }
    
    public List<String> getCountries() {
        return countries;
    }
    
    public void setCountries(List<String> countries) {
        this.countries = countries;
    }
    
    public List<String> getLanguages() {
        return languages;
    }
    
    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }
    
    public List<String> getDevices() {
        return devices;
    }
    
    public void setDevices(List<String> devices) {
        this.devices = devices;
    }
    
    public List<String> getPlatforms() {
        return platforms;
    }
    
    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }
    
    public List<String> getBrowsers() {
        return browsers;
    }
    
    public void setBrowsers(List<String> browsers) {
        this.browsers = browsers;
    }
    
    public Boolean getAlkimiProduct() {
        return alkimiProduct;
    }
    
    public void setAlkimiProduct(Boolean alkimiProduct) {
        this.alkimiProduct = alkimiProduct;
    }
    
    public List<String> getDomainAllowList() {
        return domainAllowList;
    }
    
    public void setDomainAllowList(List<String> domainAllowList) {
        this.domainAllowList = domainAllowList;
    }
    
    public String getPeriodFcType() {
        return periodFcType;
    }
    
    public void setPeriodFcType(String periodFcType) {
        this.periodFcType = periodFcType;
    }
    
    public Integer getPeriodFcValue() {
        return periodFcValue;
    }
    
    public void setPeriodFcValue(Integer periodFcValue) {
        this.periodFcValue = periodFcValue;
    }
    
    public Integer getPeriodFcLimit() {
        return periodFcLimit;
    }
    
    public void setPeriodFcLimit(Integer periodFcLimit) {
        this.periodFcLimit = periodFcLimit;
    }
    
    public Integer getTotalFcLimit() {
        return totalFcLimit;
    }
    
    public void setTotalFcLimit(Integer totalFcLimit) {
        this.totalFcLimit = totalFcLimit;
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
    
    public List<Integer> getCreativeIds() {
        return creativeIds;
    }
    
    public void setCreativeIds(List<Integer> creativeIds) {
        this.creativeIds = creativeIds;
    }
    
    public String getLineItemType() {
        return lineItemType;
    }
    
    public void setLineItemType(String lineItemType) {
        this.lineItemType = lineItemType;
    }
    
    public List<Map<String, Integer>> getIabCategory() {
        return iabCategory;
    }
    
    public void setIabCategory(List<Map<String, Integer>> iabCategory) {
        this.iabCategory = iabCategory;
    }
    
    public List<Map<String, Integer>> getContextual() {
        return contextual;
    }
    
    public void setContextual(List<Map<String, Integer>> contextual) {
        this.contextual = contextual;
    }
    
    public Integer getImpressionsGoal() {
        return impressionsGoal;
    }
    
    public void setImpressionsGoal(Integer impressionsGoal) {
        this.impressionsGoal = impressionsGoal;
    }
    
    public String getPacing() {
        return pacing;
    }
    
    public void setPacing(String pacing) {
        this.pacing = pacing;
    }
}