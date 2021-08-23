/*
 * Copyright (c) 2021 tecc
 * Lahc is licensed under the MIT licence.
 */

package me.tecc.lahc;

import java.util.HashMap;
import java.util.Map;

/**
 * Pseudo-enumeration class.
 */
public class Status {
    private int code;
    private String message;
    private String description;
    private boolean isPrimitive = false;

    public Status(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    public Status getPrimitive() {
        if (this.isPrimitive) return this;
        return getByCode(this.code);
    }

    @Override
    public String toString() {
        return this.code + " " + this.message;
    }

    private static Map<Integer, Status> byCode = new HashMap<>();

    public static Status getByCode(int code) {
        return byCode.get(code);
    }

    private static Status s(int code, String message, String desc) {
        Status status = new Status(code, message, desc);
        status.isPrimitive = true;
        byCode.put(code, status);
        return status;
    }

    /* PREDEFINED STATUSES */
    public static final Status CONTINUE = s(
            100, "Continue",
            "The client should continue with its request."
    );
    public static final Status SWITCHING_PROTOCOLS = s(
            101, "Switching Protocols",
            "Informs the client that the server will switch to the protocol specified in the Upgrade message header field."
    );
    public static final Status OK = s(
            200, "OK",
            "The request sent by the client was successful."
    );
    public static final Status CREATED = s(
            201, "Created",
            "The request was successful and the resource has been created."
    );
    public static final Status ACCEPTED = s(
            202, "Accepted",
            "The request has been accepted but has not yet finished processing."
    );
    public static final Status NON_AUTHORITATIVE_INFORMATION = s(
            203, "Non-Authoritative Information",
            "The returned meta-information in the entity header is not the definitative set of information, it might be a local copy or contain local alterations."
    );
    public static final Status NO_CONTENT = s(
            204, "No Content",
            "The request was successful but not require the return of an entity body."
    );
    public static final Status RESET_CONTENT = s(
            205, "Reset Content",
            "The request was successful and the user agent should reset the view that sent the request."
    );
    public static final Status PARTIAL_CONTENT = s(
            206, "Partial Content",
            "The partial request was successful."
    );
    public static final Status MULTIPLE_CHOICES = s(
            300, "Multiple Choices",
            "The requested resource has multiple choices, each of which has a different location."
    );
    public static final Status MOVED_PERMANENTLY = s(
            301, "Moved Permanently",
            "The requested resources has moved permanently to a new location."
    );
    public static final Status FOUND = s(
            302, "Found",
            "The requested resource has been found at a different location but the client should use the original URI."
    );
    public static final Status SEE_OTHER = s(
            303, "See Other",
            "The requested resource is located at a different location which should be returned by the location field in the response."
    );
    public static final Status NOT_MODIFIED = s(
            304, "Not Modified",
            "The resource has not been modified since the last request."
    );
    public static final Status USE_PROXY = s(
            305, "Use Proxy",
            "The requested resource can only be accessed through a proxy which should be provided in the location field."
    );
    public static final Status UNUSED = s(
            306, "Unused",
            "This status code is no longer in use but is reserved for future use."
    );
    public static final Status TEMPORARY_REDIRECT = s(
            307, "Temporary Redirect",
            "The requested resource is temporarily moved to the provided location but the client should continue to use this location as the resource may again move."
    );
    public static final Status BAD_REQUEST = s(
            400, "Bad Request",
            "The request could not be understood by the server."
    );
    public static final Status UNAUTHORIZED = s(
            401, "Unauthorized",
            "The request requires authorization."
    );
    public static final Status PAYMENT_REQUIRED = s(
            402, "Payment Required",
            "Reserved for future use."
    );
    public static final Status FORBIDDEN = s(
            403, "Forbidden",
            "Whilst the server did understand the request, the server is refusing to complete it. This is not an authorization problem."
    );
    public static final Status NOT_FOUND = s(
            404, "Not Found",
            "The requested resource was not found."
    );
    public static final Status METHOD_NOT_ALLOWED = s(
            405, "Method Not Allowed",
            "The supplied method was not allowed on the given resource."
    );
    public static final Status NOT_ACCEPTABLE = s(
            406, "Not Acceptable",
            "The resource is not able to return a response that is suitable for the characteristics required by the accept headers of the request."
    );
    public static final Status PROXY_AUTHENTICATION_REQUIRED = s(
            407, "Proxy Authentication Required",
            "The client must authenticate themselves with the proxy."
    );
    public static final Status REQUEST_TIMEOUT = s(
            408, "Request Timeout",
            "The client did not supply a request in the period required by the server."
    );
    public static final Status CONFLICT = s(
            409, "Conflict",
            "The request could not be completed as the resource is in a conflicted state."
    );
    public static final Status GONE = s(
            410, "Gone",
            "The requested resource is no longer available on the server and no redirect address is available."
    );
    public static final Status LENGTH_REQUIRED = s(
            411, "Length Required",
            "The server will not accept the request without a Content-Length field."
    );
    public static final Status PRECONDITION_FAILED = s(
            412, "Precondition Failed",
            "The supplied precondition evaluated to false on the server."
    );
    public static final Status REQUEST_ENTITY_TOO_LARGE = s(
            413, "Request Entity Too Large",
            "The request was unsuccessful because the request entity was larger than the server would allow"
    );
    public static final Status REQUESTED_URI_TOO_LONG = s(
            414, "Request URI Too Long",
            "The request was unsuccessful because the requested URI is longer than the server is willing to process (that's what she said)."
    );
    public static final Status UNSUPPORTED_MEDIA_TYPE = s(
            415, "Unsupported Media Type",
            "The request was unsuccessful because the request was for an unsupported format."
    );
    public static final Status REQUEST_RANGE_NOT_SATISFIABLE = s(
            416, "Request Range Not Satisfiable",
            "The range of the resource does not overlap with the values specified in the requests Range header field and not alternative If-Range field was supplied."
    );
    public static final Status EXPECTATION_FAILED = s(
            417, "Expectation Failed",
            "The expectation supplied in the Expectation header field could not be met by the server."
    );
    public static final Status IM_A_TEAPOT = s(
            418, "I'm a teapot",
            "I'm a teapot"
    );
    public static final Status INTERNAL_SERVER_ERROR = s(
            500, "Internal Server Error",
            "The request was unsuccessful because the server encountered an unexpected error."
    );
    public static final Status NOT_IMPLEMENTED = s(
            501, "Not Implemented",
            "The server does not support the request."
    );
    public static final Status BAD_GATEWAY = s(
            502, "Bad Gateway",
            "The server, whilst acting as a proxy, received an invalid response from the server that was fulfilling the request."
    );
    public static final Status SERVICE_UNAVAILABLE = s(
            503, "Service Unavailable",
            "The request was unsuccessful as the server is unavailable."
    );
    public static final Status GATEWAY_TIMEOUT = s(
            504, "Gateway Timeout",
            "The server, whilst acting as a proxy, did not receive a response from the upstream server in an acceptable time."
    );
    public static final Status HTTP_VERSION_NOT_SUPPORTED = s(
            505, "HTTP Version Not Supported",
            "The server does not supported the HTTP protocol version specified in the request"
    );
    public static final Status UNKNOWN = s(
            -1, "Unknown HTTP Status Code",
            "Unknown or unsupported HTTP status code"
    );

    public boolean isSuccessful() {
        return this.code >= 100 && this.code < 400;
    }
}