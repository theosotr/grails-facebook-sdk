package grails.plugin.facebooksdk;

import com.restfb.BinaryAttachment;
import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.batch.BatchRequest;
import com.restfb.batch.BatchResponse;
import com.restfb.exception.FacebookException;
import com.restfb.json.JsonObject;
import com.restfb.scope.ScopeBuilder;

import java.util.*;

import static com.restfb.util.ObjectUtil.verifyParameterPresence;
import static java.util.Arrays.asList;

/**
 * Extensions for {@link com.restfb.FacebookClient}.
 *
 */
public class FacebookExtensions {

    private static final int BATCH_SIZE = 20;

    /**
     * Fetches a single <a href="http://developers.facebook.com/docs/reference/api/">Graph API object</a>, mapping the
     * result to an instance of {@code JsonObject}.
     *
     * @param object     ID of the object to fetch, e.g. {@code "me"}.
     * @return An instance of type {@code objectType} which contains the requested object's data.
     * @throws FacebookException If an error occurs while performing the API call.
     * @deprecated JsonObject has been completely rewritten and is very Groovy-unfriendly
     */
    public static JsonObject fetchObject(FacebookClient client, String object) {
        return client.fetchObject(object, JsonObject.class);
    }

    /**
     * Fetches a single <a href="http://developers.facebook.com/docs/reference/api/">Graph API object</a>, mapping the
     * result to an instance of {@code JsonObject}.
     *
     * @param object     ID of the object to fetch, e.g. {@code "me"}.
     * @param parameters URL parameters to include in the API call (optional).
     * @return An instance of type {@code objectType} which contains the requested object's data.
     * @throws FacebookException If an error occurs while performing the API call.
     * @deprecated JsonObject has been completely rewritten and is very Groovy-unfriendly
     */
    @Deprecated
    public static JsonObject fetchObject(FacebookClient client, String object, Map<String, Object> parameters) {
        return client.fetchObject(object, JsonObject.class, buildVariableArgs(parameters));
    }

    /**
     * Fetches a single <a href="http://developers.facebook.com/docs/reference/api/">Graph API object</a>, mapping the
     * result to an instance of {@code objectType}.
     *
     * @param <T>        Java type to map to.
     * @param object     ID of the object to fetch, e.g. {@code "me"}.
     * @param objectType Object type token.
     * @return An instance of type {@code objectType} which contains the requested object's data.
     * @throws FacebookException If an error occurs while performing the API call.
     */
    public static <T> T fetchObject(FacebookClient client, String object, Class<T> objectType) {
        return client.fetchObject(object, objectType);
    }

    /**
     * Fetches a single <a href="http://developers.facebook.com/docs/reference/api/">Graph API object</a>, mapping the
     * result to an instance of {@code objectType}.
     *
     * @param <T>        Java type to map to.
     * @param object     ID of the object to fetch, e.g. {@code "me"}.
     * @param objectType Object type token.
     * @param parameters URL parameters to include in the API call (optional).
     * @return An instance of type {@code objectType} which contains the requested object's data.
     * @throws FacebookException If an error occurs while performing the API call.
     */
    public static <T> T fetchObject(FacebookClient client, String object, Class<T> objectType, Map<String, Object> parameters) {
        return client.fetchObject(object, objectType, buildVariableArgs(parameters));
    }

    /**
     * Fetches multiple <a href="http://developers.facebook.com/docs/reference/api/">Graph API objects</a> in a single
     * call, mapping the results to an instance of {@code objectType}.
     * <p>
     * You'll need to write your own container type ({@code objectType}) to hold the results. See
     * <a href="http://restfb.com">http://restfb.com</a> for an example of how to do this.
     *
     * @param <T>        Java type to map to.
     * @param ids        IDs of the objects to fetch, e.g. {@code "me", "arjun"}.
     * @param objectType Object type token.
     * @param parameters URL parameters to include in the API call (optional).
     * @return An instance of type {@code objectType} which contains the requested objects' data.
     * @throws FacebookException If an error occurs while performing the API call.
     * @deprecated use {@link #fetchAll(FacebookClient, List, Class, Map)} instead
     */
    @Deprecated
    public static <T> T fetchObjects(FacebookClient client, List<String> ids, Class<T> objectType, Map<String, Object> parameters) {
        return client.fetchObjects(ids, objectType, buildVariableArgs(parameters));
    }


