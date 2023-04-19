package com.dataportal.datastorage.model.request;

import com.dataportal.datastorage.entity.DatasourceDetails;
import com.dataportal.datastorage.entity.Metadata;
import lombok.Data;

@Data
public class DatasourceUpdate {
    private Metadata metadata;
    private DatasourceDetails datasourceDetails;
}
