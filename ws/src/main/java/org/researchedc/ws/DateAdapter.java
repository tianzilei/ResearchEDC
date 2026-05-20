package org.researchedc.ws;


import org.researchedc.exception.OpenClinicaSystemException;

import java.util.Date;
import java.text.SimpleDateFormat;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.XMLGregorianCalendar;

public class DateAdapter extends XmlAdapter<String, Date> {

    // the desired format
    private String pattern = "MM/dd/yyyy";
    
    public String marshal(Date date) throws Exception {
        return new SimpleDateFormat(pattern).format(date);
    }
    
    public Date unmarshal(String dateString) throws Exception {
        throw new OpenClinicaSystemException("Please implement me!!");
    } 
    
}