    /**
     * Fetches multiple <a href="http://developers.facebook.com/docs/reference/api/">Graph API objects</a> in a single
     * call, mapping the results to an instance of {@code objectType}.
     * <p>
     * You'll need to write your own container type ({@code objectType}) to hold the results. See
     * <a href="http://restfb.com">http://restfb.com</a> for an example of how to do this.
     *
     * @param <T>        Java type to map to.
     * @param ids        IDs of the objects to fetch, e.g. {@code "me", "arjun"}.
     * @param objectType Object type token.
     * @return An instance of type {@code objectType} which contains the requested objects' data.
     * @throws FacebookException If an error occurs while performing the API call.
     * @deprecated use {@link #fetchAll(FacebookClient, List, Class)} instead
     */
    @Deprecated
    public static <T> T fetchObjects(FacebookClient client, List<String> ids, Class<T> objectType) {
        return client.fetchObjects(ids, objectType);
    }

    /**
     * Fetches multiple <a href="http://developers.facebook.com/docs/reference/api/">Graph API objects</a> in a single
     * call, mapping the results to an instance of {@code objectType}.
     * <p>
     * You'll need to write your own container type ({@code objectType}) to hold the results. See
     * <a href="http://restfb.com">http://restfb.com</a> for an example of how to do this.
     *
     * @param <T>        Java type to map to.
     * @param ids        IDs of the objects to fetch, e.g. {@code "me", "arjun"}.
     * @param objectType Object type token.
     * @param parameters URL parameters to include in the API call (optional).
     * @return An instance of type {@code objectType} which contains the requested objects' data.
     * @throws FacebookException If an error occurs while performing the API call.
     */
    public static <T> Map<String, T> fetchAll(FacebookClient client, List<String> ids, Class<T> objectType, Map<String, Object> parameters) {
        Map<String, T> objects = new LinkedHashMap<>();

        for(List<String> part : collate(ids, BATCH_SIZE)) {
            JsonObject object = client.fetchObjects(part, JsonObject.class, buildVariableArgs(parameters));

            if (object == null) {
                return Collections.emptyMap();
            }

            for (JsonObject.Member member : object) {
                objects.put(member.getName(), client.getJsonMapper().toJavaObject(member.getValue().toString(), objectType));
            }
        }

        return objects;
    }

    /**
     * @see com.restfb.FacebookClient#executeBatch(com.restfb.batch.BatchRequest[])
     */
    public static List<BatchResponse> safeBatch(FacebookClient client, BatchRequest... batchRequests) {
        return safeBatch(client, asList(batchRequests), Collections.emptyList());
    }

    /**
     * @see com.restfb.FacebookClient#executeBatch(java.util.List)
     */
    public static List<BatchResponse> safeBatch(FacebookClient client, List<BatchRequest> batchRequests) {
        return safeBatch(client, batchRequests, Collections.emptyList());
    }

    /**
     * @see com.restfb.FacebookClient#executeBatch(java.util.List, java.util.List)
     */
    public static List<BatchResponse> safeBatch(FacebookClient client, List<BatchRequest> batchRequests, List<BinaryAttachment> binaryAttachments) {
        verifyParameterPresence("binaryAttachments", binaryAttachments);

        if (batchRequests == null || batchRequests.isEmpty()) {
            throw new IllegalArgumentException("You must specify at least one batch request.");
        }

        List<BatchResponse> responses = new ArrayList<>();

        for (List<BatchRequest> requests : collate(batchRequests, BATCH_SIZE)) {
            responses.addAll(client.executeBatch(requests, binaryAttachments));
        }

        return responses;
    }

