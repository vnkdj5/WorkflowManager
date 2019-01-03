package com.workflow.bean;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.workflow.component.Component;

public class GraphNode {

    String CId;
    Component component;
    String name;
    double x;
    double y;
    String category;

    public GraphNode(String CId, Component component, String category, double x, double y) {
        this.CId = CId;
        this.component = component;
        this.category = category;
        this.x = x;
        this.y = y;
    }

    public String getCId() {
        return CId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public String getCategory() {
        return category;
    }

    public double getX() {
        return x;
    }

    public void setXY(double x, double y) {
        this.x = x;
        this.y=y;
    }

    public double getY() {
        return y;
    }

}
