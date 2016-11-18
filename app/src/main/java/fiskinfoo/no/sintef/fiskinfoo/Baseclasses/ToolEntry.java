/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fiskinfoo.no.sintef.fiskinfoo.Baseclasses;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.UUID;

import fiskinfoo.no.sintef.fiskinfoo.Implementation.GeometryType;
import fiskinfoo.no.sintef.fiskinfoo.Implementation.GpsLocationTracker;
import fiskinfoo.no.sintef.fiskinfoo.R;

public class ToolEntry implements Parcelable {
    private String id;
    private List<Point> coordinates;
    private GeometryType geometry;
    private String IMO;
    private String IRCS;
    private String MMSI;
    private String RegNum;
    private String VesselName;
    private String VesselPhone;
    private String ContactPersonEmail;
    private String ContactPersonPhone;
    private String ContactPersonName;
    private ToolType ToolTypeCode;
    private String Source;
    private String Comment;
    private String ShortComment;
    private String RemovedTime;
    private String SetupDateTime;
    private String ToolId;
    private String LastChangedDateTime;
    private String LastChangedBySource;
    private ToolEntryStatus toolStatus;
    private int toolLogId;

    protected ToolEntry(Parcel in) {
        id = in.readString();
        coordinates = new ArrayList<>();
        in.readList(coordinates, Point.class.getClassLoader());
        geometry = GeometryType.createFromValue(in.readString());
        IMO = in.readString();
        IRCS = in.readString();
        MMSI = in.readString();
        RegNum = in.readString();
        VesselName = in.readString();
        VesselPhone = in.readString();
        ContactPersonEmail = in.readString();
        ContactPersonPhone = in.readString();
        ContactPersonName = in.readString();
        ToolTypeCode = ToolType.createFromValue(in.readString());
        Source = in.readString();
        Comment = in.readString();
        ShortComment = in.readString();
        RemovedTime = in.readString();
        SetupDateTime = in.readString();
        ToolId = in.readString();
        LastChangedDateTime = in.readString();
        LastChangedBySource = in.readString();
        toolStatus = ToolEntryStatus.createFromValue(in.readString());
        toolLogId = in.readInt();
    }

