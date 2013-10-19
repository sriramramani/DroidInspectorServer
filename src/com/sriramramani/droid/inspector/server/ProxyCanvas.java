/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.sriramramani.droid.inspector.server;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;

/**
 * A proxy Canvas to see if something was ever drawn
 * on the canvas. This is an optimization for compressing
 * bitmaps.
 */
class ProxyCanvas extends Canvas {
    private boolean mTouched = false;

    public ProxyCanvas() {
        super();
    }

    public ProxyCanvas(Bitmap bitmap) {
        super(bitmap);
    }

    public boolean wasTouched() {
        return mTouched;
    }

    @Override
    public boolean clipPath(Path path, Op op) {
        mTouched = true;
        return super.clipPath(path, op);
    }

    @Override
    public boolean clipPath(Path path) {
        mTouched = true;
        return super.clipPath(path);
    }

    @Override
    public boolean clipRect(float left, float top, float right, float bottom,
            Op op) {
        mTouched = true;
        return super.clipRect(left, top, right, bottom, op);
    }

    @Override
    public boolean clipRect(float left, float top, float right, float bottom) {
        mTouched = true;
        return super.clipRect(left, top, right, bottom);
    }

    @Override
    public boolean clipRect(int left, int top, int right, int bottom) {
        mTouched = true;
        return super.clipRect(left, top, right, bottom);
    }

    @Override
    public boolean clipRect(Rect rect, Op op) {
        mTouched = true;
        return super.clipRect(rect, op);
    }

    @Override
    public boolean clipRect(Rect rect) {
        mTouched = true;
        return super.clipRect(rect);
    }

    @Override
    public boolean clipRect(RectF rect, Op op) {
        mTouched = true;
        return super.clipRect(rect, op);
    }

    @Override
    public boolean clipRect(RectF rect) {
        mTouched = true;
        return super.clipRect(rect);
    }

    @Override
    public boolean clipRegion(Region region, Op op) {
        mTouched = true;
        return super.clipRegion(region, op);
    }

    @Override
    public boolean clipRegion(Region region) {
        mTouched = true;
        return super.clipRegion(region);
    }

    @Override
    public void concat(Matrix matrix) {
        mTouched = true;
        super.concat(matrix);
    }

    @Override
    public void drawARGB(int a, int r, int g, int b) {
        mTouched = true;
        super.drawARGB(a, r, g, b);
    }

    @Override
    public void drawArc(RectF oval, float startAngle, float sweepAngle,
            boolean useCenter, Paint paint) {
        mTouched = true;
        super.drawArc(oval, startAngle, sweepAngle, useCenter, paint);
    }

