package com.vektorcontext.dto;

public class DivergenceRankingItem {

    private String name;
    private Long total;

    public DivergenceRankingItem(String name, Long total) {
        this.name = name;
        this.total = total;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
}