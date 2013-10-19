/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.sriramramani.droid.inspector.server;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

final class JsonPrinter {

    // PhoneWindow class
    private static final String PHONE_WINDOW = "PhoneWindow";

    // Identifier for base64 encoding.
    private static final String BASE64_IDENTIFIER = "data:image/png;base64,";

    private JsonPrinter() {
        // .xXx.
    }

    /**
     * Prints the hierarchy of a view to the output stream.
     *
     * @param out BufferedWriter to write the contents.
     * @param view View for capturing layers.
     */
    public static boolean printHierarchy(BufferedWriter out, final View view) throws IOException {
        /*
         * Note: Always the writing should happen in the thread this method was called on.
         * This method will be called from one of the background threads from the ViewServer.
         * The methods on a view should always be called on the UI thread.
         * And, never write from the UI thread! :sigh:
         */
        final Handler handler = view.getHandler();
        final FutureTask<String> properties = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getProperties(view);
            }
        });

        final FutureTask<String> background = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getBackground(view);
            }
        });

        final FutureTask<String> content = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getContent(view);
            }
        });

        handler.post(properties);
        handler.post(background);
        handler.post(content);

        out.write("{");

        try {
            // Keep waiting.
            String result;
            result = properties.get();
            out.write(result);

            result = background.get();
            if (result != null) {
                out.write(result);
            }

            result = content.get();
            if (result != null) {
                out.write(result);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        out.flush();

        out.write(", 'children':[");
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            final int count = group.getChildCount();
            for (int i=0; i < count; i++) {
                printHierarchy(out, group.getChildAt(i));
                if (i + 1 < count) {
                    out.write(",");
                }
            }
        }
        out.write("]");
        out.write("}");

        out.flush();
        return true;
    }

    /**
     * Get the properties of the view.
     *
     * @param view View for getting the properties.
     * @result Return a string of the properties.
     */
    private static String getProperties(View view) {
        StringBuffer buffer = new StringBuffer(200);
        String className = view.getClass().getName();

        StringBuffer idName = new StringBuffer(20);
        final int id = view.getId();
        if (id != View.NO_ID) {
            final Resources res = view.getResources();
            if (id != 0 && res != null) {
                try {
                    String pkg;
                    switch (id & 0xff000000) {
                        case 0x7f000000:
                            pkg = "app";
                            break;
                        case 0x01000000:
                            pkg = "android";
                            break;
                        default:
                            pkg = res.getResourcePackageName(id);
                            break;
                    }
                    String typename = res.getResourceTypeName(id);
                    String entryname = res.getResourceEntryName(id);
                    idName.append("[");
                    idName.append("@");
                    idName.append(pkg);
                    idName.append(":");
                    idName.append(typename);
                    idName.append("/");
                    idName.append(entryname);
                    idName.append("]");
                } catch (Resources.NotFoundException e) {
                }
            }
        }

        buffer.append(" 'name':'" + className + "',");
        buffer.append(" 'id':'" + idName.toString() + "',");
        buffer.append(" 'hashCode':'" + view.hashCode() + "',");
        buffer.append(" 'bounds':[" + view.getLeft() + "," + view.getTop() + "," + view.getWidth() + "," + view.getHeight() + "],");
        buffer.append(" 'padding':[" + view.getPaddingLeft() + "," + view.getPaddingTop() + "," + view.getPaddingRight() + "," + view.getPaddingBottom() + "],");

        final LayoutParams params = view.getLayoutParams();
        if (params instanceof MarginLayoutParams) {
            MarginLayoutParams margin = (MarginLayoutParams) params;
            buffer.append(" 'margin':[" + margin.leftMargin + "," + margin.topMargin + "," + margin.rightMargin + "," + margin.bottomMargin + "],");
        }

        final Drawable background = view.getBackground();
        if (background != null) {
            Rect padding = new Rect();
            background.getPadding(padding);
            buffer.append(" 'drawablePadding':[" + padding.left + "," + padding.top + "," + padding.right + "," + padding.bottom + "],");
        }

        int visibility = view.getVisibility();
        if (visibility == View.VISIBLE) {
            buffer.append(" 'visibility':1");
        } else if (visibility == View.INVISIBLE) {
            buffer.append(" 'visibility':-1");
        } else {
            buffer.append(" 'visibility':0");
        }

        return buffer.toString();
    }

    private static String getBackground(View view) throws Exception {
        if (view.getVisibility() != View.VISIBLE) {
            return null;
        }

        final Drawable background = view.getBackground();
        if (background == null) {
            return null;
        }

        if (background instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable) background;
            final int color = colorDrawable.getColor();
            return (color == 0) ? null : ", 'backgroundColor':'#" + Integer.toHexString(color) + "'";
        }

        Bitmap bitmap = getBitmap(view);
        if (bitmap == null)
            return null;

        final Canvas canvas = new Canvas(bitmap);

        final int scrollX = view.getScrollX();
        final int scrollY = view.getScrollY();

        background.setBounds(0, 0, view.getRight() - view.getLeft(), view.getBottom() - view.getTop());

        if ((scrollX | scrollY) == 0) {
            background.draw(canvas);
        } else {
            canvas.translate(scrollX, scrollY);
            background.draw(canvas);
            canvas.translate(-scrollX, -scrollY);
        }

        final StringBuffer buffer = new StringBuffer();
        buffer.append(", 'backgroundImage':'");
        compressBitmapToString(buffer, bitmap, canvas);
        buffer.append("'");
        return buffer.toString();
    }

    private static String getContent(View view) throws Exception {
        if (isPhoneWindow(view) || view.getVisibility() != View.VISIBLE) {
            return null;
        }

        Bitmap bitmap = getBitmap(view);
        if (bitmap == null) {
            return null;
        }

        final ProxyCanvas canvas = new ProxyCanvas(bitmap);
        bitmap.eraseColor(0x0);

        int visibilities[] = null;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            final int count = group.getChildCount();

            // This would fail for List dividers as they look for visible items.
            visibilities = new int[count];
            for (int i=0; i < count; i++) {
                View child = group.getChildAt(i);
                visibilities[i] = child.getVisibility();
                if (visibilities[i] == View.VISIBLE) {
                    child.setVisibility(View.INVISIBLE);
                }
            }
        }

        Class<?> clazz = view.getClass();
        while (clazz != View.class) {
            try {
                Method method = clazz.getDeclaredMethod("onDraw", Canvas.class);
                method.setAccessible(true);
                method.invoke(view, canvas);
                break;
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            final int count = group.getChildCount();

            if (count > 0) {
                for (int i=0; i < count; i++) {
                    group.getChildAt(i).setVisibility(visibilities[i]);
                }
            }
        }

        if (!canvas.wasTouched()) {
            return null;
        }

        final StringBuffer buffer = new StringBuffer();
        buffer.append(", 'content':'");
        compressBitmapToString(buffer, bitmap, canvas);
        buffer.append("'");
        return buffer.toString();
    }

    private static Bitmap getBitmap(View view) {
        if (view.getWidth() == 0 || view.getHeight() == 0) {
            return null;
        }

        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        } catch(OutOfMemoryError e) {
        }

        return bitmap;
    }

    private static void compressBitmapToString(StringBuffer out, Bitmap bitmap, Canvas canvas) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        canvas.setBitmap(null);
        bitmap.recycle();

        out.append(BASE64_IDENTIFIER);
        out.append(Base64.encodeToString(bytes, Base64.NO_PADDING | Base64.NO_WRAP));
    }

    private static boolean isPhoneWindow(View view) {
        return view.getClass().getName().contains(PHONE_WINDOW);
    }
}