    /**
     * Fetches multiple <a href="http://developers.facebook.com/docs/reference/api/">Graph API objects</a> in a single
     * call, mapping the results to an instance of {@code objectType}.
     * <p>
     * You'll need to write your own container type ({@code objectType}) to hold the results. See
     * <a href="http://restfb.com">http://restfb.com</a> for an example of how to do this.
     *
     * @param <T>        Java type to map to.
     * @param ids        IDs of the objects to fetch, e.g. {@code "me", "arjun"}.
     * @param objectType Object type token.
     * @return An instance of type {@code objectType} which contains the requested objects' data.
     * @throws FacebookException If an error occurs while performing the API call.
     */
    public static <T> Map<String, T> fetchAll(FacebookClient client, List<String> ids, Class<T> objectType) {
        return fetchAll(client, ids, objectType, Collections.emptyMap());
    }

    /**
     * Fetches a Graph API {@code Connection} type, mapping the result to an instance of {@code connectionType}.
     *
     * @param <T>            Java type to map to.
     * @param connection     The name of the connection, e.g. {@code "me/feed"}.
     * @param connectionType Connection type token.
     * @param parameters     URL parameters to include in the API call (optional).
     * @return An instance of type {@code connectionType} which contains the requested Connection's data.
     * @throws FacebookException If an error occurs while performing the API call.
     */
    public static <T> Connection<T> fetchConnection(FacebookClient client, String connection, Class<T> connectionType, Map<String, Object> parameters) {
        return client.fetchConnection(connection, connectionType, buildVariableArgs(parameters));
    }

    /**
     * Fetches a Graph API {@code Connection} type, mapping the result to an instance of {@code connectionType}.
     *
     * @param <T>            Java type to map to.
     * @param connection     The name of the connection, e.g. {@code "me/feed"}.
     * @param connectionType Connection type token.
     * @return An instance of type {@code connectionType} which contains the requested Connection's data.
     * @throws FacebookException If an error occurs while performing the API call.
     */
    public static <T> Connection<T> fetchConnection(FacebookClient client, String connection, Class<T> connectionType) {
        return client.fetchConnection(connection, connectionType);
    }

    /**
     * Performs a <a href="http://developers.facebook.com/docs/api#publishing">Graph API publish</a> operation on the
     * given {@code connection}, mapping the result to an instance of {@code objectType}.
     *
     * @param <T>        Java type to map to.
     * @param connection The Connection to publish to.
     * @param objectType Object type token.
     * @param parameters URL parameters to include in the API call.
     * @return An instance of type {@code objectType} which contains the Facebook response to your publish request.
     * @throws FacebookException If an error occurs while performing the API call.
     */
    public static <T> T publish(FacebookClient client, String connection, Class<T> objectType, Map<String, Object> parameters) {
        return client.publish(connection, objectType, buildVariableArgs(parameters));
    }

    /**
     * Performs a <a href="http://developers.facebook.com/docs/api#publishing">Graph API publish</a> operation on the
     * given {@code connection}, mapping the result to an instance of {@code objectType}.
     *
     * @param <T>        Java type to map to.
     * @param connection The Connection to publish to.
     * @param objectType Object type token.
     * @return An instance of type {@code objectType} which contains the Facebook response to your publish request.
     * @throws FacebookException If an error occurs while performing the API call.
     */
    public static <T> T publish(FacebookClient client, String connection, Class<T> objectType) {
        return client.publish(connection, objectType);
    }

    /**
     * Performs a <a href="http://developers.facebook.com/docs/api#publishing">Graph API publish</a> operation on the
     * given {@code connection} and includes some files - photos, for example - in the publish request, and mapping the
     * result to an instance of {@code objectType}.
     *
     * @param <T>               Java type to map to.
     * @param connection        The Connection to publish to.
     * @param objectType        Object type token.
     * @param binaryAttachments The files to include in the publish request.
     * @param parameters        URL parameters to include in the API call.
     * @return An instance of type {@code objectType} which contains the Facebook response to your publish request.
     * @throws FacebookException If an error occurs while performing the API call.
     */
    public static <T> T publish(FacebookClient client, String connection, Class<T> objectType, List<BinaryAttachment> binaryAttachments, Map<String, Object> parameters) {
        return client.publish(connection, objectType, binaryAttachments, buildVariableArgs(parameters));
    }

