package com.vtuan.gate;

import java.util.regex.Pattern;

/**
 * Builds a narrow set of trusted VTUAN API paths.
 * Never interpolates an unchecked server identifier into a URL.
 */
public final class KeyApiPath {
  private static final Pattern SERVER_ID=Pattern.compile("[a-z0-9-]{3,32}");

  private KeyApiPath() {}

  public static String normalizeServer(String serverId) {
    return serverId==null ? "" : serverId.trim();
  }

  public static boolean isValidServer(String serverId) {
    String id=normalizeServer(serverId);
    return id.isEmpty() || SERVER_ID.matcher(id).matches();
  }

  public static String endpoint(String serverId, boolean verify) {
    String id=normalizeServer(serverId);
    if(!isValidServer(id)) throw new IllegalArgumentException("Invalid child server identifier");
    return (id.isEmpty() ? "/api/" : "/api/sv/"+id+"/")+(verify ? "verify" : "activate");
  }
}
