/*
 * Copyright (c) 2010-2015 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.web.component.progress;

import com.evolveum.midpoint.schema.statistics.OperationalInformation;
import com.evolveum.midpoint.schema.statistics.ProvisioningOperation;
import com.evolveum.midpoint.schema.statistics.ProvisioningStatisticsData;
import com.evolveum.midpoint.schema.statistics.ProvisioningStatisticsKey;
import com.evolveum.midpoint.schema.statistics.ProvisioningStatusType;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Pavol Mederly
 */
public class ProvisioningStatisticsLineDto {

    public static final String F_RESOURCE = "resource";
    public static final String F_OBJECT_CLASS = "objectClass";
        public static final String F_GET_SUCCESS = "searchSuccess";
        public static final String F_GET_FAILURE = "searchFailure";
    public static final String F_SEARCH_SUCCESS = "searchSuccess";
    public static final String F_SEARCH_FAILURE = "searchFailure";

    public static final String F_CREATE_SUCCESS = "createSuccess";
    public static final String F_CREATE_FAILURE = "createFailure";
    public static final String F_UPDATE_SUCCESS = "updateSuccess";
    public static final String F_UPDATE_FAILURE = "updateFailure";
    public static final String F_DELETE_SUCCESS = "deleteSuccess";
    public static final String F_DELETE_FAILURE = "deleteFailure";

    public static final String F_SYNC_SUCCESS = "syncSuccess";
    public static final String F_SYNC_FAILURE = "syncFailure";

    public static final String F_SCRIPT_SUCCESS = "scriptSuccess";
    public static final String F_SCRIPT_FAILURE = "scriptFailure";
    public static final String F_OTHER_SUCCESS = "otherSuccess";
    public static final String F_OTHER_FAILURE = "otherFailure";

    public static final String F_AVERAGE_TIME = "averageTime";
    public static final String F_MIN_TIME = "minTime";
    public static final String F_MAX_TIME = "maxTime";
    public static final String F_TOTAL_TIME = "totalTime";

    private String resource;
    private String objectClass;
    private int getSuccess;
    private int getFailure;
    private int searchSuccess;
    private int searchFailure;
    private int createSuccess;
    private int createFailure;
    private int updateSuccess;
    private int updateFailure;
    private int deleteSuccess;
    private int deleteFailure;
    private int syncSuccess;
    private int syncFailure;
    private int scriptSuccess;
    private int scriptFailure;
    private int otherSuccess;
    private int otherFailure;

    private Integer minTime;
    private Integer maxTime;
    private int totalTime;

    public ProvisioningStatisticsLineDto(String resource, String objectClass) {
        this.resource = resource;
        this.objectClass = objectClass;
    }

    public String getResource() {
        return resource;
    }

    public String getObjectClass() {
        return objectClass;
    }

    public int getGetSuccess() {
        return getSuccess;
    }

    public int getGetFailure() {
        return getFailure;
    }

    public int getSearchSuccess() {
        return searchSuccess;
    }

    public int getSearchFailure() {
        return searchFailure;
    }

    public int getCreateSuccess() {
        return createSuccess;
    }

    public int getCreateFailure() {
        return createFailure;
    }

    public int getUpdateSuccess() {
        return updateSuccess;
    }

    public int getUpdateFailure() {
        return updateFailure;
    }

    public int getDeleteSuccess() {
        return deleteSuccess;
    }

    public int getDeleteFailure() {
        return deleteFailure;
    }

    public int getSyncSuccess() {
        return syncSuccess;
    }

    public int getSyncFailure() {
        return syncFailure;
    }

    public int getScriptSuccess() {
        return scriptSuccess;
    }

    public int getScriptFailure() {
        return scriptFailure;
    }

    public int getOtherSuccess() {
        return otherSuccess;
    }

    public int getOtherFailure() {
        return otherFailure;
    }

