/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.sriramramani.droid.inspector.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

/**
 * <p>This class is acts as a server for communicating with Eclipse/HTML scripts.
 * Add the jar as a Referenced Library with your application. Make sure
 * to export it with the application. In principle, this works the same way
 * as HierarchyViewer's ViewServer code.</p>
 *
 * <p>To use this, your application must require the INTERNET permission.</p>
 *
 * <p>The recommended way to use this API is to register activities when
 * they are created, and to unregister them when they get destroyed:</p>
 *
 * <pre>
 * public class MyActivity extends Activity {
 *     public void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         // Set content view, etc.
 *         ViewServer.get(this).addWindow(this);
 *     }
 *
 *     public void onDestroy() {
 *         super.onDestroy();
 *         ViewServer.get(this).removeWindow(this);
 *     }
 *
 *     public void onResume() {
 *         super.onResume();
 *         ViewServer.get(this).setFocusedWindow(this);
 *     }
 * }
 * </pre>
 */
public class ViewServer implements Runnable {
    /**
     * The default port used to start view servers.
     */
    private static final int VIEW_SERVER_DEFAULT_PORT = 4545;
    private static final int VIEW_SERVER_MAX_CONNECTIONS = 10;
    private static final String BUILD_TYPE_USER = "user";

    // Debug facility
    private static final String LOG_TAG = "DroidInspector";

    // Prints the hierarchy
    private static final String COMMAND_PRINT_HIERARCHY = "print";

    private ServerSocket mServer;
    private final int mPort;

    private Thread mThread;
    private ExecutorService mThreadPool;

    private final List<WindowListener> mListeners =
        new CopyOnWriteArrayList<ViewServer.WindowListener>();

    private final HashMap<View, String> mWindows = new HashMap<View, String>();
    private final ReentrantReadWriteLock mWindowsLock = new ReentrantReadWriteLock();

    private View mFocusedWindow;
    private final ReentrantReadWriteLock mFocusLock = new ReentrantReadWriteLock();

    private static ViewServer sServer;

