package com.fresconews.fresco.framework.injection.helpers;

import com.fresconews.fresco.framework.persistence.managers.UploadManager;

import javax.inject.Inject;

/**
 * Created by wumau on 9/21/2016.
 */
public class InjectionTarget {
    @Inject
    public UploadManager mUploadManager;
}