    public ToolEntry(List<Point> coordinates, String vesselName, String vesselPhone, ToolType toolType, String setupDateTime, String regNum, String contactPersonName, String contactPersonPhone, String contactPersonEmail) {
        this.coordinates = coordinates;
        this.geometry = coordinates.size() > 1 ? GeometryType.LINESTRING : GeometryType.POINT;
        this.VesselName = vesselName;
        this.VesselPhone = vesselPhone;
        this.ToolTypeCode = toolType;
        this.toolStatus = ToolEntryStatus.STATUS_UNREPORTED;
        this.RegNum = regNum;
        this.ContactPersonName = contactPersonName;
        this.ContactPersonPhone = contactPersonPhone;
        this.ContactPersonEmail = contactPersonEmail;

        this.ToolId = UUID.randomUUID().toString(); //UUID.fromString("Fiskinfo" + VesselName + SetupDateTime).toString();
        this.id = this.ToolId;

        Date setupDate = null;
        Date lastChangedDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
        String lastChangedDateString;
        String setupDateString;

        sdf.setTimeZone(TimeZone.getDefault());

        try {
            setupDate = sdf.parse(setupDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        setupDateString = sdf.format(setupDate);
        lastChangedDateString = sdf.format(lastChangedDate);

        this.SetupDateTime = setupDateString.substring(0, setupDateString.indexOf('.')).concat("Z");
        this.LastChangedBySource = lastChangedDateString.concat("Z");
        this.LastChangedDateTime = lastChangedDateString.concat("Z");

    }

    /**
     * Datetimes are assumed to be UTC
     * @param tool
     */
    public ToolEntry(JSONObject tool, Context context) {
        try {
            id = tool.getJSONObject("properties").getString("id");

            coordinates = new ArrayList<>();
            tool.getJSONObject("properties").getString("id");

            JSONArray toolJsonCoordinates = tool.getJSONObject("geometry").getJSONArray("coordinates");

            if(tool.getJSONObject("geometry").getString("type").equals(GeometryType.LINESTRING.toString())) {
                for(int i = 0; i < toolJsonCoordinates.length(); i++) {
                    JSONArray currentPoint = toolJsonCoordinates.getJSONArray(i);
                    Point point = new Point(currentPoint.getDouble(0), currentPoint.getDouble(1));
                    coordinates.add(point);
                }
            } else {
                Point point = new Point(toolJsonCoordinates.getDouble(1), toolJsonCoordinates.getDouble(0));
                coordinates.add(point);
            }

            geometry = !tool.getJSONObject("geometry").has("type") ? null : GeometryType.createFromValue(tool.getJSONObject("geometry").getString("type"));
            IMO = !tool.getJSONObject("properties").has("imo") || "null".equals(tool.getJSONObject("properties").getString("imo")) ?
                    null : tool.getJSONObject("properties").getString("imo");
            IRCS = !tool.getJSONObject("properties").has("ircs") || "null".equals(tool.getJSONObject("properties").getString("ircs")) ?
                    null : tool.getJSONObject("properties").getString("ircs");
            MMSI =  !tool.getJSONObject("properties").has("mmsi") || "null".equals(tool.getJSONObject("properties").getString("mmsi")) ?
                    null : tool.getJSONObject("properties").getString("mmsi");
            RegNum = !tool.getJSONObject("properties").has("regnum") || "null".equals(tool.getJSONObject("properties").getString("regnum")) ?
                    null : tool.getJSONObject("properties").getString("regnum");
            VesselName = !tool.getJSONObject("properties").has("vesselname") || "null".equals(tool.getJSONObject("properties").getString("vesselname")) ?
                    null : tool.getJSONObject("properties").getString("vesselname");
            VesselPhone = !tool.getJSONObject("properties").has("vesselphone") || "null".equals(tool.getJSONObject("properties").getString("vesselphone")) ?
                    null : tool.getJSONObject("properties").getString("vesselphone");
            ContactPersonEmail = !tool.getJSONObject("properties").has("contactpersonemail") || "null".equals(tool.getJSONObject("properties").getString("contactpersonemail")) ?
                    null : tool.getJSONObject("properties").getString("contactpersonemail");
            ContactPersonPhone = !tool.getJSONObject("properties").has("contactpersonphone") || "null".equals(tool.getJSONObject("properties").getString("contactpersonphone")) ?
                    null :  tool.getJSONObject("properties").getString("contactpersonphone");
            ContactPersonName = !tool.getJSONObject("properties").has("contactpersonname") || "null".equals(tool.getJSONObject("properties").getString("contactpersonname")) ?
                    null : tool.getJSONObject("properties").getString("contactpersonname");
            ToolTypeCode = !tool.getJSONObject("properties").has("tooltypecode") || "null".equals(tool.getJSONObject("properties").getString("tooltypecode")) ?
                    null : ToolType.createFromValue(tool.getJSONObject("properties").getString("tooltypecode"));
            Source = !tool.getJSONObject("properties").has("source") || "null".equals(tool.getJSONObject("properties").getString("source")) ?
                    null : tool.getJSONObject("properties").getString("source");
            Comment = !tool.getJSONObject("properties").has("comment") || "null".equals(tool.getJSONObject("properties").getString("comment")) ?
                    null : tool.getJSONObject("properties").getString("comment");
            ShortComment = !tool.getJSONObject("properties").has("shortcomment") || "null".equals(tool.getJSONObject("properties").getString("shortcomment")) ?
                    null : tool.getJSONObject("properties").getString("shortcomment");
            RemovedTime = !tool.getJSONObject("properties").has("removeddatetime") || "null".equals(tool.getJSONObject("properties").getString("removeddatetime")) ?
                    null : tool.getJSONObject("properties").getString("removeddatetime");
            ToolId = !tool.getJSONObject("properties").has("toolid") || "null".equals(tool.getJSONObject("properties").getString("toolid")) ?
                    null : tool.getJSONObject("properties").getString("toolid");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            SimpleDateFormat sdfMilliSeconds = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

            Date serverSetupDateTime;
            Date serverUpdatedBySourceDateTime;
            Date serverLastUpdatedDateTime;

            if(tool.getJSONObject("properties").has("setupdatetime") && !"null".equals(tool.getJSONObject("properties").getString("setupdatetime"))) {
                sdf.setTimeZone(TimeZone.getTimeZone("GMT-1"));
                sdfMilliSeconds.setTimeZone(TimeZone.getTimeZone("GMT-1"));

                serverSetupDateTime = tool.getJSONObject("properties").getString("setupdatetime").length() == context.getResources().getInteger(R.integer.datetime_without_milliseconds_length) ?
                        sdf.parse(tool.getJSONObject("properties").getString("setupdatetime")) : sdfMilliSeconds.parse(tool.getJSONObject("properties").getString("setupdatetime"));

                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                SetupDateTime = sdf.format(serverSetupDateTime);
            }

            if(tool.getJSONObject("properties").has("lastchangeddatetime") && !"null".equals(tool.getJSONObject("properties").getString("lastchangeddatetime"))) {
                sdf.setTimeZone(TimeZone.getTimeZone("GMT-1"));
                sdfMilliSeconds.setTimeZone(TimeZone.getTimeZone("GMT-1"));

                serverLastUpdatedDateTime = tool.getJSONObject("properties").getString("lastchangeddatetime").length() == context.getResources().getInteger(R.integer.datetime_without_milliseconds_length) ?
                        sdf.parse(tool.getJSONObject("properties").getString("lastchangeddatetime")) : sdfMilliSeconds.parse(tool.getJSONObject("properties").getString("lastchangeddatetime"));

                sdfMilliSeconds.setTimeZone(TimeZone.getTimeZone("UTC"));

                LastChangedDateTime = sdfMilliSeconds.format(serverLastUpdatedDateTime);
            }
            if(tool.getJSONObject("properties").has("lastchangedbysource") && !"null".equals(tool.getJSONObject("properties").getString("lastchangedbysource"))) {
                sdf.setTimeZone(TimeZone.getTimeZone("GMT-1"));
                sdfMilliSeconds.setTimeZone(TimeZone.getTimeZone("GMT-1"));

                serverUpdatedBySourceDateTime = tool.getJSONObject("properties").getString("lastchangedbysource").length() == context.getResources().getInteger(R.integer.datetime_without_milliseconds_length) ?
                        sdf.parse(tool.getJSONObject("properties").getString("lastchangedbysource")) : sdfMilliSeconds.parse(tool.getJSONObject("properties").getString("lastchangedbysource"));

                sdfMilliSeconds.setTimeZone(TimeZone.getTimeZone("UTC"));

                LastChangedBySource = sdfMilliSeconds.format(serverUpdatedBySourceDateTime);
            }

            toolStatus =  ToolEntryStatus.STATUS_RECEIVED;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Datetimes are assumed to be UTC
     * @param tool
     */
    public void updateFromGeoJson(JSONObject tool, Context context) {
        try {
            id = tool.getJSONObject("properties").getString("id");

            coordinates = new ArrayList<>();
            tool.getJSONObject("properties").getString("id");

            JSONArray toolJsonCoordinates = tool.getJSONObject("geometry").getJSONArray("coordinates");

            if(tool.getJSONObject("geometry").getString("type").equals(GeometryType.LINESTRING.toString())) {
                for(int i = 0; i < toolJsonCoordinates.length(); i++) {
                    JSONArray currentPoint = toolJsonCoordinates.getJSONArray(i);
                    Point point = new Point(currentPoint.getDouble(1), currentPoint.getDouble(0));
                    coordinates.add(point);
                }
            } else {
                Point point = new Point(toolJsonCoordinates.getDouble(1), toolJsonCoordinates.getDouble(0));
                coordinates.add(point);
            }

            geometry = !tool.getJSONObject("geometry").has("type") ? geometry : GeometryType.createFromValue(tool.getJSONObject("geometry").getString("type"));
            IMO = !tool.getJSONObject("properties").has("imo") ? IMO :
                    (tool.getJSONObject("properties").getString("imo") == null || "null".equals(tool.getJSONObject("properties").getString("imo"))) ?
                            IMO : tool.getJSONObject("properties").getString("imo");
            IRCS = !tool.getJSONObject("properties").has("ircs") ? IRCS :
                    (tool.getJSONObject("properties").getString("ircs") == null || "null".equals(tool.getJSONObject("properties").getString("ircs"))) ?
                            IRCS : tool.getJSONObject("properties").getString("ircs");
            MMSI =  !tool.getJSONObject("properties").has("mmsi") ? MMSI :
                    (tool.getJSONObject("properties").getString("mmsi") == null || "null".equals(tool.getJSONObject("properties").getString("mmsi"))) ?
                            MMSI : tool.getJSONObject("properties").getString("mmsi");
            RegNum = !tool.getJSONObject("properties").has("regnum") ? RegNum :
                    (tool.getJSONObject("properties").getString("regnum") == null || "null".equals(tool.getJSONObject("properties").getString("regnum"))) ?
                            RegNum : tool.getJSONObject("properties").getString("regnum");
            VesselName = !tool.getJSONObject("properties").has("vesselname") ? VesselName :
                    (tool.getJSONObject("properties").getString("vesselname") == null || "null".equals(tool.getJSONObject("properties").getString("vesselname"))) ?
                            VesselName : tool.getJSONObject("properties").getString("vesselname");
            VesselPhone = !tool.getJSONObject("properties").has("vesselphone") ? VesselPhone :
                    (tool.getJSONObject("properties").getString("vesselphone") == null || "null".equals(tool.getJSONObject("properties").getString("vesselphone"))) ?
                            VesselPhone : tool.getJSONObject("properties").getString("vesselphone");
            ContactPersonEmail = !tool.getJSONObject("properties").has("contactpersonemail") ? ContactPersonEmail :
                    (tool.getJSONObject("properties").getString("contactpersonemail") == null || "null".equals(tool.getJSONObject("properties").getString("contactpersonemail"))) ?
                            ContactPersonEmail : tool.getJSONObject("properties").getString("contactpersonemail");
            ContactPersonPhone = !tool.getJSONObject("properties").has("contactpersonphone") ? ContactPersonPhone :
                    (tool.getJSONObject("properties").getString("contactpersonphone") == null || "null".equals(tool.getJSONObject("properties").getString("contactpersonphone"))) ?
                            ContactPersonPhone : tool.getJSONObject("properties").getString("contactpersonphone");
            ContactPersonName = !tool.getJSONObject("properties").has("contactpersonname") ? ContactPersonName :
                    (tool.getJSONObject("properties").getString("contactpersonname") == null || "null".equals(tool.getJSONObject("properties").getString("contactpersonname"))) ?
                            ContactPersonName : tool.getJSONObject("properties").getString("contactpersonname");
            ToolTypeCode = !tool.getJSONObject("properties").has("tooltypecode") ? ToolTypeCode :
                    (tool.getJSONObject("properties").getString("tooltypecode") == null || "null".equals(tool.getJSONObject("properties").getString("tooltypecode"))) ?
                            ToolTypeCode : ToolType.createFromValue(tool.getJSONObject("properties").getString("tooltypecode"));
            Source = !tool.getJSONObject("properties").has("source") ? Source :
                    (tool.getJSONObject("properties").getString("source") == null || "null".equals(tool.getJSONObject("properties").getString("source"))) ?
                            Source : tool.getJSONObject("properties").getString("source");
            Comment = !tool.getJSONObject("properties").has("comment") ? Comment :
                    (tool.getJSONObject("properties").getString("comment") == null || "null".equals(tool.getJSONObject("properties").getString("comment"))) ?
                            Comment : tool.getJSONObject("properties").getString("comment");
            ShortComment = !tool.getJSONObject("properties").has("shortcomment") ? ShortComment :
                    (tool.getJSONObject("properties").getString("shortcomment") == null || "null".equals(tool.getJSONObject("properties").getString("shortcomment"))) ?
                            ShortComment : tool.getJSONObject("properties").getString("shortcomment");
            RemovedTime = !tool.getJSONObject("properties").has("removeddatetime") ? RemovedTime :
                    (tool.getJSONObject("properties").getString("removeddatetime") == null || "null".equals(tool.getJSONObject("properties").getString("removeddatetime"))) ?
                            RemovedTime : tool.getJSONObject("properties").getString("removeddatetime");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            SimpleDateFormat sdfMilliSeconds = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

            Date serverSetupDateTime;
            Date serverUpdatedBySourceDateTime;
            Date serverLastUpdatedDateTime;

            if(tool.getJSONObject("properties").has("setupdatetime") && !"null".equals(tool.getJSONObject("properties").getString("setupdatetime"))) {
                sdf.setTimeZone(TimeZone.getTimeZone("GMT-1"));
                sdfMilliSeconds.setTimeZone(TimeZone.getTimeZone("GMT-1"));

                serverSetupDateTime = tool.getJSONObject("properties").getString("setupdatetime").length() == context.getResources().getInteger(R.integer.datetime_without_milliseconds_length) ?
                        sdf.parse(tool.getJSONObject("properties").getString("setupdatetime")) : sdfMilliSeconds.parse(tool.getJSONObject("properties").getString("setupdatetime"));

                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                SetupDateTime = sdf.format(serverSetupDateTime);
            }

            if(tool.getJSONObject("properties").has("lastchangeddatetime") && !"null".equals(tool.getJSONObject("properties").getString("lastchangeddatetime"))) {
                sdf.setTimeZone(TimeZone.getTimeZone("GMT-1"));
                sdfMilliSeconds.setTimeZone(TimeZone.getTimeZone("GMT-1"));

                serverLastUpdatedDateTime = tool.getJSONObject("properties").getString("lastchangeddatetime").length() == context.getResources().getInteger(R.integer.datetime_without_milliseconds_length) ?
                        sdf.parse(tool.getJSONObject("properties").getString("lastchangeddatetime")) : sdfMilliSeconds.parse(tool.getJSONObject("properties").getString("lastchangeddatetime"));

                sdfMilliSeconds.setTimeZone(TimeZone.getTimeZone("UTC"));

                LastChangedDateTime = sdfMilliSeconds.format(serverLastUpdatedDateTime);
            }
            if(tool.getJSONObject("properties").has("lastchangedbysource") && !"null".equals(tool.getJSONObject("properties").getString("lastchangedbysource"))) {
                sdf.setTimeZone(TimeZone.getTimeZone("GMT-1"));
                sdfMilliSeconds.setTimeZone(TimeZone.getTimeZone("GMT-1"));

                serverUpdatedBySourceDateTime = tool.getJSONObject("properties").getString("lastchangedbysource").length() == context.getResources().getInteger(R.integer.datetime_without_milliseconds_length) ?
                        sdf.parse(tool.getJSONObject("properties").getString("lastchangedbysource")) : sdfMilliSeconds.parse(tool.getJSONObject("properties").getString("lastchangedbysource"));

                sdfMilliSeconds.setTimeZone(TimeZone.getTimeZone("UTC"));

                LastChangedBySource = sdfMilliSeconds.format(serverUpdatedBySourceDateTime);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toGeoJson(GpsLocationTracker gpsLocationTracker) {
        JSONObject geoJsonTool = new JSONObject();
        try {
            JSONObject geometry = new JSONObject();
            JSONObject properties = new JSONObject();
            JSONArray Toolcoordinates = new JSONArray();

            if(this.geometry == GeometryType.POINT) {
                Toolcoordinates.put(coordinates.get(0).getLongitude());
                Toolcoordinates.put(coordinates.get(0).getLatitude());
            } else {
                for(Point currentPosition : this.coordinates) {
                    JSONArray position = new JSONArray();
                    position.put(currentPosition.getLongitude());
                    position.put(currentPosition.getLatitude());

                    Toolcoordinates.put(position);
                }
            }

            geoJsonTool.put("id", this.id == null ? JSONObject.NULL : this.id);
            geometry.put("coordinates", Toolcoordinates == null ? JSONObject.NULL : Toolcoordinates);
            geometry.put("type", this.geometry == null ? JSONObject.NULL : this.geometry.toString());
            geometry.put("crs", JSONObject.NULL);
            geometry.put("bbox", JSONObject.NULL);
            geoJsonTool.put("geometry", geometry);


            properties.put("IMO", this.IMO == null ? JSONObject.NULL : this.IMO);
            properties.put("IRCS", this.IRCS == null ? JSONObject.NULL : this.IRCS);
            properties.put("MMSI", this.MMSI == null ? JSONObject.NULL : this.MMSI);
            properties.put("VesselName", this.VesselName == null ? JSONObject.NULL : this.VesselName);
            properties.put("VesselPhone", this.VesselPhone == null ? JSONObject.NULL : this.VesselPhone);
            properties.put("ToolTypeCode", this.ToolTypeCode == null ? JSONObject.NULL : this.ToolTypeCode.getToolCode());
            properties.put("ToolTypeName", this.ToolTypeCode == null ? JSONObject.NULL : this.ToolTypeCode.toString());
            properties.put("Source", this.Source == null ? JSONObject.NULL : this.Source);
            properties.put("Comment", this.Comment == null ? JSONObject.NULL : this.Comment);
            properties.put("ShortComment", this.ShortComment == null ? JSONObject.NULL : this.ShortComment);
            properties.put("RemovedTime", this.RemovedTime == null ? JSONObject.NULL : this.RemovedTime);
            properties.put("SetupTime", this.SetupDateTime == null ? JSONObject.NULL : this.SetupDateTime);
            properties.put("ToolColor", "#" + Integer.toHexString(this.ToolTypeCode.getHexColorValue()).toUpperCase());
            properties.put("ToolId", this.ToolId == null ? JSONObject.NULL : this.ToolId);
            properties.put("LastChangedDateTime", this.LastChangedDateTime == null ? JSONObject.NULL : this.LastChangedDateTime);
            properties.put("LastChangedBySource", this.LastChangedBySource == null ? JSONObject.NULL : this.LastChangedBySource);
            properties.put("ContactPersonName", this.ContactPersonName == null ? JSONObject.NULL : this.ContactPersonName);
            properties.put("ContactPersonPhone", this.ContactPersonPhone == null ? JSONObject.NULL : this.ContactPersonPhone);
            properties.put("ContactPersonEmail", this.ContactPersonEmail == null ? JSONObject.NULL : this.ContactPersonEmail);

            Date toolReportDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            double toolReportingLatitude = gpsLocationTracker.getLatitude();
            double toolReportingLongitude = gpsLocationTracker.getLongitude();

            properties.put("CurrentTime", sdf.format(toolReportDate));
            properties.put("CurrentPositionLat ", toolReportingLatitude);
            properties.put("CurrentPositionLon", toolReportingLongitude);

            if(this.toolStatus == ToolEntryStatus.STATUS_REMOVED_UNCONFIRMED && this.RemovedTime != null) {
                properties.put("removeddatetime", this.RemovedTime);
            }

            geoJsonTool.put("properties", properties);

            geoJsonTool.put("type", "Feature");
            geoJsonTool.put("crs", JSONObject.NULL);
            geoJsonTool.put("bbox", JSONObject.NULL);

            return geoJsonTool;
        } catch (JSONException e) {
            e.printStackTrace();

            return null;
        }
    }

    public static final Creator<ToolEntry> CREATOR = new Creator<ToolEntry>() {
        @Override
        public ToolEntry createFromParcel(Parcel in) {
            return new ToolEntry(in);
        }

        @Override
        public ToolEntry[] newArray(int size) {
            return new ToolEntry[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeList(coordinates);
        dest.writeString(geometry.toString());
        dest.writeString(IMO);
        dest.writeString(IRCS);
        dest.writeString(MMSI);
        dest.writeString(RegNum);
        dest.writeString(VesselName);
        dest.writeString(VesselPhone);
        dest.writeString(ContactPersonEmail);
        dest.writeString(ContactPersonPhone);
        dest.writeString(ContactPersonName);
        dest.writeString(ToolTypeCode.toString());
        dest.writeString(Source);
        dest.writeString(Comment);
        dest.writeString(ShortComment);
        dest.writeString(RemovedTime);
        dest.writeString(SetupDateTime);
        dest.writeString(ToolId);
        dest.writeString(LastChangedDateTime);
        dest.writeString(LastChangedBySource);
        dest.writeString(toolStatus.toString());
        dest.writeInt(toolLogId);
    }

    public String getId() {
        return id == null ? "" : id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Point> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Point> coordinates) {
        this.coordinates = coordinates;
        this.geometry = coordinates.size() > 1 ? GeometryType.LINESTRING : GeometryType.POINT;
    }

    public GeometryType getGeometry() {
        return geometry;
    }

    public void setGeometry(GeometryType geometry) {
        this.geometry = geometry;
    }

    public String getIMO() {
        return IMO == null ? "" : IMO;
    }

    public void setIMO(String IMO) {
        this.IMO = IMO;
    }

    public String getIRCS() {
        return IRCS == null ? "" : IRCS;
    }

    public void setIRCS(String IRCS) {
        this.IRCS = IRCS;
    }

    public String getMMSI() {
        return MMSI == null ? "" : MMSI;
    }

    public void setMMSI(String MMSI) {
        this.MMSI = MMSI;
    }

    public String getRegNum() {
        return RegNum == null ? "" : RegNum;
    }

    public void setRegNum(String regNum) {
        RegNum = regNum;
    }

    public String getVesselName() {
        return VesselName == null ? "" : VesselName;
    }

    public void setVesselName(String vesselName) {
        VesselName = vesselName;
    }

    public String getVesselPhone() {
        return VesselPhone == null ? "" : VesselPhone;
    }

    public void setVesselPhone(String vesselPhone) {
        VesselPhone = vesselPhone;
    }

    public ToolType getToolType() {
        return ToolTypeCode;
    }

    public void setToolType(ToolType toolType) {
        this.ToolTypeCode = toolType;
    }

    public String getComment() {
        return Comment == null ? "" : Comment;
    }

    public void setComment(String comment) {
        if(comment == null || comment.isEmpty()) {
            return;
        }

        Comment = comment;
    }

    public String getShortComment() {
        return ShortComment == null ? "" : ShortComment;
    }

    public void setShortComment(String shortComment) {
        ShortComment = shortComment;
    }

    public String getRemovedTime() {
        return RemovedTime == null ? "" : RemovedTime;
    }

    public void setRemovedTime(String removedTime) {
        RemovedTime = removedTime;
    }

    public String getSetupDateTime() {
        return SetupDateTime == null ? "" : SetupDateTime;
    }

    public String getSetupDate() {
        return SetupDateTime == null ? "" : SetupDateTime.substring(0, 10);
    }

    public void setSetupDateTime(String setupDateTime) {
        SetupDateTime = setupDateTime;
    }

    public String getToolId() {
        return ToolId == null ? "" : ToolId;
    }

    public void setToolId(String toolId) {
        ToolId = toolId;
    }

    public String getLastChangedDateTime() {
        return LastChangedDateTime == null ? "" : LastChangedDateTime;
    }

    public void setLastChangedDateTime(String lastChangedDateTime) {
        LastChangedDateTime = lastChangedDateTime;
    }

    public String getLastChangedBySource() {
        return LastChangedBySource == null ? "" : LastChangedBySource;
    }

    public void setLastChangedBySource(String lastChangedDateTime) {
        LastChangedBySource = lastChangedDateTime;
    }

    public ToolEntryStatus getToolStatus() {
        return toolStatus;
    }

    public void setToolStatus(ToolEntryStatus toolStatus) {
        this.toolStatus = toolStatus;
    }

    public String getContactPersonEmail() {
        return ContactPersonEmail == null ? "" : ContactPersonEmail;
    }

    public void setContactPersonEmail(String contactPersonEmail) {
        ContactPersonEmail = contactPersonEmail;
    }

    public String getContactPersonPhone() {
        return ContactPersonPhone == null ? "" : ContactPersonPhone;
    }

    public void setContactPersonPhone(String contactPersonPhone) {
        ContactPersonPhone = contactPersonPhone;
    }

    public String getContactPersonName() {
        return ContactPersonName == null ? "" : ContactPersonName;
    }

    public void setContactPersonName(String contactPersonName) {
        ContactPersonName = contactPersonName;
    }

    public String getSource() {
        return Source == null ? "" : Source;
    }

    public void setSource(String source) {
        Source = source;
    }

    public ToolType getToolTypeCode() {
        return ToolTypeCode;
    }

    public void setToolTypeCode(ToolType toolTypeCode) {
        ToolTypeCode = toolTypeCode;
    }

    public int getToolLogId () {
        return toolLogId;
    }

    public void setToolLogId(int id) {
        toolLogId = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