    /**
     * Performs a <a href="http://developers.facebook.com/docs/api#publishing">Graph API publish</a> operation on the
     * given {@code connection} and includes some files - photos, for example - in the publish request, and mapping the
     * result to an instance of {@code objectType}.
     *
     * @param <T>               Java type to map to.
     * @param connection        The Connection to publish to.
     * @param objectType        Object type token.
     * @param binaryAttachments The files to include in the publish request.
     * @return An instance of type {@code objectType} which contains the Facebook response to your publish request.
     * @throws FacebookException If an error occurs while performing the API call.
     */
    public static <T> T publish(FacebookClient client, String connection, Class<T> objectType, List<BinaryAttachment> binaryAttachments) {
        return client.publish(connection, objectType, binaryAttachments);
    }

    /**
     * Performs a <a href="http://developers.facebook.com/docs/api#publishing">Graph API publish</a> operation on the
     * given {@code connection} and includes a file - a photo, for example - in the publish request, and mapping the
     * result to an instance of {@code objectType}.
     *
     * @param <T>              Java type to map to.
     * @param connection       The Connection to publish to.
     * @param objectType       Object type token.
     * @param binaryAttachment The file to include in the publish request.
     * @param parameters       URL parameters to include in the API call.
     * @return An instance of type {@code objectType} which contains the Facebook response to your publish request.
     * @throws FacebookException If an error occurs while performing the API call.
     */
    public static <T> T publish(FacebookClient client, String connection, Class<T> objectType, BinaryAttachment binaryAttachment, Map<String, Object> parameters) {
        return client.publish(connection, objectType, binaryAttachment, buildVariableArgs(parameters));
    }

    /**
     * Performs a <a href="http://developers.facebook.com/docs/api#publishing">Graph API publish</a> operation on the
     * given {@code connection} and includes a file - a photo, for example - in the publish request, and mapping the
     * result to an instance of {@code objectType}.
     *
     * @param <T>              Java type to map to.
     * @param connection       The Connection to publish to.
     * @param objectType       Object type token.
     * @param binaryAttachment The file to include in the publish request.
     * @return An instance of type {@code objectType} which contains the Facebook response to your publish request.
     * @throws FacebookException If an error occurs while performing the API call.
     */
    public static <T> T publish(FacebookClient client, String connection, Class<T> objectType, BinaryAttachment binaryAttachment) {
        return client.publish(connection, objectType, binaryAttachment);
    }

    /**
     * Performs a <a href="http://developers.facebook.com/docs/api#deleting">Graph API delete</a> operation on the given
     * {@code object}.
     *
     * @param object     The ID of the object to delete.
     * @param parameters URL parameters to include in the API call.
     * @return {@code true} if Facebook indicated that the object was successfully deleted, {@code false} otherwise.
     * @throws FacebookException If an error occurred while attempting to delete the object.
     */
    public static boolean deleteObject(FacebookClient client, String object, Map<String, Object> parameters) {
        return client.deleteObject(object, buildVariableArgs(parameters));
    }

    /**
     * Performs a <a href="http://developers.facebook.com/docs/api#deleting">Graph API delete</a> operation on the given
     * {@code object}.
     *
     * @param object     The ID of the object to delete.
     * @return {@code true} if Facebook indicated that the object was successfully deleted, {@code false} otherwise.
     * @throws FacebookException If an error occurred while attempting to delete the object.
     */
    public static boolean deleteObject(FacebookClient client, String object) {
        return client.deleteObject(object);
    }

