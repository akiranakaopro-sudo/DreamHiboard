package com.coui.appcompat.roundRect;

import android.graphics.Path;
import android.graphics.RectF;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.version.COUIVersionUtil;
import com.oplus.graphics.OplusPath;
import com.oplus.graphics.OplusPathAdapter;

public class COUIShapePath {

    public static Path getRoundRectPath(
            Path path,
            RectF rectF,
            float radius,
            boolean topLeft,
            boolean topRight,
            boolean bottomLeft,
            boolean bottomRight
    ) {
        float halfWidth;
        float bottomLeftStartX;

        float safeRadius = Math.max(radius, 0.0f);
        path.reset();

        float left = rectF.left;
        float right = rectF.right;
        float bottom = rectF.bottom;
        float top = rectF.top;

        float width = right - left;
        float height = bottom - top;
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        float radiusRatio = safeRadius / Math.min(centerX, centerY);
        float controlScale = ((double) radiusRatio) > 0.5d
                ? 1.0f - (Math.min(1.0f, (radiusRatio - 0.5f) / 0.4f) * 0.13877845f)
                : 1.0f;
        float vertexScale = radiusRatio > 0.6f
                ? 1.0f + (Math.min(1.0f, (radiusRatio - 0.6f) / 0.3f) * 0.042454004f)
                : 1.0f;

        path.moveTo(left + centerX, top);

        if (topRight) {
            float radiusUnit = safeRadius / 100.0f;
            float cornerReach = radiusUnit * 128.19f * controlScale;

            path.lineTo(Math.max(centerX, width - cornerReach) + left, top);

            float cornerRight = left + width;
            float controlX1 = radiusUnit * 83.62f * vertexScale;
            float controlX2 = radiusUnit * 67.45f;
            float controlY1 = radiusUnit * 4.64f;
            float controlX3 = radiusUnit * 51.16f;
            float controlY2 = radiusUnit * 13.36f;

            halfWidth = centerX;

            path.cubicTo(
                    cornerRight - controlX1,
                    top,
                    cornerRight - controlX2,
                    top + controlY1,
                    cornerRight - controlX3,
                    top + controlY2
            );

            float controlX4 = radiusUnit * 34.86f;
            float controlY3 = radiusUnit * 22.07f;

            path.cubicTo(
                    cornerRight - controlX4,
                    top + controlY3,
                    cornerRight - controlY3,
                    top + controlX4,
                    cornerRight - controlY2,
                    top + controlX3
            );

            path.cubicTo(
                    cornerRight - controlY1,
                    top + controlX2,
                    cornerRight,
                    top + controlX1,
                    cornerRight,
                    top + Math.min(centerY, cornerReach)
            );
        } else {
            path.lineTo(left + width, top);
            halfWidth = centerX;
        }

        if (bottomRight) {
            float cornerRight = left + width;
            float radiusUnit = safeRadius / 100.0f;
            float cornerReach = radiusUnit * 128.19f * controlScale;

            path.lineTo(cornerRight, Math.max(centerY, height - cornerReach) + top);

            float cornerBottom = top + height;
            float controlY1 = radiusUnit * 83.62f * vertexScale;
            float controlX1 = radiusUnit * 4.64f;
            float controlY2 = radiusUnit * 67.45f;
            float controlX2 = radiusUnit * 13.36f;
            float controlY3 = radiusUnit * 51.16f;

            path.cubicTo(
                    cornerRight,
                    cornerBottom - controlY1,
                    cornerRight - controlX1,
                    cornerBottom - controlY2,
                    cornerRight - controlX2,
                    cornerBottom - controlY3
            );

            float controlX3 = radiusUnit * 22.07f;
            float controlY4 = radiusUnit * 34.86f;

            path.cubicTo(
                    cornerRight - controlX3,
                    cornerBottom - controlY4,
                    cornerRight - controlY4,
                    cornerBottom - controlX3,
                    cornerRight - controlY3,
                    cornerBottom - controlX2
            );

            float endControlX = cornerRight - controlY1;
            bottomLeftStartX = halfWidth;

            path.cubicTo(
                    cornerRight - controlY2,
                    cornerBottom - controlX1,
                    endControlX,
                    cornerBottom,
                    left + Math.max(bottomLeftStartX, width - cornerReach),
                    cornerBottom
            );
        } else {
            path.lineTo(width + left, top + height);
            bottomLeftStartX = halfWidth;
        }

        if (bottomLeft) {
            float radiusUnit = safeRadius / 100.0f;
            float cornerReach = radiusUnit * 128.19f * controlScale;
            float cornerBottom = top + height;

            path.lineTo(Math.min(bottomLeftStartX, cornerReach) + left, cornerBottom);

            float controlX1 = radiusUnit * 83.62f * vertexScale;
            float controlX2 = radiusUnit * 67.45f;
            float controlY1 = radiusUnit * 4.64f;
            float controlX3 = radiusUnit * 51.16f;
            float controlY2 = radiusUnit * 13.36f;

            path.cubicTo(
                    left + controlX1,
                    cornerBottom,
                    left + controlX2,
                    cornerBottom - controlY1,
                    left + controlX3,
                    cornerBottom - controlY2
            );

            float controlX4 = radiusUnit * 34.86f;
            float controlY3 = radiusUnit * 22.07f;

            path.cubicTo(
                    left + controlX4,
                    cornerBottom - controlY3,
                    left + controlY3,
                    cornerBottom - controlX4,
                    left + controlY2,
                    cornerBottom - controlX3
            );

            path.cubicTo(
                    left + controlY1,
                    cornerBottom - controlX2,
                    left,
                    cornerBottom - controlX1,
                    left,
                    top + Math.max(centerY, height - cornerReach)
            );
        } else {
            path.lineTo(left, height + top);
        }

        if (topLeft) {
            float radiusUnit = safeRadius / 100.0f;
            float cornerReach = 128.19f * radiusUnit * controlScale;

            path.lineTo(left, Math.min(centerY, cornerReach) + top);

            float controlY1 = 83.62f * radiusUnit * vertexScale;
            float controlX1 = 4.64f * radiusUnit;
            float controlY2 = 67.45f * radiusUnit;
            float controlX2 = 13.36f * radiusUnit;
            float controlY3 = 51.16f * radiusUnit;

            path.cubicTo(
                    left,
                    top + controlY1,
                    left + controlX1,
                    top + controlY2,
                    left + controlX2,
                    top + controlY3
            );

            float controlX3 = 22.07f * radiusUnit;
            float controlY4 = radiusUnit * 34.86f;

            path.cubicTo(
                    left + controlX3,
                    top + controlY4,
                    left + controlY4,
                    top + controlX3,
                    left + controlY3,
                    top + controlX2
            );

            path.cubicTo(
                    left + controlY2,
                    top + controlX1,
                    left + controlY1,
                    top,
                    left + Math.min(bottomLeftStartX, cornerReach),
                    top
            );
        } else {
            path.lineTo(left, top);
        }

        path.close();
        return path;
    }

