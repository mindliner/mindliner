package com.mindliner.managers;

import com.mindliner.entities.MlUserPreferences;
import com.mindliner.entities.mlsLog;
import com.mindliner.entities.mlsObject;
import com.mindliner.entities.mlsUser;
import java.util.Objects;

/**
 * News candidates tie together the log record which gives rise to news, the
 * underlying log object and the users preferences which contains information
 * about news delivery choices.
 *
 * @author Marius Messerli
 */
public class NewsCandidate {

    MlUserPreferences userPrefs;
    mlsLog logRecord;
    mlsObject logObject;

    public NewsCandidate(MlUserPreferences userPrefs, mlsLog logRecord, mlsObject logObject) {
        this.logRecord = logRecord;
        this.logObject = logObject;
        this.userPrefs = userPrefs;
    }

    public mlsUser getActor() {
        return logRecord == null ? null : logRecord.getUser();
    }

    public mlsLog getLogRecord() {
        return logRecord;
    }

    public mlsObject getLogObject() {
        return logObject;
    }

    public MlUserPreferences getUserPrefs() {
        return userPrefs;
    }

    @Override
    public String toString() {
        return "a=" + getActor() + ", l=" + logRecord + ", lo=" + logObject;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.userPrefs);
        hash = 41 * hash + Objects.hashCode(this.logRecord);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NewsCandidate other = (NewsCandidate) obj;
        if (!Objects.equals(this.userPrefs, other.userPrefs)) {
            return false;
        }
        if (!Objects.equals(this.logRecord, other.logRecord)) {
            return false;
        }
        return true;
    }
    
    

}