    /**
     * generates the login dialog url
     *
     * @param appId                The ID of your app, found in your app's dashboard.
     * @param redirectUri          The URL that you want to redirect the person logging in back to. This URL will capture the response from
     *                             the Login Dialog. If you are using this in a webview within a desktop app, this must be set to
     *                             <code>https://www.facebook.com/connect/login_success.html</code>.
     * @param scope                List of Permissions to request from the person using your app.
     * @param additionalParameters List of additional parameters
     * @return the login dialog url
     * @since 1.9.0
     */
    public static String getLoginDialogUrl(FacebookClient client, String appId, String redirectUri, ScopeBuilder scope, Map<String, Object> additionalParameters) {
        return client.getLoginDialogUrl(appId, redirectUri, scope, buildVariableArgs(additionalParameters));
    }

    /**
     * generates the login dialog url
     *
     * @param appId                The ID of your app, found in your app's dashboard.
     * @param redirectUri          The URL that you want to redirect the person logging in back to. This URL will capture the response from
     *                             the Login Dialog. If you are using this in a webview within a desktop app, this must be set to
     *                             <code>https://www.facebook.com/connect/login_success.html</code>.
     * @param scope                List of Permissions to request from the person using your app.
     * @return the login dialog url
     * @since 1.9.0
     */
    public static String getLoginDialogUrl(FacebookClient client, String appId, String redirectUri, ScopeBuilder scope) {
        return client.getLoginDialogUrl(appId, redirectUri, scope);
    }

    /**
     * @deprecated use {@link #fetchConnection(FacebookClient, String, Class, Map)} instead.
     */
    @Deprecated
    public static <T> Connection<T> makeRequest(FacebookClient client, String endPoint, Class<T> connectionType, Map<String, Object> parameters) {
        return fetchConnection(client, endPoint, connectionType, parameters);
    }

    /**
     * @deprecated use {@link FacebookClient#fetchConnection(String, Class, Parameter...)} instead.
     */
    @Deprecated
    public static <T> Connection<T> makeRequest(FacebookClient client, String endPoint, Class<T> connectionType) {
        return fetchConnection(client, endPoint, connectionType, Collections.emptyMap());
    }

    /**
     * @deprecated use {@link #publish(FacebookClient, String, Class, Map)} instead.
     */
    @Deprecated
    public static <T> T makePostRequest(FacebookClient client, String connection, Class<T> objectType, Map<String, Object> parameters) {
        return publish(client, connection, objectType, parameters);
    }

    /**
     * @deprecated use {@link FacebookClient#publish(String, Class, Parameter...)} instead.
     */
    @Deprecated
    public static <T> T makePostRequest(FacebookClient client, String connection, Class<T> objectType) {
        return client.publish(connection, objectType);
    }

    /**
     * @deprecated use {@link #deleteObject(FacebookClient, String, Map)} instead.
     */
    @Deprecated
    public static boolean makeDeleteRequest(FacebookClient client, String endPoint, Map<String, Object> parameters) {
        return deleteObject(client, endPoint, parameters);
    }

    /**
     * @deprecated use {@link FacebookClient#deleteObject(String, Parameter...)} instead.
     */
    @Deprecated
    public static boolean makeDeleteRequest(FacebookClient client, String endPoint) {
        return client.deleteObject(endPoint);
    }

    private static Parameter[] buildVariableArgs(Map<String, Object> parameters) {
        return parameters
                .entrySet()
                .stream()
                .map(e -> Parameter.with(e.getKey(), e.getValue()))
                .toArray(Parameter[]::new);
    }

    private static <T> List<List<T>> collate(List<T> selfList, int step) {
        if (selfList == null) {
            return Collections.singletonList(Collections.emptyList());
        }

        int size = selfList.size();

        if (size <= step) {
            return Collections.singletonList(selfList);
        }

        List<List<T>> answer = new ArrayList<>();
        if (step <= 0) throw new IllegalArgumentException("step must be greater than zero");
        for (int pos = 0; pos < size && pos > -1; pos += step) {
            List<T> element = new ArrayList<>() ;
            for (int offs = pos; offs < pos + size && offs < size; offs++) {
                element.add(selfList.get(offs));
            }
            answer.add(element) ;
        }
        return answer ;
    }

}