    public static Path getSmoothRoundRectPath(
            Path path,
            RectF rectF,
            float radius,
            float weight,
            boolean topLeft,
            boolean topRight,
            boolean bottomLeft,
            boolean bottomRight
    ) {
        if (path != null && rectF != null) {
            float[] radii = new float[8];

            path.reset();

            float topLeftRadius = topLeft ? radius : 0.0f;
            float topRightRadius = topRight ? radius : 0.0f;
            float bottomLeftRadius = bottomLeft ? radius : 0.0f;
            float bottomRightRadius = bottomRight ? radius : 0.0f;

            radii[1] = topLeftRadius;
            radii[0] = topLeftRadius;
            radii[3] = topRightRadius;
            radii[2] = topRightRadius;
            radii[5] = bottomLeftRadius;
            radii[4] = bottomLeftRadius;
            radii[7] = bottomRightRadius;
            radii[6] = bottomRightRadius;

            if (!RoundCornerUtil.isPathSupportSingleCorner()
                    || !RoundCornerUtil.isSmoothRoundRectOn()) {
                path.addRoundRect(rectF, radii, Path.Direction.CW);
            } else if (RoundCornerUtil.getSmoothStyleType() != 1) {
                new OplusPath(path).addSmoothRoundRect(rectF, radii, Path.Direction.CW, weight);
            } else if (COUIVersionUtil.getOSVersionCode() > 37) {
                new OplusPathAdapter(path, 1).addSmoothRoundRect(
                        rectF,
                        radii,
                        Path.Direction.CW,
                        weight
                );
            } else {
                new OplusPathAdapter(path, 1).addSmoothRoundRect(
                        rectF,
                        radii,
                        Path.Direction.CW
                );
            }
        }

        return path;
    }

