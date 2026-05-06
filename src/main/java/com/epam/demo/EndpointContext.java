package com.epam.demo;

/**
 * ThreadLocal carrier for the current HTTP endpoint name.
 * Set by each controller before an AI call so that TraceObservationHandler
 * can label every downstream model invocation with the originating endpoint.
 */
class EndpointContext {

  private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

  static void set(String endpoint) { CURRENT.set(endpoint); }
  static String get() { return CURRENT.get() != null ? CURRENT.get() : "unknown"; }
  static void clear() { CURRENT.remove(); }
}
