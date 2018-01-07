/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mindliner.web.backbeans;

import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author Ming
 */
@ManagedBean
@RequestScoped
public class ErrorBB {
    
   private static final String BR = "\n";  
  
   public String getStackTrace()  
   {  
       FacesContext context = FacesContext.getCurrentInstance();  
       Map<String,Object> map = context.getExternalContext().getRequestMap();  
       Throwable throwable = (Throwable) map.get("javax.servlet.error.exception");  
       StringBuilder builder = new StringBuilder();  
       builder.append(throwable.getMessage()).append(BR);
       System.out.println(throwable.getMessage());
  
       for (StackTraceElement element : throwable.getStackTrace())  
       {  
         builder.append(element).append(BR);  
       }  
  
       return builder.toString();  
   }  
}