    private static boolean isWeightValid(float weight) {
        return weight != 0.0f;
    }

    public static Path getSmoothRoundRectPath(
            Path path,
            RectF rectF,
            float radius,
            float weight
    ) {
        return getSmoothRoundRectPath(path, rectF, radius, weight, true, true, true, true);
    }

    public static Path getRoundRectPath(Path path, RectF rectF, float topRadius, float bottomRadius) {
        float safeTopRadius = Math.max(topRadius, 0.0f);
        float safeBottomRadius = Math.max(bottomRadius, 0.0f);

        path.reset();

        float left = rectF.left;
        float right = rectF.right;
        float bottom = rectF.bottom;
        float top = rectF.top;

        float width = right - left;
        float height = bottom - top;
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        float topRadiusRatio = safeTopRadius / Math.min(centerX, centerY);
        float topControlScale = ((double) topRadiusRatio) > 0.5d
                ? 1.0f - (Math.min(1.0f, (topRadiusRatio - 0.5f) / 0.4f) * 0.13877845f)
                : 1.0f;

        float bottomRadiusRatio = safeBottomRadius / Math.min(centerX, centerY);
        float bottomControlScale = ((double) bottomRadiusRatio) > 0.5d
                ? 1.0f - (Math.min(1.0f, (bottomRadiusRatio - 0.5f) / 0.4f) * 0.13877845f)
                : 1.0f;

        float topVertexScale = topRadiusRatio > 0.6f
                ? (Math.min(1.0f, (topRadiusRatio - 0.6f) / 0.3f) * 0.042454004f) + 1.0f
                : 1.0f;

        float bottomVertexScale = bottomRadiusRatio > 0.6f
                ? 1.0f + (Math.min(1.0f, (bottomRadiusRatio - 0.6f) / 0.3f) * 0.042454004f)
                : 1.0f;

        path.moveTo(left + centerX, top);

        float topRadiusUnit = safeTopRadius / 100.0f;
        float topCornerReach = topRadiusUnit * 128.19f * topControlScale;

        path.lineTo(Math.max(centerX, width - topCornerReach) + left, top);

        float cornerRight = left + width;
        float topControlX1 = topRadiusUnit * 83.62f * topVertexScale;
        float topControlX2 = topRadiusUnit * 67.45f;
        float topControlY1 = topRadiusUnit * 4.64f;
        float topY1 = top + topControlY1;
        float topControlX3 = topRadiusUnit * 51.16f;
        float topControlY2 = topRadiusUnit * 13.36f;
        float topY2 = top + topControlY2;

        path.cubicTo(
                cornerRight - topControlX1,
                top,
                cornerRight - topControlX2,
                topY1,
                cornerRight - topControlX3,
                topY2
        );

        float topControlX4 = topRadiusUnit * 34.86f;
        float topControlY3 = topRadiusUnit * 22.07f;
        float topY3 = top + topControlY3;
        float topY4 = top + topControlX4;
        float topY5 = top + topControlX3;

        path.cubicTo(
                cornerRight - topControlX4,
                topY3,
                cornerRight - topControlY3,
                topY4,
                cornerRight - topControlY2,
                topY5
        );

        float topY6 = top + topControlX2;
        float topY7 = top + topControlX1;

        path.cubicTo(
                cornerRight - topControlY1,
                topY6,
                cornerRight,
                topY7,
                cornerRight,
                top + Math.min(centerY, topCornerReach)
        );

        float bottomRadiusUnit = safeBottomRadius / 100.0f;
        float bottomCornerReach = bottomControlScale * 128.19f * bottomRadiusUnit;
        float bottomReachStart = height - bottomCornerReach;

        path.lineTo(cornerRight, Math.max(centerY, bottomReachStart) + top);

        float cornerBottom = height + top;
        float bottomControlY1 = 83.62f * bottomRadiusUnit * bottomVertexScale;
        float bottomY1 = cornerBottom - bottomControlY1;
        float bottomControlX1 = 4.64f * bottomRadiusUnit;
        float bottomControlY2 = 67.45f * bottomRadiusUnit;
        float bottomY2 = cornerBottom - bottomControlY2;
        float bottomControlX2 = 13.36f * bottomRadiusUnit;
        float bottomControlY3 = 51.16f * bottomRadiusUnit;
        float bottomY3 = cornerBottom - bottomControlY3;

        path.cubicTo(
                cornerRight,
                bottomY1,
                cornerRight - bottomControlX1,
                bottomY2,
                cornerRight - bottomControlX2,
                bottomY3
        );

        float bottomControlX3 = 22.07f * bottomRadiusUnit;
        float bottomControlY4 = bottomRadiusUnit * 34.86f;
        float bottomY4 = cornerBottom - bottomControlY4;
        float bottomY5 = cornerBottom - bottomControlX3;
        float bottomY6 = cornerBottom - bottomControlX2;

        path.cubicTo(
                cornerRight - bottomControlX3,
                bottomY4,
                cornerRight - bottomControlY4,
                bottomY5,
                cornerRight - bottomControlY3,
                bottomY6
        );

        float bottomY7 = cornerBottom - bottomControlX1;

        path.cubicTo(
                cornerRight - bottomControlY2,
                bottomY7,
                cornerRight - bottomControlY1,
                cornerBottom,
                left + Math.max(centerX, width - bottomCornerReach),
                cornerBottom
        );

        path.lineTo(left + Math.min(centerX, bottomCornerReach), cornerBottom);

        path.cubicTo(
                left + bottomControlY1,
                cornerBottom,
                left + bottomControlY2,
                bottomY7,
                left + bottomControlY3,
                bottomY6
        );

        path.cubicTo(
                left + bottomControlY4,
                bottomY5,
                left + bottomControlX3,
                bottomY4,
                left + bottomControlX2,
                bottomY3
        );

        path.cubicTo(
                left + bottomControlX1,
                bottomY2,
                left,
                bottomY1,
                left,
                top + Math.max(centerY, bottomReachStart)
        );

        path.lineTo(left, Math.min(centerY, topCornerReach) + top);

        path.cubicTo(
                left,
                topY7,
                left + topControlY1,
                topY6,
                left + topControlY2,
                topY5
        );

        path.cubicTo(
                left + topControlY3,
                topY4,
                left + topControlX4,
                topY3,
                left + topControlX3,
                topY2
        );

        path.cubicTo(
                left + topControlX2,
                topY1,
                left + topControlX1,
                top,
                left + Math.min(centerX, topCornerReach),
                top
        );

        path.close();
        return path;
    }

    public static Path getRoundRectPath(Path path, RectF rectF, float radius) {
        return getRoundRectPath(path, rectF, radius, true, true, true, true);
    }
}