package com.xuecheng.content.model.po;


public class CourseCategory {

  private String id;
  private String name;
  private String label;
  private String parentid;
  private long isShow;
  private long orderby;
  private long isLeaf;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }


  public String getParentid() {
    return parentid;
  }

  public void setParentid(String parentid) {
    this.parentid = parentid;
  }


  public long getIsShow() {
    return isShow;
  }

  public void setIsShow(long isShow) {
    this.isShow = isShow;
  }


  public long getOrderby() {
    return orderby;
  }

  public void setOrderby(long orderby) {
    this.orderby = orderby;
  }


  public long getIsLeaf() {
    return isLeaf;
  }

  public void setIsLeaf(long isLeaf) {
    this.isLeaf = isLeaf;
  }

}