    public int getAverageTime() {
        int totalCount = getSuccess + getFailure + searchSuccess + searchFailure +
                createSuccess + createFailure + updateSuccess + updateFailure + deleteSuccess + deleteFailure +
                syncSuccess + syncFailure + scriptSuccess + scriptFailure + otherSuccess + otherFailure;
        if (totalCount > 0) {
            return totalTime / totalCount;
        } else {
            return 0;
        }
    }

    public int getMinTime() {
        return minTime != null ? minTime : 0;
    }

    public int getMaxTime() {
        return maxTime != null ? maxTime : 0;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public static List<ProvisioningStatisticsLineDto> extractFromOperationalInformation(OperationalInformation operationalInformation) {
        List<ProvisioningStatisticsLineDto> retval = new ArrayList<>();
        Map<ProvisioningStatisticsKey, ProvisioningStatisticsData> dataMap = operationalInformation.getProvisioningData();
        if (dataMap == null) {
            return retval;
        }
        for (Map.Entry<ProvisioningStatisticsKey, ProvisioningStatisticsData> entry : dataMap.entrySet()) {
            ProvisioningStatisticsKey key = entry.getKey();
            String resource = key.getResourceName();
            String oc = key.getObjectClass().getLocalPart();
            ProvisioningStatisticsLineDto lineDto = findLineDto(retval, resource, oc);
            if (lineDto == null) {
                lineDto = new ProvisioningStatisticsLineDto(resource, oc);
                retval.add(lineDto);
            }
            lineDto.setValue(key.getOperation(), key.getStatusType(), entry.getValue().getCount(),
                    entry.getValue().getMinDuration(), entry.getValue().getMaxDuration(), entry.getValue().getTotalDuration());
        }
        return retval;
    }

    private static ProvisioningStatisticsLineDto findLineDto(List<ProvisioningStatisticsLineDto> list, String resource, String objectClass) {
        for (ProvisioningStatisticsLineDto lineDto : list) {
            if (StringUtils.equals(lineDto.getResource(), resource) && StringUtils.equals(lineDto.getObjectClass(), objectClass)) {
                return lineDto;
            }
        }
        return null;
    }

    private void setValue(ProvisioningOperation operation, ProvisioningStatusType statusType, int count, int min, int max, long totalDuration) {
        switch (operation) {
            case ICF_GET:
                if (statusType == ProvisioningStatusType.SUCCESS) {
                    getSuccess+=count;
                } else {
                    getFailure+=count;
                }
                break;
            case ICF_SEARCH:
                if (statusType == ProvisioningStatusType.SUCCESS) {
                    searchSuccess+=count;
                } else {
                    searchFailure+=count;
                }
                break;
            case ICF_CREATE:
                if (statusType == ProvisioningStatusType.SUCCESS) {
                    createSuccess+=count;
                } else {
                    createFailure+=count;
                }
                break;
            case ICF_UPDATE:
                if (statusType == ProvisioningStatusType.SUCCESS) {
                    updateSuccess+=count;
                } else {
                    updateFailure+=count;
                }
                break;
            case ICF_DELETE:
                if (statusType == ProvisioningStatusType.SUCCESS) {
                    deleteSuccess+=count;
                } else {
                    deleteFailure+=count;
                }
                break;
            case ICF_SYNC:
                if (statusType == ProvisioningStatusType.SUCCESS) {
                    syncSuccess+=count;
                } else {
                    syncFailure+=count;
                }
                break;
            case ICF_SCRIPT:
                if (statusType == ProvisioningStatusType.SUCCESS) {
                    scriptSuccess+=count;
                } else {
                    scriptFailure+=count;
                }
                break;
            case ICF_GET_LATEST_SYNC_TOKEN:
            case ICF_GET_SCHEMA:
                if (statusType == ProvisioningStatusType.SUCCESS) {
                    otherSuccess+=count;
                } else {
                    otherFailure+=count;
                }
                break;
            default:
                throw new IllegalArgumentException("Illegal operation name: " + operation);
        }
        if (minTime == null || min < minTime) {
            minTime = min;
        }
        if (maxTime == null || max > maxTime) {
            maxTime = max;
        }
        totalTime += totalDuration;
    }
}