    @Override
    public void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
        mTouched = true;
        super.drawBitmap(bitmap, left, top, paint);
    }

    @Override
    public void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
        mTouched = true;
        super.drawBitmap(bitmap, matrix, paint);
    }

    @Override
    public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
        mTouched = true;
        super.drawBitmap(bitmap, src, dst, paint);
    }

    @Override
    public void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) {
        mTouched = true;
        super.drawBitmap(bitmap, src, dst, paint);
    }

    @Override
    public void drawBitmap(int[] colors, int offset, int stride, float x,
            float y, int width, int height, boolean hasAlpha, Paint paint) {
        mTouched = true;
        super.drawBitmap(colors, offset, stride, x, y, width, height, hasAlpha, paint);
    }

    @Override
    public void drawBitmap(int[] colors, int offset, int stride, int x, int y,
            int width, int height, boolean hasAlpha, Paint paint) {
        mTouched = true;
        super.drawBitmap(colors, offset, stride, x, y, width, height, hasAlpha, paint);
    }

    @Override
    public void drawBitmapMesh(Bitmap bitmap, int meshWidth, int meshHeight,
            float[] verts, int vertOffset, int[] colors, int colorOffset,
            Paint paint) {
        mTouched = true;
        super.drawBitmapMesh(bitmap, meshWidth, meshHeight, verts, vertOffset, colors,
                colorOffset, paint);
    }

    @Override
    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        mTouched = true;
        super.drawCircle(cx, cy, radius, paint);
    }

    @Override
    public void drawColor(int color, Mode mode) {
        mTouched = true;
        super.drawColor(color, mode);
    }

    @Override
    public void drawColor(int color) {
        mTouched = true;
        super.drawColor(color);
    }

    @Override
    public void drawLine(float startX, float startY, float stopX, float stopY,
            Paint paint) {
        mTouched = true;
        super.drawLine(startX, startY, stopX, stopY, paint);
    }

    @Override
    public void drawLines(float[] pts, int offset, int count, Paint paint) {
        mTouched = true;
        super.drawLines(pts, offset, count, paint);
    }

    @Override
    public void drawLines(float[] pts, Paint paint) {
        mTouched = true;
        super.drawLines(pts, paint);
    }

    @Override
    public void drawOval(RectF oval, Paint paint) {
        mTouched = true;
        super.drawOval(oval, paint);
    }

    @Override
    public void drawPaint(Paint paint) {
        mTouched = true;
        super.drawPaint(paint);
    }

    @Override
    public void drawPath(Path path, Paint paint) {
        mTouched = true;
        super.drawPath(path, paint);
    }

    @Override
    public void drawPicture(Picture picture, Rect dst) {
        mTouched = true;
        super.drawPicture(picture, dst);
    }

    @Override
    public void drawPicture(Picture picture, RectF dst) {
        mTouched = true;
        super.drawPicture(picture, dst);
    }

    @Override
    public void drawPicture(Picture picture) {
        mTouched = true;
        super.drawPicture(picture);
    }

    @Override
    public void drawPoint(float x, float y, Paint paint) {
        mTouched = true;
        super.drawPoint(x, y, paint);
    }

    @Override
    public void drawPoints(float[] pts, int offset, int count, Paint paint) {
        mTouched = true;
        super.drawPoints(pts, offset, count, paint);
    }

    @Override
    public void drawPoints(float[] pts, Paint paint) {
        mTouched = true;
        super.drawPoints(pts, paint);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void drawPosText(char[] text, int index, int count, float[] pos,
            Paint paint) {
        mTouched = true;
        super.drawPosText(text, index, count, pos, paint);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void drawPosText(String text, float[] pos, Paint paint) {
        mTouched = true;
        super.drawPosText(text, pos, paint);
    }

    @Override
    public void drawRGB(int r, int g, int b) {
        mTouched = true;
        super.drawRGB(r, g, b);
    }

    @Override
    public void drawRect(float left, float top, float right, float bottom,
            Paint paint) {
        mTouched = true;
        super.drawRect(left, top, right, bottom, paint);
    }

    @Override
    public void drawRect(Rect r, Paint paint) {
        mTouched = true;
        super.drawRect(r, paint);
    }

    @Override
    public void drawRect(RectF rect, Paint paint) {
        mTouched = true;
        super.drawRect(rect, paint);
    }

    @Override
    public void drawRoundRect(RectF rect, float rx, float ry, Paint paint) {
        mTouched = true;
        super.drawRoundRect(rect, rx, ry, paint);
    }

    @Override
    public void drawText(char[] text, int index, int count, float x, float y,
            Paint paint) {
        mTouched = true;
        super.drawText(text, index, count, x, y, paint);
    }

    @Override
    public void drawText(CharSequence text, int start, int end, float x,
            float y, Paint paint) {
        mTouched = true;
        super.drawText(text, start, end, x, y, paint);
    }

    @Override
    public void drawText(String text, float x, float y, Paint paint) {
        mTouched = true;
        super.drawText(text, x, y, paint);
    }

    @Override
    public void drawText(String text, int start, int end, float x, float y,
            Paint paint) {
        mTouched = true;
        super.drawText(text, start, end, x, y, paint);
    }

    @Override
    public void drawTextOnPath(char[] text, int index, int count, Path path,
            float hOffset, float vOffset, Paint paint) {
        mTouched = true;
        super.drawTextOnPath(text, index, count, path, hOffset, vOffset, paint);
    }

    @Override
    public void drawTextOnPath(String text, Path path, float hOffset,
            float vOffset, Paint paint) {
        mTouched = true;
        super.drawTextOnPath(text, path, hOffset, vOffset, paint);
    }

    @Override
    public void drawVertices(VertexMode mode, int vertexCount, float[] verts,
            int vertOffset, float[] texs, int texOffset, int[] colors,
            int colorOffset, short[] indices, int indexOffset, int indexCount,
            Paint paint) {
        mTouched = true;
        super.drawVertices(mode, vertexCount, verts, vertOffset, texs, texOffset,
                colors, colorOffset, indices, indexOffset, indexCount, paint);
    }

    @Override
    public boolean getClipBounds(Rect bounds) {
        mTouched = true;
        return super.getClipBounds(bounds);
    }

    @Override
    public int getDensity() {
        mTouched = true;
        return super.getDensity();
    }

    @Override
    public DrawFilter getDrawFilter() {
        mTouched = true;
        return super.getDrawFilter();
    }

    @Override
    public int getHeight() {
        mTouched = true;
        return super.getHeight();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void getMatrix(Matrix ctm) {
        mTouched = true;
        super.getMatrix(ctm);
    }

    @Override
    public int getMaximumBitmapHeight() {
        mTouched = true;
        return super.getMaximumBitmapHeight();
    }

    @Override
    public int getMaximumBitmapWidth() {
        mTouched = true;
        return super.getMaximumBitmapWidth();
    }

    @Override
    public int getSaveCount() {
        mTouched = true;
        return super.getSaveCount();
    }

    @Override
    public int getWidth() {
        mTouched = true;
        return super.getWidth();
    }

    @Override
    public boolean isHardwareAccelerated() {
        mTouched = true;
        return super.isHardwareAccelerated();
    }

    @Override
    public boolean isOpaque() {
        mTouched = true;
        return super.isOpaque();
    }

    @Override
    public boolean quickReject(float left, float top, float right,
            float bottom, EdgeType type) {
        mTouched = true;
        return super.quickReject(left, top, right, bottom, type);
    }

    @Override
    public boolean quickReject(Path path, EdgeType type) {
        mTouched = true;
        return super.quickReject(path, type);
    }

    @Override
    public boolean quickReject(RectF rect, EdgeType type) {
        mTouched = true;
        return super.quickReject(rect, type);
    }

    @Override
    public void restore() {
        mTouched = true;
        super.restore();
    }

    @Override
    public void restoreToCount(int saveCount) {
        mTouched = true;
        super.restoreToCount(saveCount);
    }

    @Override
    public void rotate(float degrees) {
        mTouched = true;
        super.rotate(degrees);
    }

    @Override
    public int save() {
        mTouched = true;
        return super.save();
    }

    @Override
    public int save(int saveFlags) {
        mTouched = true;
        return super.save(saveFlags);
    }

    @Override
    public int saveLayer(float left, float top, float right, float bottom,
            Paint paint, int saveFlags) {
        mTouched = true;
        return super.saveLayer(left, top, right, bottom, paint, saveFlags);
    }

    @Override
    public int saveLayer(RectF bounds, Paint paint, int saveFlags) {
        mTouched = true;
        return super.saveLayer(bounds, paint, saveFlags);
    }

    @Override
    public int saveLayerAlpha(float left, float top, float right, float bottom,
            int alpha, int saveFlags) {
        mTouched = true;
        return super.saveLayerAlpha(left, top, right, bottom, alpha, saveFlags);
    }

    @Override
    public int saveLayerAlpha(RectF bounds, int alpha, int saveFlags) {
        mTouched = true;
        return super.saveLayerAlpha(bounds, alpha, saveFlags);
    }

    @Override
    public void scale(float sx, float sy) {
        mTouched = true;
        super.scale(sx, sy);
    }

    @Override
    public void setBitmap(Bitmap bitmap) {
        mTouched = true;
        super.setBitmap(bitmap);
    }

    @Override
    public void setDensity(int density) {
        mTouched = true;
        super.setDensity(density);
    }

    @Override
    public void setDrawFilter(DrawFilter filter) {
        mTouched = true;
        super.setDrawFilter(filter);
    }

    @Override
    public void setMatrix(Matrix matrix) {
        mTouched = true;
        super.setMatrix(matrix);
    }

    @Override
    public void skew(float sx, float sy) {
        mTouched = true;
        super.skew(sx, sy);
    }

    @Override
    public void translate(float dx, float dy) {
        mTouched = true;
        super.translate(dx, dy);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        mTouched = true;
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        mTouched = true;
        return super.equals(o);
    }

    @Override
    protected void finalize() throws Throwable {
        mTouched = true;
        super.finalize();
    }

    @Override
    public int hashCode() {
        mTouched = true;
        return super.hashCode();
    }

    @Override
    public String toString() {
        mTouched = true;
        return super.toString();
    }
}
