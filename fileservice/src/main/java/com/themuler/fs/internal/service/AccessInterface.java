package com.themuler.fs.internal.service;

import com.themuler.fs.api.Feature;

public interface AccessInterface {

  boolean allowAccess(Feature feature);
}
