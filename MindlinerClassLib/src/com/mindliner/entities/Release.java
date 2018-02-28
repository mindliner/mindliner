package com.mindliner.entities;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * This class manages the Mindliner releases. It is a system table and as such
 * it is not managed by users but by the application itself. It ensures that the
 * various parts of the application (server, client, web) stays compatible and
 * informs about actions otherwise.
 *
 * @author Marius Messerli
 */
@Entity
@Table(name = "sys_releases")
@NamedQueries({
    @NamedQuery(name = "Release.findAll", query = "SELECT r FROM Release r"),
    @NamedQuery(name = "Release.findByVersionNumber", query = "SELECT r FROM Release r WHERE r.versionNumber = :versionNumber")})
public class Release implements Serializable {

    /**
     * Change the following two records for every release, set devstate to false
     * and change the version string
     */
    private static final boolean DEVELOPMENT_STATE = false;
    public static final String VERSION_STRING = "Mindliner 2.6.25";

    // YYYYMMDDNN where nn is just a two-digit running number for releases of the day
    public static final int VERSION_NUMBER = 2018022801;

    // make this the oldest version that is still compatible with this server
    public static final int OLDEST_DESKTOP_VERSION = 2017092501;

    /**
     * Returns whether or not the application is in development
     *
     * @return True if this software is in development state, false otherwise
     * (production state)
     */
    public static boolean isDevelopmentState() {
        return DEVELOPMENT_STATE;
    }

    static {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CET"));
        RELEASE_DATE = cal.getTime();
    }

    private static final String DEFAULT_WEBSTART_URL = "http://arboardone.mindliner.com:8080/MINDLINER489/MindlinerDesktop";
    public static final String DEFAULT_CLIENT_ADMIN_PAGE = "https://mls1.mindliner.com:8181/MF2/faces/account.xhtml";
    public static final String DEFAULT_SUBSCRIPTION_ADMIN_PAGE = "https://mls1.mindliner.com:8181/MF2/faces/news.xhtml?tabidx=1";
    private static final String DEFAULT_DISTRIBUTION_URL_BASE = "http://www.mindliner.com/releases/";
    private static final String DEFAULT_DISTRIBUTION_FILE_NAME = "MindlinerDesktop.zip";
    public static final String WEBAPP_OBJECT_VIEWER_URL = "https://mls1.mindliner.com:8181/MF2/faces/workspace.xhtml";

    /**
     * Describes the status of the client compared to the server.
     */
    public static enum ClientStatus {

        Outdated, // won't run, needs to be upgraded
        OlderAndCompatible, // still runs but needs to be updated soon
        Current, // identical to the server
        NewerAndCompatible, // newer than the server but compatible
        TooNew // won't run, needs downgrading
    }

    // The release date, constructed in the static constructor above
    public static final Date RELEASE_DATE;

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "ID")
    private Integer id;
    @Column(name = "VERSION_STRING")
    private String versionString;
    @Column(name = "VERSION_NUMBER")
    private int versionNumber;
    @Column(name = "OLDEST_DESKTOP_VERSION")
    private int oldestDesktopVersion;
    @Column(name = "LATEST_DESKTOP_VERSION")
    private int latestDesktopVersion;
    @Column(name = "WEBSTART_URL")
    private String webstartUrl = "";
    @Column(name = "DISTRIBUTION_URL")
    private String distributionUrl = "";
    @Column(name = "RELEASE_NOTES_URL")
    private String releaseNotesUrl = "";
    @Column(name = "RELEASE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date releaseDate = new Date();

    public Release() {
        webstartUrl = DEFAULT_WEBSTART_URL;
        distributionUrl = DEFAULT_DISTRIBUTION_URL_BASE.concat(DEFAULT_DISTRIBUTION_FILE_NAME);
    }

    public Release(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersionString() {
        return versionString;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public int getOldestDesktopVersion() {
        return oldestDesktopVersion;
    }

    public void setOldestDesktopVersion(int oldestDesktopVersion) {
        this.oldestDesktopVersion = oldestDesktopVersion;
    }

    public int getLatestDesktopVersion() {
        return latestDesktopVersion;
    }

    public void setLatestDesktopVersion(int latestDesktopVersion) {
        this.latestDesktopVersion = latestDesktopVersion;
    }

    public String getWebstartUrl() {
        return webstartUrl;
    }

    public void setWebstartUrl(String webstartUrl) {
        this.webstartUrl = webstartUrl;
    }

    public String getDistributionUrl() {
        return distributionUrl;
    }

    public void setDistributionUrl(String distributionUrl) {
        this.distributionUrl = distributionUrl;
    }

    public String getReleaseNotesUrl() {
        return releaseNotesUrl;
    }

    public void setReleaseNotesUrl(String releaseNotesUrl) {
        this.releaseNotesUrl = releaseNotesUrl;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Release)) {
            return false;
        }
        Release other = (Release) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    /**
     * Determins if the client is still compatible.
     *
     * @param currentClientVersion The software version of the calling client
     * @return A status indicated how the client software version compares to
     * the server version
     */
    public ClientStatus getClientStatus(int currentClientVersion) {

        if (currentClientVersion < getOldestDesktopVersion()) {
            return ClientStatus.Outdated;
        }
        if (currentClientVersion < getVersionNumber()) {
            return ClientStatus.OlderAndCompatible;
        }
        if (currentClientVersion == getVersionNumber()) {
            return ClientStatus.Current;
        }
        if (currentClientVersion <= getLatestDesktopVersion()) {
            return ClientStatus.NewerAndCompatible;
        }
        return ClientStatus.TooNew;
    }

    @Override
    public String toString() {
        return versionString;
    }

}