    /**
     * Returns a unique instance of the ViewServer. This method should only be
     * called from the main thread of your application. The server will have
     * the same lifetime as your process.
     *
     * If your application does not have the <code>android:debuggable</code>
     * flag set in its manifest, the server returned by this method will
     * be a dummy object that does not do anything. This allows you to use
     * the same code in debug and release versions of your application.
     *
     * @param context A Context used to check whether the application is
     *                debuggable, this can be the application context
     */
    public static ViewServer get(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
//        if (BUILD_TYPE_USER.equals(Build.TYPE) &&
        if ((info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            if (sServer == null) {
                sServer = new ViewServer(ViewServer.VIEW_SERVER_DEFAULT_PORT);
            }

            if (!sServer.isRunning()) {
                try {
                    sServer.start();
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Error:", e);
                }
            }
        } else {
            sServer = new NoopViewServer();
        }

        return sServer;
    }

    private ViewServer() {
        mPort = -1;
    }

    /**
     * Creates a new ViewServer associated with the specified window manager on the
     * specified local port. The server is not started by default.
     *
     * @param port The port for the server to listen to.
     *
     * @see #start()
     */
    private ViewServer(int port) {
        mPort = port;
    }

    /**
     * Starts the server.
     *
     * @return True if the server was successfully created, or false if it already exists.
     * @throws IOException If the server cannot be created.
     *
     * @see #stop()
     * @see #isRunning()
     * @see WindowManagerService#startViewServer(int)
     */
    public boolean start() throws IOException {
        Log.i(LOG_TAG, "starting view server");
        if (mThread != null) {
            return false;
        }

        mThread = new Thread(this, "Local View Server [port=" + mPort + "]");
        mThreadPool = Executors.newFixedThreadPool(VIEW_SERVER_MAX_CONNECTIONS);
        mThread.start();

        return true;
    }

    /**
     * Stops the server.
     *
     * @return True if the server was stopped, false if an error occurred or if the
     *         server wasn't started.
     *
     * @see #start()
     * @see #isRunning()
     * @see WindowManagerService#stopViewServer()
     */
    public boolean stop() {
        if (mThread != null) {
            mThread.interrupt();
            if (mThreadPool != null) {
                try {
                    mThreadPool.shutdownNow();
                } catch (SecurityException e) {
                    Log.w(LOG_TAG, "Could not stop all view server threads");
                }
            }

            mThreadPool = null;
            mThread = null;

            try {
                mServer.close();
                mServer = null;
                return true;
            } catch (IOException e) {
                Log.w(LOG_TAG, "Could not close the view server");
            }
        }

        mWindowsLock.writeLock().lock();
        try {
            mWindows.clear();
        } finally {
            mWindowsLock.writeLock().unlock();
        }

        mFocusLock.writeLock().lock();
        try {
            mFocusedWindow = null;
        } finally {
            mFocusLock.writeLock().unlock();
        }

        return false;
    }

    /**
     * Indicates whether the server is currently running.
     *
     * @return True if the server is running, false otherwise.
     *
     * @see #start()
     * @see #stop()
     * @see WindowManagerService#isViewServerRunning()
     */
    public boolean isRunning() {
        return mThread != null && mThread.isAlive();
    }

    /**
     * Invoke this method to register a new view hierarchy.
     *
     * @param activity The activity whose view hierarchy/window to register
     *
     * @see #addWindow(View, String)
     * @see #removeWindow(Activity)
     */
    public void addWindow(Activity activity) {
        String name = activity.getTitle().toString();
        if (TextUtils.isEmpty(name)) {
            name = activity.getClass().getCanonicalName() +
                    "/0x" + System.identityHashCode(activity);
        } else {
            name += "(" + activity.getClass().getCanonicalName() + ")";
        }
        addWindow(activity.getWindow().getDecorView(), name);
    }

    /**
     * Invoke this method to unregister a view hierarchy.
     *
     * @param activity The activity whose view hierarchy/window to unregister
     *
     * @see #addWindow(Activity)
     * @see #removeWindow(View)
     */
    public void removeWindow(Activity activity) {
        removeWindow(activity.getWindow().getDecorView());
    }

    /**
     * Invoke this method to register a new view hierarchy.
     *
     * @param view A view that belongs to the view hierarchy/window to register
     * @name name The name of the view hierarchy/window to register
     *
     * @see #removeWindow(View)
     */
    public void addWindow(View view, String name) {
        mWindowsLock.writeLock().lock();
        try {
            mWindows.put(view.getRootView(), name);
        } finally {
            mWindowsLock.writeLock().unlock();
        }
        fireWindowsChangedEvent();
    }

    /**
     * Invoke this method to unregister a view hierarchy.
     *
     * @param view A view that belongs to the view hierarchy/window to unregister
     *
     * @see #addWindow(View, String)
     */
    public void removeWindow(View view) {
        mWindowsLock.writeLock().lock();
        try {
            mWindows.remove(view.getRootView());
        } finally {
            mWindowsLock.writeLock().unlock();
        }
        fireWindowsChangedEvent();
    }

    /**
     * Invoke this method to change the currently focused window.
     *
     * @param activity The activity whose view hierarchy/window hasfocus,
     *                 or null to remove focus
     */
    public void setFocusedWindow(Activity activity) {
        setFocusedWindow(activity.getWindow().getDecorView());
    }

    /**
     * Invoke this method to change the currently focused window.
     *
     * @param view A view that belongs to the view hierarchy/window that has focus,
     *             or null to remove focus
     */
    public void setFocusedWindow(View view) {
        mFocusLock.writeLock().lock();
        try {
            mFocusedWindow = view == null ? null : view.getRootView();
        } finally {
            mFocusLock.writeLock().unlock();
        }
        fireFocusChangedEvent();
    }

    /**
     * Main server loop.
     */
    public void run() {
        try {
            mServer = new ServerSocket(mPort, VIEW_SERVER_MAX_CONNECTIONS, InetAddress.getLocalHost());
        } catch (Exception e) {
            Log.w(LOG_TAG, "Starting ServerSocket error: ", e);
        }

        while (mServer != null && Thread.currentThread() == mThread) {
            // Any uncaught exception will crash the system process
            try {
                Socket client = mServer.accept();
                if (mThreadPool != null) {
                    mThreadPool.submit(new ViewServerWorker(client));
                } else {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Log.w(LOG_TAG, "Connection error: ", e);
            }
        }
    }

    private void fireWindowsChangedEvent() {
        for (WindowListener listener : mListeners) {
            listener.windowsChanged();
        }
    }

    private void fireFocusChangedEvent() {
        for (WindowListener listener : mListeners) {
            listener.focusChanged();
        }
    }

    private interface WindowListener {
        void windowsChanged();
        void focusChanged();
    }

    private static class UncloseableOuputStream extends OutputStream {
        private final OutputStream mStream;

        UncloseableOuputStream(OutputStream stream) {
            mStream = stream;
        }

        public void close() throws IOException {
            // Don't close.
        }

        public boolean equals(Object o) {
            return mStream.equals(o);
        }

        public void flush() throws IOException {
            mStream.flush();
        }

        public int hashCode() {
            return mStream.hashCode();
        }

        public String toString() {
            return mStream.toString();
        }

        public void write(byte[] buffer, int offset, int count)
                throws IOException {
            mStream.write(buffer, offset, count);
        }

        public void write(byte[] buffer) throws IOException {
            mStream.write(buffer);
        }

        public void write(int oneByte) throws IOException {
            mStream.write(oneByte);
        }
    }

    private static class NoopViewServer extends ViewServer {
        private NoopViewServer() {
        }

        @Override
        public boolean start() throws IOException {
            return false;
        }

        @Override
        public boolean stop() {
            return false;
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public void addWindow(Activity activity) {
        }

        @Override
        public void removeWindow(Activity activity) {
        }

        @Override
        public void addWindow(View view, String name) {
        }

        @Override
        public void removeWindow(View view) {
        }

        @Override
        public void setFocusedWindow(Activity activity) {
        }

        @Override
        public void setFocusedWindow(View view) {
        }

        @Override
        public void run() {
        }
    }

    private class ViewServerWorker implements Runnable {
        private Socket mClient;

        public ViewServerWorker(Socket client) {
            mClient = client;
        }

        @Override
        public void run() {
            BufferedReader in = null;
            BufferedWriter out = null;
            try {
                in = new BufferedReader(new InputStreamReader(mClient.getInputStream()), 1024);
                final String request = in.readLine();

                String command;
                String parameters;

                int index = request.indexOf(' ');
                if (index == -1) {
                    command = request;
                    parameters = "";
                } else {
                    command = request.substring(0, index);
                    parameters = request.substring(index + 1);
                }

                boolean isJson = false;
                if (parameters != null && parameters.equalsIgnoreCase("json")) {
                    isJson = true;
                }

                boolean result = false;
                if (COMMAND_PRINT_HIERARCHY.equalsIgnoreCase(command)) {
                    if (!mClient.isOutputShutdown()) {
                        out = new BufferedWriter(new OutputStreamWriter(new UncloseableOuputStream(mClient.getOutputStream())));

                        // Print hierarchy.
                        if (isJson) {
                            result = JsonPrinter.printHierarchy(out, mFocusedWindow);
                        } else {
                            result = XMLPrinter.printHierarchy(out, mFocusedWindow);
                        }
                    } else {
                        Log.i(LOG_TAG, "output is shutdown");
                    }
                }

                if (!result) {
                    Log.w(LOG_TAG, "An error occurred with the command: " + command);
                }
            } catch(Exception e) {
                Log.w(LOG_TAG, "Connection error: ", e);
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.i(LOG_TAG, "exception while closing output stream");
                    }
                }
            }
        }
    }
}
