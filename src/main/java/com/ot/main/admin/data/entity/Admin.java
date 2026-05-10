package com.ot.main.admin.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name="F_Admin")
@Data
public class Admin {

   @Id
   private String id;
   
   @Column (nullable = false)
   private String pw;
   
}