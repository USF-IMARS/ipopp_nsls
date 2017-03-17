/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
/******************************************************************************
*
*  NISGS/NSLS
*
*  History:
*
*   8-Feb-06, 	Original version.
*   9-Jun-06, 	Added RT-STPS.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls;
import gov.nasa.gsfc.nisgs.nsls.util.Util;

import java.util.*;

public abstract class NSLS {
  /****************************************************************************
  * The MMS subsystem.
  ****************************************************************************/
  public static class MMS extends Log.Source {
    private static final long serialVersionUID = 1L;
    /**************************************************************************
    * 
    **************************************************************************/
    public MMS () {

    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String toString () {
      return "MMS";
    }
  }
  /****************************************************************************
  * The RT-STPS subsystem.
  ****************************************************************************/
  public static class RTSTPS extends Log.Source {
    private static final long serialVersionUID = 1L;
    /**************************************************************************
    * 
    **************************************************************************/
    public RTSTPS () {

    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String toString () {
      return "RT-STPS";
    }
  }
  /****************************************************************************
  * The SC subsystem.
  ****************************************************************************/
  public static class SC extends Log.Source {
    private String subsystem;
    private static final long serialVersionUID = 1L;
    /**************************************************************************
    * 
    **************************************************************************/
    public SC (String subsystem) {
      this.subsystem = subsystem;
    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String toString () {
      return "SC" + (subsystem != null ? "/" + subsystem : "");
    }
  }
  /****************************************************************************
  * The NCS subsystem.
  ****************************************************************************/
  public static class NCS extends Log.Source {
    private String stationName;
    private String groupName;
    private String jobTag;
    private String messageType;
    private static final long serialVersionUID = 1L;
    /**************************************************************************
    * @param stationName The name of the station.
    **************************************************************************/
    public NCS (String stationName) {
      this.stationName = stationName;
      this.groupName = null;
      this.jobTag = null;
      this.messageType = null;
    }
    /**************************************************************************
    * @param stationName The name of the station.
    * @param groupName The name of the group.
    **************************************************************************/
    public NCS (String stationName, String groupName) {
      this.stationName = stationName;
      this.groupName = groupName;
      this.jobTag = null;
      this.messageType = null;
    }
    /**************************************************************************
    * @param stationName The name of the station.
    * @param groupName The name of the group.
    * @param jobTag The job tag.
    **************************************************************************/
    public NCS (String stationName, String groupName, String jobTag) {
      this.stationName = stationName;
      this.groupName = groupName;
      this.jobTag = jobTag;
    }
    /**************************************************************************
    * @param stationName The name of the station.
    * @param groupName The name of the group.
    * @param jobTag The job tag.
    * @param messageType The message type.
    **************************************************************************/
    public NCS (String stationName, String groupName,
    		String jobTag, String messageType) {
      this.stationName = stationName;
      this.groupName = groupName;
      this.jobTag = jobTag;
      this.messageType = messageType;
    }
    /**************************************************************************
    * @param stationName The name of the station.
    **************************************************************************/
    public void setStationName (String stationName) {
      this.stationName = stationName;
    }
    /**************************************************************************
    * @return The name of the station.
    **************************************************************************/
    public String getStationName () {
      return stationName;
    }
    /**************************************************************************
    * @param groupName The name of the group.
    **************************************************************************/
    public void setGroupName (String groupName) {
      this.groupName = groupName;
    }
    /**************************************************************************
    * @return The name of the group.
    **************************************************************************/
    public String getGroupName () {
      return groupName;
    }
    /**************************************************************************
    * @param jobTag The job tag.
    **************************************************************************/
    public void setJobTag (String jobTag) {
      this.jobTag = jobTag;
    }
    /**************************************************************************
    * @return The job tag.
    **************************************************************************/
    public String getJobTag () {
      return jobTag;
    }
    /**************************************************************************
    * @param messageType The message type.
    **************************************************************************/
    public void setMessageType (String messageType) {
      this.messageType = messageType;
    }
    /**************************************************************************
    * @return The message type.
    **************************************************************************/
    public String getMessageType () {
      return messageType;
    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String toString () {
      return "NCS" +
	     (getStationName() != null ? "/" + getStationName() : "") +
      	     (getGroupName() != null ? "/" + getGroupName() : "") +
      	     (getJobTag() != null ? "/" + getJobTag() : "") +
      	     (getMessageType() != null ? "/" + getMessageType() : "");
    }
  }
  /****************************************************************************
  * The DSM subsystem.
  ****************************************************************************/
  public static class DSM extends Log.Source {
    private String programName;
    private static final long serialVersionUID = 1L;
    /**************************************************************************
    * @param programName The name of the program.
    **************************************************************************/
    public DSM (String programName) {
      this.programName = programName;
    }
    /**************************************************************************
    * @return The name of the program.
    **************************************************************************/
    public String getProgramName () {
      return programName;
    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String toString () {
      return "DSM" + (getProgramName() != null ? "/" + getProgramName() : "");
    }
  }
  /****************************************************************************
  * The IS subsystem.
  ****************************************************************************/
  public static class IS extends Log.Source {
    private String programName;
    private static final long serialVersionUID = 1L;
    /**************************************************************************
    * @param programName The name of the program.
    **************************************************************************/
    public IS (String programName) {
      this.programName = programName;
    }
    /**************************************************************************
    * @return The name of the program.
    **************************************************************************/
    public String getProgramName () {
      return programName;
    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String toString () {
      return "IS" + (getProgramName() != null ? "/" + getProgramName() : "");
    }
  }
  /****************************************************************************
  * Pass parameters.
  ****************************************************************************/
  public static class PassParameters extends Log.Parameters {
    private String passId;
    private String spacecraft;
    private String groundStation;
    private Date aos;
    private Date los;
    private static final long serialVersionUID = 1L;
    /**************************************************************************
    * @param passId The pass ID.
    * @param spacecraft The name of the spacecraft.
    * @param groundStation The name of the ground station.
    * @param aos The date/time of AOS.
    * @param los The date/time of LOS.
    **************************************************************************/
    public PassParameters (String passId, String spacecraft,
    			   String groundStation, Date aos, Date los) {
      this.passId = passId;
      this.spacecraft = spacecraft;
      this.groundStation = groundStation;
      this.aos = aos;
      this.los = los;
    }
    /**************************************************************************
    * @return The pass ID.
    **************************************************************************/
    public String getPassId () {
      return passId;
    }
    /**************************************************************************
    * @return The name of the spacecraft.
    **************************************************************************/
    public String getSpacecraft () {
      return spacecraft;
    }
    /**************************************************************************
    * @return The name of the ground station.
    **************************************************************************/
    public String getGroundStation () {
      return groundStation;
    }
    /**************************************************************************
    * @return The date/time of AOS.
    **************************************************************************/
    public Date getAos () {
      return aos;
    }
    /**************************************************************************
    * @return The date/time of LOS.
    **************************************************************************/
    public Date getLos () {
      return los;
    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String toString () {
      return getPassId() + 
      	     (getSpacecraft() != null ? "/" + getSpacecraft() : "") +
      	     (getGroundStation() != null ? "/" + getGroundStation() : "") +
      	     (getAos() != null ? "/" + Util.encodeDate(getAos()) : "") +
      	     (getLos() != null ? "/" + Util.encodeDate(getLos()) : "");
    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String[] toStrings () {
      LinkedList<String> list = new LinkedList<String>();
      list.add("Pass ID: " + getPassId());
      if (getSpacecraft() != null) {
      	list.add("Spacecraft: " + getSpacecraft());
      }
      if (getGroundStation() != null) {
      	list.add("Ground Station: " + getGroundStation());
      }
      if (getAos() != null) {
      	list.add("AOS: " + Util.encodeDate(getAos()));
      }
      if (getLos() != null) {
      	list.add("LOS: " + Util.encodeDate(getLos()));
      }
      return (String[]) list.toArray(new String[list.size()]);
    }
  }
  /****************************************************************************
  * Granule parameters.
  ****************************************************************************/
  public static class GranuleParameters extends Log.Parameters {
    private String granuleId;
    private String passId;
    private static final long serialVersionUID = 1L;
    /**************************************************************************
    * @param granuleId The granule ID.
    * @param passId The pass ID.
    **************************************************************************/
    public GranuleParameters (String granuleId, String passId) {
      this.granuleId = granuleId;
      this.passId = passId;
    }
    /**************************************************************************
    * @return The granule ID.
    **************************************************************************/
    public String getGranuleId () {
      return granuleId;
    }
    /**************************************************************************
    * @return The pass ID.
    **************************************************************************/
    public String getPassId () {
      return passId;
    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String toString () {
      return getGranuleId() + (getPassId() != null ? "/" + getPassId() : "");
    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String[] toStrings () {
      LinkedList<String> list = new LinkedList<String>();
      list.add("Granule ID: " + getGranuleId());
      if (getPassId() != null) {
      	list.add("Pass ID: " + getPassId());
      }
      return (String[]) list.toArray(new String[list.size()]);
    }
  }
  /****************************************************************************
  * Product parameters.
  ****************************************************************************/
  public static class ProductParameters extends Log.Parameters {
    private String productId;
    private String granuleId;
    private String passId;
    private String productType;
    private Date start;
    private Date stop;
    private static final long serialVersionUID = 1L;
    /**************************************************************************
    * @param productId The product ID.
    * @param granuleId The granule ID.
    * @param passId The pass ID.
    * @param productType The product type.
    **************************************************************************/
    public ProductParameters (String productId, String granuleId,
    			      String passId, String productType) {
      this.productId = productId;
      this.granuleId = granuleId;
      this.passId = passId;
      this.productType = productType;
      this.start = null;
      this.stop = null;
    }
    /**************************************************************************
    * @param productId The product ID.
    * @param granuleId The granule ID.
    * @param passId The pass ID.
    * @param productType The product type.
    * @param start The start date/time of the product.
    * @param stop The stop date/time of the product.
    **************************************************************************/
    public ProductParameters (String productId, String granuleId,
    			      String passId, String productType,
			      Date start, Date stop) {
      this.productId = productId;
      this.granuleId = granuleId;
      this.passId = passId;
      this.productType = productType;
      this.start = start;
      this.stop = stop;
    }
    /**************************************************************************
    * @return The product ID.
    **************************************************************************/
    public String getProductId () {
      return productId;
    }
    /**************************************************************************
    * @return The granule ID.
    **************************************************************************/
    public String getGranuleId () {
      return granuleId;
    }
    /**************************************************************************
    * @return The pass ID.
    **************************************************************************/
    public String getPassId () {
      return passId;
    }
    /**************************************************************************
    * @return The product type.
    **************************************************************************/
    public String getProductType () {
      return productType;
    }
    /**************************************************************************
    * @return The start date/time of the product.
    **************************************************************************/
    public Date getStart () {
      return start;
    }
    /**************************************************************************
    * @return The stop date/time of the product.
    **************************************************************************/
    public Date getStop () {
      return stop;
    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String toString () {
      return getProductId() +
	     (getGranuleId() != null ? "/" + getGranuleId() : "") +
	     (getPassId() != null ? "/" + getPassId() : "") +
	     (getProductType() != null ? "/" + getProductType() : "") +
      	     (getStart() != null ? "/" + Util.encodeDate(getStart()) : "") +
      	     (getStop() != null ? "/" + Util.encodeDate(getStop()) : "");
    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String[] toStrings () {
      LinkedList<String> list = new LinkedList<String>();
      list.add("Product ID: " + getProductId());
      if (getGranuleId() != null) {
      	list.add("Granule ID: " + getGranuleId());
      }
      if (getPassId() != null) {
      	list.add("Pass ID: " + getPassId());
      }
      if (getProductType() != null) {
      	list.add("Product Type: " + getProductType());
      }
      if (getStart() != null) {
      	list.add("Start: " + Util.encodeDate(getStart()));
      }
      if (getStop() != null) {
      	list.add("Stop: " + Util.encodeDate(getStop()));
      }
      return (String[]) list.toArray(new String[list.size()]);
    }
  }
  /****************************************************************************
  * File parameters.
  ****************************************************************************/
  public static class FileParameters extends Log.Parameters {
    private String filePath;
    private String fileType;
    private long fileSize;
    private static final long serialVersionUID = 1L;
    /**************************************************************************
    * @param filePath The relative or absolute file path.
    **************************************************************************/
    public FileParameters (String filePath) {
      this.filePath = filePath;
      this.fileType = null;
      this.fileSize = 0;
    }
    /**************************************************************************
    * @param filePath The relative or absolute file path.
    * @param fileType The file type.
    * @param fileSize The file size (in bytes).
    **************************************************************************/
    public FileParameters (String filePath, String fileType, long fileSize) {
      this.filePath = filePath;
      this.fileType = fileType;
      this.fileSize = fileSize;
    }
    /**************************************************************************
    * @return The relative or absolute file path.
    **************************************************************************/
    public String getFilePath () {
      return filePath;
    }
    /**************************************************************************
    * @return The file type.
    **************************************************************************/
    public String getFileType () {
      return fileType;
    }
    /**************************************************************************
    * @return The file size (in bytes).
    **************************************************************************/
    public long getFileSize () {
      return fileSize;
    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String toString () {
      return getFilePath() +
      	     (getFileType() != null ? "/" + getFileType() : "") +
      	     (getFileSize() != 0 ? "/" + Long.toString(getFileSize()) : "");
    }
    /**************************************************************************
    * 
    **************************************************************************/
    public String[] toStrings () {
      LinkedList<String> list = new LinkedList<String>();
      list.add("File Path: " + getFilePath());
      if (getFileType() != null) {
      	list.add("File Type: " + getFileType());
      }
      if (getFileSize() != 0) {
      	list.add("File Size: " + Long.toString(getFileSize()) + " bytes");
      }
      return (String[]) list.toArray(new String[list.size()]);
    }
  }
}
