/*
 * Copyright 2011 Selenium committers
 * 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.selendroid.android;

import java.util.ArrayList;
import java.util.List;

import io.selendroid.ServerInstrumentation;
import io.selendroid.android.internal.Point;
import io.selendroid.server.model.SelendroidDriver;
import io.selendroid.server.model.TouchScreen;
import io.selendroid.server.model.interactions.Coordinates;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.ScrollView;

/**
 * Implements touch capabilities of a device.
 * 
 */
public class AndroidTouchScreen implements TouchScreen {

  private final SelendroidDriver driver;

  public AndroidTouchScreen(SelendroidDriver driver) {
    this.driver = driver;
  }

  public void singleTap(Coordinates where) {
    Point toTap = where.getLocationOnScreen();
    List<MotionEvent> motionEvents = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, toTap));
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_UP, toTap));
    sendMotionEvents(motionEvents);
  }

  public void down(int x, int y) {
    List<MotionEvent> event = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    Point coords = new Point(x, y);
    event.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, coords));
    sendMotionEvents(event);
  }

  public void up(int x, int y) {
    List<MotionEvent> event = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    Point coords = new Point(x, y);
    event.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_UP, coords));
    sendMotionEvents(event);
  }

  public void move(int x, int y) {
    List<MotionEvent> event = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    Point coords = new Point(x, y);
    event.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_MOVE, coords));
    sendMotionEvents(event);
  }

  public void scroll(Coordinates where, int xOffset, int yOffset) {
    long downTime = SystemClock.uptimeMillis();
    List<MotionEvent> motionEvents = new ArrayList<MotionEvent>();
    Point origin = where.getLocationOnScreen();
    Point destination = new Point(origin.x + xOffset, origin.y + yOffset);
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, origin));

    Scroll scroll = new Scroll(origin, destination, downTime);
    // Initial acceleration from origin to reference point
    motionEvents.addAll(getMoveEvents(downTime, downTime, origin, scroll.getDecelerationPoint(),
        scroll.INITIAL_STEPS, scroll.TIME_BETWEEN_EVENTS));
    // Deceleration phase from reference point to destination
    motionEvents.addAll(getMoveEvents(downTime, scroll.getEventTimeForReferencePoint(),
        scroll.getDecelerationPoint(), destination, scroll.DECELERATION_STEPS,
        scroll.TIME_BETWEEN_EVENTS));

    motionEvents.add(getMotionEvent(downTime,
        (downTime + scroll.getEventTimeForDestinationPoint()), MotionEvent.ACTION_UP, destination));
    sendMotionEvents(motionEvents);
  }

  public void doubleTap(Coordinates where) {
    Point toDoubleTap = where.getLocationOnScreen();
    List<MotionEvent> motionEvents = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, toDoubleTap));
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_UP, toDoubleTap));
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, toDoubleTap));
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_UP, toDoubleTap));
    sendMotionEvents(motionEvents);
  }

  public void longPress(Coordinates where) {
    long downTime = SystemClock.uptimeMillis();
    List<MotionEvent> motionEvents = new ArrayList<MotionEvent>();
    Point point = where.getLocationOnScreen();
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, point));
    motionEvents.add(getMotionEvent(downTime, (downTime + 3000), MotionEvent.ACTION_UP, point));
    sendMotionEvents(motionEvents);
  }

  public void scroll(final int xOffset, final int yOffset) {
    List<View> scrollableContainer =
        ViewHierarchyAnalyzer.getDefaultInstance().findScrollableContainer();
    ServerInstrumentation instrumentation = ServerInstrumentation.getInstance();
    if (scrollableContainer == null) {
      // nothing to do
      return;
    }
    for (View view : scrollableContainer) {
      if (view instanceof AbsListView) {
        final AbsListView absListView = (AbsListView) view;
        instrumentation.runOnUiThread(new Runnable() {
          public void run() {
            absListView.scrollBy(xOffset, yOffset);
          }
        });
      } else if (view instanceof ScrollView) {
        final ScrollView scrollView = (ScrollView) view;
        instrumentation.runOnUiThread(new Runnable() {
          public void run() {
            scrollView.scrollBy(xOffset, yOffset);
          }
        });
      } else if (view instanceof WebView) {
        final WebView webView = (WebView) view;
        instrumentation.runOnUiThread(new Runnable() {
          public void run() {
            webView.scrollBy(xOffset, yOffset);
          }
        });
      }
    }
  }

  public void flick(final int speedX, final int speedY) {
    List<View> scrollableContainer =
        ViewHierarchyAnalyzer.getDefaultInstance().findScrollableContainer();
    ServerInstrumentation instrumentation = ServerInstrumentation.getInstance();
    if (scrollableContainer == null) {
      // nothing to do
      return;
    }
    for (View view : scrollableContainer) {
      if (view instanceof AbsListView) {
        // ignore
      } else if (view instanceof ScrollView) {
        final ScrollView scrollView = (ScrollView) view;
        instrumentation.runOnUiThread(new Runnable() {
          public void run() {
            scrollView.fling(speedY);
          }
        });
      } else if (view instanceof WebView) {
        final WebView webView = (WebView) view;
        instrumentation.runOnUiThread(new Runnable() {
          public void run() {
            webView.flingScroll(speedX, speedY);
          }
        });
      }
    }
  }

  public void flick(Coordinates where, int xOffset, int yOffset, int speed) {
    long downTime = SystemClock.uptimeMillis();
    List<MotionEvent> motionEvents = new ArrayList<MotionEvent>();
    Point origin = where.getLocationOnScreen();
    Point destination = new Point(origin.x + xOffset, origin.y + yOffset);
    Flick flick = new Flick(speed);
    motionEvents.add(getMotionEvent(downTime, downTime, MotionEvent.ACTION_DOWN, origin));
    motionEvents.addAll(getMoveEvents(downTime, downTime, origin, destination, flick.STEPS,
        flick.getTimeBetweenEvents()));
    motionEvents.add(getMotionEvent(downTime, flick.getTimeForDestinationPoint(downTime),
        MotionEvent.ACTION_UP, destination));
    sendMotionEvents(motionEvents);
  }

  private MotionEvent getMotionEvent(long start, long eventTime, int action, Point coords) {
    return MotionEvent.obtain(start, eventTime, action, coords.x, coords.y, 0);
  }

  private List<MotionEvent> getMoveEvents(long downTime, long startingEVentTime, Point origin,
      Point destination, int steps, long timeBetweenEvents) {
    List<MotionEvent> move = new ArrayList<MotionEvent>();
    MotionEvent event = null;

    float xStep = (destination.x - origin.x) / steps;
    float yStep = (destination.y - origin.y) / steps;
    float x = origin.x;
    float y = origin.y;
    long eventTime = startingEVentTime;

    for (int i = 0; i < steps - 1; i++) {
      x += xStep;
      y += yStep;
      eventTime += timeBetweenEvents;
      event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x, y, 0);
      move.add(event);
    }

    eventTime += timeBetweenEvents;
    move.add(getMotionEvent(downTime, eventTime, MotionEvent.ACTION_MOVE, destination));
    return move;
  }

  private void sendMotionEvents(final List<MotionEvent> eventsToSend) {
    ServerInstrumentation instr = ServerInstrumentation.getInstance();
    try {
      instr.waitForIdleSync();
      for (MotionEvent event : eventsToSend) {
        instr.sendPointerSync(event);
      }
    } catch (SecurityException ignored) {
      ignored.printStackTrace();
    }
  }

  final class Scroll {

    private Point origin;
    private Point destination;
    private long downTime;
    // A regular scroll usually has 15 gestures, where the last 5 are used for deceleration
    final static int INITIAL_STEPS = 10;
    final static int DECELERATION_STEPS = 5;
    final int TOTAL_STEPS = INITIAL_STEPS + DECELERATION_STEPS;
    // Time in milliseconds to provide a speed similar to scroll
    final long TIME_BETWEEN_EVENTS = 50;

    public Scroll(Point origin, Point destination, long downTime) {
      this.origin = origin;
      this.destination = destination;
      this.downTime = downTime;
    }

    // This method is used to calculate the point where the deceleration will start at 20% of the
    // distance to the destination point
    private Point getDecelerationPoint() {
      int deltaX = (destination.x - origin.x);
      int deltaY = (destination.y - origin.y);
      // Coordinates of reference point where deceleration should start for scroll gesture, on the
      // last 20% of the total distance to scroll
      int xRef = (int) (deltaX * 0.8);
      int yRef = (int) (deltaY * 0.8);
      return new Point(origin.x + xRef, origin.y + yRef);
    }

    private long getEventTimeForReferencePoint() {
      return (downTime + INITIAL_STEPS * TIME_BETWEEN_EVENTS);
    }

    private long getEventTimeForDestinationPoint() {
      return (downTime + TOTAL_STEPS * TIME_BETWEEN_EVENTS);
    }
  }

  final class Flick {

    private final int SPEED_NORMAL = 0;
    private final int SPEED_FAST = 1;
    // A regular scroll usually has 4 gestures
    private final static int STEPS = 4;
    private int speed;

    public Flick(int speed) {
      this.speed = speed;
    }

    private long getTimeBetweenEvents() {
      if (speed == SPEED_NORMAL) {
        return 25; // Time in milliseconds to provide a speed similar to normal flick
      } else if (speed == SPEED_FAST) {
        return 9; // Time in milliseconds to provide a speed similar to fast flick
      }
      return 0;
    }

    private long getTimeForDestinationPoint(long downTime) {
      return (downTime + STEPS * getTimeBetweenEvents());
    }
  }
}
