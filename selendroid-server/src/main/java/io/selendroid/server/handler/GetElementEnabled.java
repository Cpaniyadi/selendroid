package io.selendroid.server.handler;

import io.selendroid.server.RequestHandler;
import io.selendroid.server.Response;

import org.json.JSONException;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.StaleElementReferenceException;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.AndroidElement;
import io.selendroid.util.SelendroidLogger;
import org.webbitserver.HttpRequest;

public class GetElementEnabled extends RequestHandler {

  public GetElementEnabled(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    SelendroidLogger.log("is element enabled command");
    String id = getElementId();

    AndroidElement element = getElementFromCache(id);
    if (element == null) {
      return new SelendroidResponse(getSessionId(), 10, new SelendroidException("Element with id '" + id
          + "' was not found in cache."));
    }
    try {
      return new SelendroidResponse(getSessionId(), element.isEnabled());
    } catch (StaleElementReferenceException se) {
      return new SelendroidResponse(getSessionId(), 10, se);
    }
  }
}
